package com.oltvi.neural.ui.world3d

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Estado de movimiento del personaje
// ---------------------------------------------------------------------------

enum class AccionPersonaje {
    IDLE, WALK, RUN, JUMP, CROUCH, GRAB, PUNCH
}

data class InputJoystick(
    val x: Float = 0f,  // -1..1 (izquierda / derecha)
    val y: Float = 0f   // -1..1 (abajo / arriba)
) {
    val magnitude: Float get() = sqrt(x * x + y * y).coerceIn(0f, 1f)
    val angle: Float get() = atan2(x, y)   // radianes, para rotar el personaje
    val isMoving: Boolean get() = magnitude > 0.1f
}

// ---------------------------------------------------------------------------
// Controlador — máquina de estados
// ---------------------------------------------------------------------------

class CharacterController {

    var accion by mutableStateOf(AccionPersonaje.IDLE)
        private set

    var joystick by mutableStateOf(InputJoystick())
        private set

    // Posición del personaje en el mundo (unidades de escena)
    var posX by mutableFloatStateOf(0f)
        private set
    var posZ by mutableFloatStateOf(0f)
        private set

    // Rotación Y del personaje (radianes)
    var rotacionY by mutableFloatStateOf(0f)
        private set

    // Velocidades
    private val velocidadCaminata = 2.0f    // unidades/seg
    private val velocidadCorrer = 4.5f      // unidades/seg

    // Estado salto
    private var saltando = false
    private var velY = 0f
    private val gravedad = -15f
    private val fuerzaSalto = 7f
    var alturaY by mutableFloatStateOf(0f)
        private set

    // ---------------------------------------------------------------------------

    fun onJoystickMove(x: Float, y: Float) {
        joystick = InputJoystick(x.coerceIn(-1f, 1f), y.coerceIn(-1f, 1f))
        if (joystick.isMoving && accion == AccionPersonaje.IDLE) {
            accion = if (joystick.magnitude > 0.7f) AccionPersonaje.RUN else AccionPersonaje.WALK
        } else if (!joystick.isMoving && (accion == AccionPersonaje.WALK || accion == AccionPersonaje.RUN)) {
            accion = AccionPersonaje.IDLE
        }
    }

    fun onSaltar() {
        if (!saltando && accion != AccionPersonaje.JUMP && accion != AccionPersonaje.CROUCH) {
            saltando = true
            velY = fuerzaSalto
            accion = AccionPersonaje.JUMP
        }
    }

    fun onAgacharse() {
        accion = if (accion == AccionPersonaje.CROUCH) {
            if (joystick.isMoving) AccionPersonaje.WALK else AccionPersonaje.IDLE
        } else {
            AccionPersonaje.CROUCH
        }
    }

    fun onAgarrar() {
        if (accion != AccionPersonaje.JUMP) {
            accion = AccionPersonaje.GRAB
        }
    }

    fun onGolpear() {
        if (accion != AccionPersonaje.JUMP) {
            accion = AccionPersonaje.PUNCH
        }
    }

    // Llamado cada frame con el delta de tiempo en segundos
    fun update(deltaTime: Float) {
        if (!joystick.isMoving && accion == AccionPersonaje.IDLE && !saltando) return

        // Actualizar posición según joystick
        if (joystick.isMoving && accion != AccionPersonaje.CROUCH) {
            val velocidad = when (accion) {
                AccionPersonaje.RUN -> velocidadCorrer
                AccionPersonaje.WALK -> velocidadCaminata
                AccionPersonaje.JUMP -> velocidadCaminata * 0.7f
                else -> 0f
            }
            posX += joystick.x * velocidad * deltaTime
            posZ -= joystick.y * velocidad * deltaTime
            rotacionY = joystick.angle
        }

        // Física del salto
        if (saltando || alturaY > 0f) {
            velY += gravedad * deltaTime
            alturaY = (alturaY + velY * deltaTime).coerceAtLeast(0f)
            if (alturaY == 0f) {
                saltando = false
                velY = 0f
                accion = if (joystick.isMoving) {
                    if (joystick.magnitude > 0.7f) AccionPersonaje.RUN else AccionPersonaje.WALK
                } else AccionPersonaje.IDLE
            }
        }

        // Volver a idle tras acciones de un solo frame
        if (accion == AccionPersonaje.GRAB || accion == AccionPersonaje.PUNCH) {
            // La animación se marca como "terminada" externamente; aquí solo devolvemos idle
            // si no hay movimiento activo
            if (!joystick.isMoving) accion = AccionPersonaje.IDLE
        }
    }

    fun finalizarAccion() {
        if (accion == AccionPersonaje.GRAB || accion == AccionPersonaje.PUNCH) {
            accion = if (joystick.isMoving) AccionPersonaje.WALK else AccionPersonaje.IDLE
        }
    }
}
