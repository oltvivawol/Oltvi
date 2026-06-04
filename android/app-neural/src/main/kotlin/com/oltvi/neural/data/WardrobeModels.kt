package com.oltvi.neural.data

// ---------------------------------------------------------------------------
// Guardarropa — categorías, rareza, prendas
// ---------------------------------------------------------------------------

enum class CategoriaPrenda(val displayName: String, val icon: String) {
    CABEZA("Cabeza", "🎩"),
    CARA("Cara", "👓"),
    TORSO("Torso", "👕"),
    PIERNAS("Piernas", "👖"),
    PIES("Pies", "👟"),
    ACCESORIO("Accesorio", "📿")
}

enum class Rareza(val displayName: String, val colorHex: String, val multiplicadorPrecio: Float) {
    COMUN("Común", "#9E9E9E", 1.0f),
    RARA("Rara", "#2196F3", 2.5f),
    EPICA("Épica", "#9C27B0", 6.0f),
    LEGENDARIA("Legendaria", "#FFB800", 15.0f)
}

data class PrendaRopa(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val categoria: CategoriaPrenda,
    val rareza: Rareza,
    val precioBase: Int,
    val colorHex: String = "#FFFFFF",
    val colorSecundarioHex: String? = null,
    val assetRef: String? = null,        // ruta al .glb o textura en Storage
    val marcaRef: String? = null,        // null = prenda original / futuro: id de marca licenciada
    val disponible: Boolean = true,
    val generadoIA: Boolean = false,     // true si fue creado por Vertex AI Studio
    val promptOriginal: String? = null   // prompt que generó el modelo (para re-generación)
) {
    val precio: Int get() = (precioBase * rareza.multiplicadorPrecio).toInt()
}

data class EquipamientoAvatar(
    val cabeza: String? = null,
    val cara: String? = null,
    val torso: String? = null,
    val piernas: String? = null,
    val pies: String? = null,
    val accesorio: String? = null
) {
    fun equipar(prendaId: String, categoria: CategoriaPrenda): EquipamientoAvatar = when (categoria) {
        CategoriaPrenda.CABEZA -> copy(cabeza = prendaId)
        CategoriaPrenda.CARA -> copy(cara = prendaId)
        CategoriaPrenda.TORSO -> copy(torso = prendaId)
        CategoriaPrenda.PIERNAS -> copy(piernas = prendaId)
        CategoriaPrenda.PIES -> copy(pies = prendaId)
        CategoriaPrenda.ACCESORIO -> copy(accesorio = prendaId)
    }

    fun desequipar(categoria: CategoriaPrenda): EquipamientoAvatar = equipar("", categoria).let {
        when (categoria) {
            CategoriaPrenda.CABEZA -> copy(cabeza = null)
            CategoriaPrenda.CARA -> copy(cara = null)
            CategoriaPrenda.TORSO -> copy(torso = null)
            CategoriaPrenda.PIERNAS -> copy(piernas = null)
            CategoriaPrenda.PIES -> copy(pies = null)
            CategoriaPrenda.ACCESORIO -> copy(accesorio = null)
        }
    }

    fun prendaEn(categoria: CategoriaPrenda): String? = when (categoria) {
        CategoriaPrenda.CABEZA -> cabeza
        CategoriaPrenda.CARA -> cara
        CategoriaPrenda.TORSO -> torso
        CategoriaPrenda.PIERNAS -> piernas
        CategoriaPrenda.PIES -> pies
        CategoriaPrenda.ACCESORIO -> accesorio
    }
}

// ---------------------------------------------------------------------------
// Seed — catálogo original (sin marcas registradas)
// ---------------------------------------------------------------------------

object WardrobeSeedData {

