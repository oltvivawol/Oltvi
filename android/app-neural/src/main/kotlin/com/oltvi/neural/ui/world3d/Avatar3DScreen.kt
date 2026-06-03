package com.oltvi.neural.ui.world3d

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.oltvi.neural.data.ClaseRPG
import com.oltvi.neural.data.ObjetivoVida
import com.oltvi.neural.data.PerfilNeural
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNode
import kotlin.math.toDegrees

// Ruta en assets/ — reemplazar por el modelo rigged real cuando esté listo
private const val AVATAR_MODEL_PATH = "models/avatar_default.glb"

// ---------------------------------------------------------------------------
// Vista 3D en 3ra persona — SceneView/Filament, Compose-native
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

    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)

    // Nodo contenedor del avatar — siempre existe, el modelo es hijo opcional
    val avatarNode = rememberNode(engine)

    // Cargar el modelo .glb si existe en assets
    LaunchedEffect(avatarNode) {
        try {
            val instance = modelLoader.createModelInstance(AVATAR_MODEL_PATH)
            val modelNode = ModelNode(modelInstance = instance, scaleToUnits = 1.0f)
            avatarNode.addChildNode(modelNode)
        } catch (_: Exception) {
            // Modelo aún no disponible — la escena renderiza vacía
            // Los controles y la cámara funcionan igual para probar la lógica
        }
    }

    // Cámara 3ra persona: detrás y arriba del personaje
    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 2.5f, z = 5f)
        lookAt(avatarNode)
    }

    // Buffer para delta de tiempo (no-state para no disparar recomposición)
    val lastNanos = remember { LongArray(1) { System.nanoTime() } }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF080C18))) {

        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            childNodes = listOf(avatarNode),
            onFrame = {
                // Delta de tiempo
                val now = System.nanoTime()
                val dt = ((now - lastNanos[0]) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNanos[0] = now

                // Actualizar físicas y estado del personaje
                controller.update(dt)

                // Mover el nodo del avatar según el controlador
                avatarNode.position = Position(
                    x = controller.posX,
                    y = controller.alturaY,
                    z = controller.posZ
                )
                avatarNode.rotation = Rotation(
                    y = toDegrees(controller.rotacionY.toDouble()).toFloat()
                )

                // Cámara sigue al avatar desde atrás y arriba (offset fijo)
                cameraNode.position = Position(
                    x = controller.posX,
                    y = controller.alturaY + 2.5f,
                    z = controller.posZ + 5f
                )
                cameraNode.lookAt(avatarNode)
            }
        )

        // Overlay de controles: joystick + botones de acción
        GameControlsOverlay(
            controller = controller,
            accionActual = controller.accion,
            onBack = onBack
        )
    }
}
