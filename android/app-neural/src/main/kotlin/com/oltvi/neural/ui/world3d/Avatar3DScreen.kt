package com.oltvi.neural.ui.world3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import com.oltvi.neural.theme.EstacionDetector
import com.oltvi.neural.ui.world3d.minigames.CarreraEvento
import com.oltvi.neural.ui.world3d.minigames.CarreraHUD
import com.oltvi.neural.ui.world3d.minigames.CarreraState
import com.oltvi.neural.ui.world3d.minigames.RecoleccionHUD
import com.oltvi.neural.ui.world3d.minigames.RecoleccionState
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlin.math.toDegrees

private const val AVATAR_MODEL_PATH = "models/avatar_default.glb"

// ---------------------------------------------------------------------------
// Vista 3D en 3ra persona — SceneView/Filament, Compose-native
// World environment: NOA / La Esperanza with seasonal directional lighting
// ---------------------------------------------------------------------------

@Composable
fun Avatar3DScreen(
    perfil: PerfilNeural = PerfilNeural(
        id = "demo", nombre = "Jugador",
        clase = ClaseRPG.EXPLORADOR, objetivo = ObjetivoVida.TRABAJO
    ),
    onBack: () -> Unit
) {
    val controller = remember { CharacterController() }
    val carrera = remember { CarreraState() }
    val recoleccion = remember { RecoleccionState() }
    var xpCarrera by remember { mutableIntStateOf(0) }

    val estacion = remember { EstacionDetector.actual }

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val avatarNode = rememberNode(engine)

    // Seasonal directional sun light
    val sunLightNode = rememberSunLightNode(estacion)

    // World geometry anchors (positioned nodes — geometry loaded from assets when available)
    val envNodes = rememberEnvironmentNodes()

    LaunchedEffect(avatarNode) {
        try {
            val instance = modelLoader.createModelInstance(AVATAR_MODEL_PATH)
            val modelNode = ModelNode(modelInstance = instance, scaleToUnits = 1.0f)
            avatarNode.addChildNode(modelNode)
        } catch (_: Exception) {
            // Model not yet available — controls and lighting work regardless
        }
    }

    // Third-person camera: behind and above avatar
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 2.5f, z = 5f)
        lookAt(avatarNode)
    }

    val lastNanos = remember { LongArray(1) { System.nanoTime() } }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080C18))) {

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            // avatarNode + sunLight + environment anchor nodes
            childNodes = remember(avatarNode, sunLightNode, envNodes) {
                listOf(avatarNode, sunLightNode) + envNodes
            },
            onFrame = {
                val now = System.nanoTime()
                val dt = ((now - lastNanos[0]) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos[0] = now

                controller.update(dt)

                avatarNode.position = Position(
                    x = controller.posX,
                    y = controller.alturaY,
                    z = controller.posZ
                )
                avatarNode.rotation = Rotation(
                    y = toDegrees(controller.rotacionY.toDouble()).toFloat()
                )

                // Camera follows avatar (third-person offset)
                cameraNode.position = Position(
                    x = controller.posX,
                    y = controller.alturaY + 2.5f,
                    z = controller.posZ + 5f
                )
                cameraNode.lookAt(avatarNode)

                // ── Minigame frame updates ────────────────────────────────
                val eventoCarrera = carrera.update(dt, controller.posX, controller.posZ)
                if (eventoCarrera == CarreraEvento.COMPLETADA) xpCarrera = 200

                recoleccion.update(controller.posX, controller.posZ)
            }
        )

        // ── Race HUD ───────────────────────────────────────────────────────
        CarreraHUD(
            state = carrera,
            xpGanado = xpCarrera,
            modifier = Modifier.fillMaxSize()
        )

        // ── Credits HUD — top right ────────────────────────────────────────
        RecoleccionHUD(
            state = recoleccion,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(end = 16.dp, top = 56.dp)
        )

        // ── Controls overlay ───────────────────────────────────────────────
        GameControlsOverlay(
            controller = controller,
            accionActual = controller.accion,
            carreraActiva = carrera.activa,
            onBack = onBack,
            onToggleCarrera = {
                if (carrera.activa) carrera.cancelar() else {
                    xpCarrera = 0
                    carrera.iniciar()
                }
            }
        )
    }
}
