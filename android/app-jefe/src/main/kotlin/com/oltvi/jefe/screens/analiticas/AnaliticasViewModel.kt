package com.oltvi.jefe.screens.analiticas

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * ViewModel for the analytics screen.
 *
 * Provides chart-ready data sets for revenue and trip counts across four time
 * ranges. All values are static mock data; swap [cargarDatos] for a real
 * analytics repository when a backend is available.
 *
 * Colors for [tiposServicio] are packed as [Long] ARGB values (0xFFRRGGBB) so
 * the composable layer can convert them to [androidx.compose.ui.graphics.Color]
 * without depending on Compose types here in the ViewModel.
 */
@HiltViewModel
class AnaliticasViewModel @Inject constructor() : ViewModel() {

    enum class RangoTiempo { HOY, SEMANA, MES, TRIMESTRE }

    data class State(
        val rangoSeleccionado: RangoTiempo = RangoTiempo.SEMANA,
        /** (label, value ARS thousands) pairs ready for bar/line charts. */
        val revenueData: List<Pair<String, Float>> = emptyList(),
        /** (label, trip count) pairs ready for bar/line charts. */
        val viajesData: List<Pair<String, Float>> = emptyList(),
        /**
         * Service-type breakdown: (displayName, percentage, colorArgb).
         * Percentages are pre-computed and sum to 100.
         */
        val tiposServicio: List<Triple<String, Float, Long>> = emptyList(),
    )

    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state.asStateFlow()

    init {
        cargarDatos(RangoTiempo.SEMANA)
    }

    // ── Public surface ─────────────────────────────────────────────────────────

    fun seleccionarRango(rango: RangoTiempo) {
        _state.update { it.copy(rangoSeleccionado = rango) }
        cargarDatos(rango)
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private fun cargarDatos(rango: RangoTiempo) {
        val (revenue, viajes) = buildSeries(rango)
        _state.update {
            it.copy(
                revenueData = revenue,
                viajesData = viajes,
                tiposServicio = buildTiposServicio(),
            )
        }
    }

    private fun buildSeries(rango: RangoTiempo): Pair<List<Pair<String, Float>>, List<Pair<String, Float>>> =
        when (rango) {
            RangoTiempo.HOY -> Pair(
                listOf("0h", "4h", "8h", "12h", "16h", "20h", "23h")
                    .zip(listOf(0f, 2f, 15f, 42f, 38f, 65f, 28f)),
                listOf("0h", "4h", "8h", "12h", "16h", "20h", "23h")
                    .zip(listOf(0f, 1f, 8f, 21f, 19f, 32f, 14f))
            )

            RangoTiempo.SEMANA -> Pair(
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    .zip(listOf(38f, 42f, 45f, 41f, 58f, 72f, 51f)),
                listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
                    .zip(listOf(152f, 168f, 180f, 164f, 232f, 288f, 204f))
            )

            RangoTiempo.MES -> Pair(
                (1..30).map { "D$it" }
                    .zip(buildMonthSeries(seed = 42L, min = 30f, max = 80f)),
                (1..30).map { "D$it" }
                    .zip(buildMonthSeries(seed = 99L, min = 100f, max = 350f))
            )

            RangoTiempo.TRIMESTRE -> Pair(
                listOf("Ene", "Feb", "Mar").zip(listOf(980f, 1150f, 1280f)),
                listOf("Ene", "Feb", "Mar").zip(listOf(3920f, 4600f, 5120f))
            )
        }

    /**
     * Produces a deterministic 30-value series using a seeded LCG so the
     * monthly chart looks realistic but does not change on recomposition.
     */
    private fun buildMonthSeries(seed: Long, min: Float, max: Float): List<Float> {
        var s = seed
        return (1..30).map {
            // LCG parameters from Numerical Recipes
            s = (s * 1664525L + 1013904223L) and 0xFFFFFFFFL
            val t = (s and 0xFFFFFFFFL).toFloat() / 0xFFFFFFFFL.toFloat()
            min + (max - min) * t
        }
    }

    private fun buildTiposServicio(): List<Triple<String, Float, Long>> = listOf(
        Triple("Pasajero", 68f, 0xFFE67E22L),
        Triple("Mensajería", 18f, 0xFF27AE60L),
        Triple("Carga", 9f, 0xFFF1C40FL),
        Triple("Vial", 5f, 0xFFE74C3CL),
    )
}
