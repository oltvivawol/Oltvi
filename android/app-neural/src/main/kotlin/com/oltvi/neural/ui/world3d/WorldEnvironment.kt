package com.oltvi.neural.ui.world3d

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.google.android.filament.LightManager
import com.oltvi.neural.theme.EstacionVawol
import io.github.sceneview.math.Position
import io.github.sceneview.math.Scale
import io.github.sceneview.node.Node
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberNode

// ---------------------------------------------------------------------------
// VAWOL — WorldEnvironment
// Procedural NOA scene for Ingenio La Esperanza / San Pedro de Jujuy
// Scale: 1 scene unit = 1 metre (Filament standard)
// ---------------------------------------------------------------------------

// Seasonal directional light parameters (Southern hemisphere)
data class LuzEstacional(
    val r: Float, val g: Float, val b: Float,
    val intensidadLux: Float,       // Sun luminance in lux (~50 000 – 110 000)
    val dirX: Float, val dirY: Float, val dirZ: Float
)

object WorldEnvironment {

    // ── Lighting per season ────────────────────────────────────────────────

    fun luzPorEstacion(estacion: EstacionVawol): LuzEstacional = when (estacion) {
        EstacionVawol.VERANO    -> LuzEstacional(1.00f, 0.80f, 0.53f, 100_000f, -0.30f, -1.0f, -0.50f)
        EstacionVawol.OTONO     -> LuzEstacional(1.00f, 0.67f, 0.40f,  70_000f, -0.20f, -0.9f, -0.40f)
        EstacionVawol.INVIERNO  -> LuzEstacional(0.53f, 0.80f, 1.00f,  50_000f, -0.10f, -0.8f, -0.60f)
        EstacionVawol.PRIMAVERA -> LuzEstacional(1.00f, 0.93f, 0.67f,  85_000f, -0.30f, -1.0f, -0.40f)
    }

    // ── Scene layout — NOA / Jujuy territory ──────────────────────────────

    // Caña de azúcar: r=0.05 m, h=3.5 m — rows west of origin (plantation)
    val sugarcanePositions: List<Triple<Float, Float, Float>> = buildList {
        for (row in -6..6) for (col in -10..-3) {
            add(Triple(col * 1.1f, 0f, row * 0.9f))
        }
    }

    // Tipa / Ceibo trees: trunk h=12m crown r=3m;  Palmeras: h=8m crown r=2m
    data class Arbol(val x: Float, val z: Float, val altoTronco: Float, val radioCopa: Float)
    val arboles: List<Arbol> = listOf(
        Arbol(-20f, -15f, 12f, 3f),
        Arbol(-25f,   5f, 10f, 2.5f),
        Arbol( 18f, -20f,  8f, 2f),
        Arbol( 22f,  10f, 12f, 3f),
        Arbol(  0f, -35f, 10f, 2.5f),
        Arbol(-10f,  25f,  8f, 2f),
        Arbol( 30f,  -5f, 12f, 3f),
        Arbol(-30f, -10f, 10f, 2.5f),
        Arbol(  5f,  30f,  8f, 2f),
        Arbol(-15f,  35f, 12f, 3f)
    )

    // Background mountains at ~300 m — large scaled planes
    data class Montana(val x: Float, val z: Float, val escalaY: Float)
    val montanas: List<Montana> = listOf(
        Montana(-150f, -300f, 80f),
        Montana(   0f, -310f, 100f),
        Montana( 150f, -295f,  75f),
        Montana(-200f, -280f,  60f),
        Montana( 200f, -285f,  70f)
    )

    // Ground plane: 200 × 200 m, flat
    const val GROUND_HALF_EXTENT = 100f
}

// ---------------------------------------------------------------------------
// Compose helper — creates a directional light node for the current season
// ---------------------------------------------------------------------------

/**
 * Creates a SceneView Node whose Filament entity has a LightManager component
 * (DIRECTIONAL, colour and intensity driven by the current season).
 *
 * Add the returned node to the Scene composable's childNodes so Filament
 * picks up the light when it adds the entity to its internal scene.
 */
@Composable
fun rememberSunLightNode(
    estacion: EstacionVawol
): Node {
    val engine = rememberEngine()
    val lightNode = rememberNode(engine)

    LaunchedEffect(estacion) {
        val luz = WorldEnvironment.luzPorEstacion(estacion)
        // Attach a directional-light component to the node's existing entity
        LightManager.Builder(LightManager.Type.DIRECTIONAL)
            .color(luz.r, luz.g, luz.b)
            .intensity(luz.intensidadLux)
            .direction(luz.dirX, luz.dirY, luz.dirZ)
            .castShadows(true)
            .build(engine, lightNode.entity)
    }

    return lightNode
}

/**
 * Returns an empty Node at the correct world-space position for each element
 * in the scene layout. Actual geometry (cylinders for trunks, spheres for
 * crowns) is loaded as child ModelNodes once the .glb assets are available.
 *
 * Even without geometry, the positions are already correct — adding a child
 * ModelNode later will place it at the right world coordinates.
 */
@Composable
fun rememberEnvironmentNodes(): List<Node> {
    val engine = rememberEngine()

    // One anchor node per sugarcane stalk
    val sugarcaneNodes = remember {
        WorldEnvironment.sugarcanePositions.map { (x, y, z) ->
            Node(engine).apply {
                position = Position(x, y, z)
                scale = Scale(0.05f, 3.5f, 0.05f)    // r=0.05 m, h=3.5 m
            }
        }
    }

    // One anchor node per tree (trunk position + scale)
    val treeNodes = remember {
        WorldEnvironment.arboles.map { arbol ->
            Node(engine).apply {
                position = Position(arbol.x, 0f, arbol.z)
                scale = Scale(0.2f, arbol.altoTronco, 0.2f)
            }
        }
    }

    // Mountain anchors — far background
    val mountainNodes = remember {
        WorldEnvironment.montanas.map { m ->
            Node(engine).apply {
                position = Position(m.x, 0f, m.z)
                scale = Scale(50f, m.escalaY, 10f)
            }
        }
    }

    return remember(sugarcaneNodes, treeNodes, mountainNodes) {
        sugarcaneNodes + treeNodes + mountainNodes
    }
}
