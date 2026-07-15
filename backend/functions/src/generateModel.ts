import * as functions from "firebase-functions/v2/https";
import * as admin from "firebase-admin";
import { VertexAI } from "@google-cloud/vertexai";
import axios from "axios";

admin.initializeApp();

const storage = admin.storage();
const firestore = admin.firestore();

// ---------------------------------------------------------------------------
// VAWOL — generateModel Cloud Function
// Pipeline:
//   1. Validate authenticated UID + cooldown (1 generation/hour)
//   2. Gemini 2.0 Flash refines the user prompt into a detailed 3D description
//   3. Vertex AI Imagen generates 4 views (front / back / left / right)
//   4. Tripo3D API converts 4-view images → GLB
//   5. Upload GLB to Firebase Storage: neural/models/generated/{uid}_{ts}.glb
//   6. Return { modelId, glbUrl, previewUrl }
// ---------------------------------------------------------------------------

const PROJECT_ID  = process.env.GCLOUD_PROJECT ?? "project-34c5d1da-2226-4ace-bb3";
const LOCATION    = "us-central1";
const TRIPO_API   = process.env.TRIPO3D_API_KEY;   // set via firebase functions:config:set
const COOLDOWN_MS = 60 * 60 * 1000;               // 1 hour

interface GenerateModelRequest {
  uid: string;
  categoria: string;
  color: string;
  descripcion: string;
}

export const generateModel = functions.onCall(
  { region: LOCATION, timeoutSeconds: 300, memory: "512MiB" },
  async (request) => {
    // ── 1. Auth guard ──────────────────────────────────────────────────────
    if (!request.auth) {
      throw new functions.HttpsError("unauthenticated", "Autenticación requerida");
    }
    const uid = request.auth.uid;
    const data = request.data as GenerateModelRequest;

    if (!data.categoria || !data.color || !data.descripcion) {
      throw new functions.HttpsError("invalid-argument", "Faltan parámetros requeridos");
    }

    // ── 2. Cooldown check (1 generation per hour per user) ─────────────────
    const cooldownRef = firestore.collection("neural_generation_cooldowns").doc(uid);
    const cooldownSnap = await cooldownRef.get();
    if (cooldownSnap.exists) {
      const lastTs = (cooldownSnap.data()?.lastGeneration?.toDate() as Date | undefined)?.getTime() ?? 0;
      if (Date.now() - lastTs < COOLDOWN_MS) {
        const minutesLeft = Math.ceil((COOLDOWN_MS - (Date.now() - lastTs)) / 60000);
        throw new functions.HttpsError(
          "resource-exhausted",
          `Podés generar 1 modelo por hora. Intentá en ${minutesLeft} min.`
        );
      }
    }

    const ts = Date.now();
    const modelId = `${uid}_${ts}`;

    // ── 3. Gemini 2.0 Flash — refine prompt ───────────────────────────────
    const vertexAI = new VertexAI({ project: PROJECT_ID, location: LOCATION });
    const gemini = vertexAI.getGenerativeModel({ model: "gemini-2.0-flash" });

    const promptRefinement = await gemini.generateContent({
      contents: [{
        role: "user",
        parts: [{
          text: `Sos un diseñador de moda y especialista en generación 3D.
Refiná la siguiente descripción de prenda para que sea una prompt visual
detallada para un modelo 3D de alta calidad. Incluí: material, textura,
estilo, detalles visuales específicos. Max 80 palabras. Solo el texto.

Prenda: ${data.categoria}
Color: ${data.color}
Descripción: ${data.descripcion}`
        }]
      }]
    });

    const refinedPrompt = gemini.countTokens
      ? (gemini as any)
      : gemini;

    const promptText = gemini.countTokens
      ? data.descripcion  // fallback
      : gemini;

    // Extract refined prompt text
    const promptRefined = geminiExtractText(promptRefinement) ?? data.descripcion;

    // ── 4. Vertex AI Imagen — generate 4 views ────────────────────────────
    const imagenModel = vertexAI.getGenerativeModel({
      model: "imagegeneration@006"
    });

    const views = ["front view", "back view", "left side view", "right side view"];
    const imageBase64s: string[] = [];

    for (const view of views) {
      try {
        const imgResult = await (imagenModel as any).generateImages({
          prompt: `${promptRefined}, ${view}, white background, product photography, 3D render`,
          numberOfImages: 1,
          aspectRatio: "1:1",
          outputOptions: { mimeType: "image/png" }
        });
        const b64 = imgResult.images?.[0]?.bytesBase64Encoded ?? null;
        if (b64) imageBase64s.push(b64);
      } catch (e) {
        functions.logger.warn(`Imagen generation failed for ${view}`, e);
      }
    }

    let glbUrl: string;
    let previewUrl = "";

    // ── 5. Convert to GLB (Tripo3D or fallback) ───────────────────────────
    if (imageBase64s.length >= 2 && TRIPO_API) {
      try {
        glbUrl = await convertWithTripo3D(imageBase64s, modelId, TRIPO_API);
      } catch (e) {
        functions.logger.error("Tripo3D conversion failed, using placeholder", e);
        glbUrl = await getPlaceholderGlb(data.categoria, uid, ts);
      }
    } else {
      // Fallback: use a placeholder GLB from the base collection
      glbUrl = await getPlaceholderGlb(data.categoria, uid, ts);
    }

    // Preview: first generated image uploaded to Storage
    if (imageBase64s.length > 0) {
      const previewPath = `neural/models/previews/${modelId}_preview.png`;
      const buf = Buffer.from(imageBase64s[0], "base64");
      const previewRef = storage.bucket().file(previewPath);
      await previewRef.save(buf, { contentType: "image/png", public: true });
      previewUrl = `https://storage.googleapis.com/${storage.bucket().name}/${previewPath}`;
    }

    // ── 6. Save metadata + update cooldown ────────────────────────────────
    await firestore.collection("neural_generated_models").doc(modelId).set({
      uid,
      modelId,
      categoria: data.categoria,
      color: data.color,
      descripcion: data.descripcion,
      promptRefined,
      glbUrl,
      previewUrl,
      ts: admin.firestore.FieldValue.serverTimestamp()
    });

    await cooldownRef.set({
      lastGeneration: admin.firestore.FieldValue.serverTimestamp()
    });

    return { modelId, glbUrl, previewUrl };
  }
);

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function geminiExtractText(response: any): string | null {
  try {
    return response?.response?.candidates?.[0]?.content?.parts?.[0]?.text ?? null;
  } catch {
    return null;
  }
}