    fun catalogoInicial(): List<PrendaRopa> = listOf(
        // ── TORSO ──────────────────────────────────────────────────────────
        PrendaRopa("t_basic_white", "Remera Básica Blanca", "Clásico atemporal.", CategoriaPrenda.TORSO, Rareza.COMUN, 80, "#FFFFFF"),
        PrendaRopa("t_basic_black", "Remera Básica Negra", "Nunca falla.", CategoriaPrenda.TORSO, Rareza.COMUN, 80, "#1A1A1A"),
        PrendaRopa("t_neural_tee", "Remera Neural", "Con el logo de la red.", CategoriaPrenda.TORSO, Rareza.RARA, 200, "#7B2FBE", "#00D4FF"),
        PrendaRopa("t_hoodie_gray", "Hoodie Gris Urbano", "Para los días fríos de la ciudad.", CategoriaPrenda.TORSO, Rareza.RARA, 350, "#616161"),
        PrendaRopa("t_hoodie_electric", "Hoodie Eléctrico", "Brilla en la noche.", CategoriaPrenda.TORSO, Rareza.EPICA, 900, "#00D4FF", "#7B2FBE"),
        PrendaRopa("t_jacket_gold", "Campera Dorada", "Solo para los líderes.", CategoriaPrenda.TORSO, Rareza.LEGENDARIA, 500, "#FFB800", "#FF6B35"),
        PrendaRopa("t_suit_dark", "Saco Oscuro", "Presencia en cada reunión.", CategoriaPrenda.TORSO, Rareza.EPICA, 750, "#212121"),
        PrendaRopa("t_jersey_blue", "Camiseta Azul Eléctrica", "Energía pura.", CategoriaPrenda.TORSO, Rareza.RARA, 280, "#1565C0"),

        // ── PIERNAS ────────────────────────────────────────────────────────
        PrendaRopa("p_jeans_blue", "Jeans Azul Clásico", "El pantalón de siempre.", CategoriaPrenda.PIERNAS, Rareza.COMUN, 100, "#1565C0"),
        PrendaRopa("p_jeans_black", "Jeans Negro", "Modo nocturno.", CategoriaPrenda.PIERNAS, Rareza.COMUN, 100, "#0D0D0D"),
        PrendaRopa("p_cargo_green", "Cargo Verde Urbano", "Bolsillos para todo.", CategoriaPrenda.PIERNAS, Rareza.RARA, 320, "#388E3C"),
        PrendaRopa("p_shorts_white", "Short Blanco Sport", "Para los días de acción.", CategoriaPrenda.PIERNAS, Rareza.COMUN, 90, "#F5F5F5"),
        PrendaRopa("p_jogger_neural", "Jogger Neural", "Rayitas eléctricas en el costado.", CategoriaPrenda.PIERNAS, Rareza.EPICA, 650, "#1A237E", "#00D4FF"),

        // ── PIES ───────────────────────────────────────────────────────────
        PrendaRopa("f_sneaker_white", "Zapatillas Blancas", "Básico de la calle.", CategoriaPrenda.PIES, Rareza.COMUN, 150, "#FFFFFF"),
        PrendaRopa("f_sneaker_black", "Zapatillas Negras", "Siempre limpias.", CategoriaPrenda.PIES, Rareza.COMUN, 150, "#111111"),
        PrendaRopa("f_boots_brown", "Botas Urbanas", "Para pisar fuerte en el barrio.", CategoriaPrenda.PIES, Rareza.RARA, 400, "#5D4037"),
        PrendaRopa("f_sneaker_electric", "Zapatillas Eléctricas", "Con suela luminosa.", CategoriaPrenda.PIES, Rareza.EPICA, 800, "#00D4FF", "#FFFFFF"),
        PrendaRopa("f_boots_legendary", "Botas Legendarias", "Cada paso deja huella.", CategoriaPrenda.PIES, Rareza.LEGENDARIA, 600, "#FFB800", "#8B5E3C"),

        // ── CABEZA ─────────────────────────────────────────────────────────
        PrendaRopa("h_cap_black", "Gorra Negra", "Clásica de visera recta.", CategoriaPrenda.CABEZA, Rareza.COMUN, 120, "#111111"),
        PrendaRopa("h_cap_neural", "Gorra Neural", "El símbolo de la red en la frente.", CategoriaPrenda.CABEZA, Rareza.RARA, 300, "#7B2FBE"),
        PrendaRopa("h_beanie_gray", "Gorro de Lana Gris", "Para el invierno porteño.", CategoriaPrenda.CABEZA, Rareza.COMUN, 100, "#757575"),
        PrendaRopa("h_crown_gold", "Corona Dorada", "Para el que llegó a lo más alto.", CategoriaPrenda.CABEZA, Rareza.LEGENDARIA, 800, "#FFB800"),
        PrendaRopa("h_hood_purple", "Capucha Violeta", "El color de la red.", CategoriaPrenda.CABEZA, Rareza.EPICA, 550, "#7B2FBE"),

        // ── CARA ───────────────────────────────────────────────────────────
        PrendaRopa("c_glasses_round", "Anteojos Redondos", "Look intelectual.", CategoriaPrenda.CARA, Rareza.COMUN, 90, "#212121"),
        PrendaRopa("c_sunglasses_cool", "Lentes de Sol", "Actitud de verano.", CategoriaPrenda.CARA, Rareza.RARA, 250, "#1A1A1A"),
        PrendaRopa("c_glasses_electric", "Gafas Eléctricas", "Con lentes de color cian.", CategoriaPrenda.CARA, Rareza.EPICA, 500, "#00D4FF"),

        // ── ACCESORIO ─────────────────────────────────────────────────────
        PrendaRopa("a_watch_silver", "Reloj Plateado", "Elegante y puntual.", CategoriaPrenda.ACCESORIO, Rareza.RARA, 280, "#C0C0C0"),
        PrendaRopa("a_chain_gold", "Cadena Dorada", "Brilla en el barrio.", CategoriaPrenda.ACCESORIO, Rareza.EPICA, 700, "#FFB800"),
        PrendaRopa("a_backpack_neural", "Mochila Neural", "Lleva todo sin perder el estilo.", CategoriaPrenda.ACCESORIO, Rareza.RARA, 450, "#7B2FBE", "#00D4FF")
    )
}