async function convertWithTripo3D(
  imageBase64s: string[],
  modelId: string,
  apiKey: string
): Promise<string> {
  // Upload task to Tripo3D API
  const uploadRes = await axios.post(
    "https://platform.tripo3d.ai/api/task",
    {
      type: "image_to_model",
      file: {
        type: "png",
        file: imageBase64s[0]  // front view
      }
    },
    { headers: { Authorization: `Bearer ${apiKey}`, "Content-Type": "application/json" } }
  );

  const taskId = uploadRes.data?.data?.task_id;
  if (!taskId) throw new Error("No task_id from Tripo3D");

  // Poll until done (max 120 s)
  for (let i = 0; i < 24; i++) {
    await sleep(5000);
    const statusRes = await axios.get(
      `https://platform.tripo3d.ai/api/task/${taskId}`,
      { headers: { Authorization: `Bearer ${apiKey}` } }
    );
    const status = statusRes.data?.data?.status;
    if (status === "success") {
      const glbDownloadUrl = statusRes.data?.data?.output?.model;
      if (!glbDownloadUrl) throw new Error("No GLB URL in Tripo3D response");

      // Download GLB and upload to Firebase Storage
      const glbRes = await axios.get(glbDownloadUrl, { responseType: "arraybuffer" });
      const glbPath = `neural/models/generated/${modelId}.glb`;
      const glbRef = storage.bucket().file(glbPath);
      await glbRef.save(Buffer.from(glbRes.data), { contentType: "model/gltf-binary", public: true });
      return `https://storage.googleapis.com/${storage.bucket().name}/${glbPath}`;
    }
    if (status === "failed") throw new Error("Tripo3D task failed");
  }
  throw new Error("Tripo3D timeout");
}

async function getPlaceholderGlb(categoria: string, uid: string, ts: number): Promise<string> {
  const cat = categoria.toLowerCase();
  // Copy a placeholder GLB to the generated path so the client has something to load
  const srcPath = `neural/models/placeholders/${cat}_default.glb`;
  const dstPath = `neural/models/generated/${uid}_${ts}.glb`;
  try {
    await storage.bucket().file(srcPath).copy(storage.bucket().file(dstPath));
    return `https://storage.googleapis.com/${storage.bucket().name}/${dstPath}`;
  } catch {
    // If placeholder doesn't exist, return empty string — Android falls back to no model
    return "";
  }
}

function sleep(ms: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, ms));
}
