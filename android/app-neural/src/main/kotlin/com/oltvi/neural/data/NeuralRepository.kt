package com.oltvi.neural.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// Jugador en vivo (posición + avatar para el mapa social)
// ---------------------------------------------------------------------------

data class JugadorEnVivo(
    val uid: String,
    val nombre: String,
    val clase: ClaseRPG,
    val lat: Double,
    val lng: Double,
    val nivel: NivelNeural = NivelNeural.INICIADO,
    val avatarConfig: AvatarConfig = AvatarConfig()
)

// ---------------------------------------------------------------------------
// Repository — Firestore + Storage + Auth con fallback mock offline
// ---------------------------------------------------------------------------

@Singleton
class NeuralRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {

    private val isOnline: Boolean
        get() = try { firestore.firestoreSettings.isPersistenceEnabled; true } catch (_: Exception) { false }

    // ── Auth ────────────────────────────────────────────────────────────────

    suspend fun ensureAnonymousAuth(): String {
        val current = auth.currentUser
        if (current != null) return current.uid
        return try {
            val result = auth.signInAnonymously().await()
            result.user?.uid ?: "offline_${System.currentTimeMillis()}"
        } catch (_: Exception) {
            "offline_${System.currentTimeMillis()}"
        }
    }

    val currentUid: String? get() = auth.currentUser?.uid

    // ── Perfil ───────────────────────────────────────────────────────────────

    fun observeProfile(uid: String): Flow<PerfilNeural?> = callbackFlow {
        var reg: ListenerRegistration? = null
        try {
            reg = firestore.collection("neural_profiles")
                .document(uid)
                .addSnapshotListener { snap, err ->
                    if (err != null || snap == null || !snap.exists()) {
                        trySend(null)
                        return@addSnapshotListener
                    }
                    trySend(snap.toPerfilNeural())
                }
        } catch (_: Exception) {
            trySend(null)
        }
        awaitClose { reg?.remove() }
    }

    suspend fun saveProfile(perfil: PerfilNeural) {
        val uid = perfil.id.ifEmpty { ensureAnonymousAuth() }
        try {
            firestore.collection("neural_profiles")
                .document(uid)
                .set(perfil.toMap(), SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // offline — Firestore persiste localmente con persistencia habilitada
        }
    }

    // ── Catálogo de tienda ───────────────────────────────────────────────────

    fun loadCatalog(): Flow<List<PrendaRopa>> = flow {
        // Primero emitimos seed local para respuesta inmediata
        emit(WardrobeSeedData.catalogoInicial())
        try {
            val snap = firestore.collection("tienda_catalogo").get().await()
            if (!snap.isEmpty) {
                val prendas = snap.documents.mapNotNull { it.toPrendaRopa() }
                emit(prendas)
            }
        } catch (_: Exception) {
            // seed ya emitida, seguimos con eso
        }
    }

    // ── Compras / inventario ─────────────────────────────────────────────────

    suspend fun comprarPrenda(uid: String, prenda: PrendaRopa, saldoActual: Int): Result<Int> {
        if (saldoActual < prenda.precio) return Result.failure(Exception("Sin Neurocréditos suficientes"))
        val nuevoSaldo = saldoActual - prenda.precio
        return try {
            firestore.runTransaction { tx ->
                val ref = firestore.collection("neural_profiles").document(uid)
                val snap = tx.get(ref)
                val inventario = (snap.get("inventario") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                if (prenda.id in inventario) throw Exception("Ya tenés esta prenda")
                tx.update(ref, mapOf(
                    "monedas" to nuevoSaldo,
                    "inventario" to inventario + prenda.id
                ))
            }.await()
            Result.success(nuevoSaldo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun equipar(uid: String, equipamiento: EquipamientoAvatar) {
        try {
            firestore.collection("neural_profiles").document(uid)
                .update("equipamiento", equipamiento.toMap())
                .await()
        } catch (_: Exception) {}
    }

    // ── Jugadores en vivo (mapa social) ─────────────────────────────────────

    fun observeLivePlayers(lat: Double, lng: Double, radiusKm: Double = 5.0): Flow<List<JugadorEnVivo>> =
        callbackFlow {
            var reg: ListenerRegistration? = null
            try {
                reg = firestore.collection("neural_players_live")
                    .addSnapshotListener { snap, _ ->
                        val players = snap?.documents?.mapNotNull { it.toJugadorEnVivo() } ?: emptyList()
                        trySend(players.filter { player ->
                            distanciaKm(lat, lng, player.lat, player.lng) <= radiusKm
                        })
                    }
            } catch (_: Exception) {
                trySend(emptyList())
            }
            awaitClose { reg?.remove() }
        }

    suspend fun updateLivePosition(uid: String, nombre: String, clase: ClaseRPG, nivel: NivelNeural,
                                   lat: Double, lng: Double) {
        try {
            firestore.collection("neural_players_live").document(uid)
                .set(mapOf(
                    "uid" to uid, "nombre" to nombre, "clase" to clase.name,
                    "lat" to lat, "lng" to lng, "nivel" to nivel.name,
                    "ts" to com.google.firebase.Timestamp.now()
                ), SetOptions.merge())
                .await()
        } catch (_: Exception) {}
    }

    // ── Storage URL para assets 3D ───────────────────────────────────────────

    suspend fun getAssetUrl(path: String): String? = try {
        storage.reference.child(path).downloadUrl.await().toString()
    } catch (_: Exception) { null }

    // ── Helpers Firestore ↔ Kotlin ───────────────────────────────────────────

    private fun distanciaKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLng / 2).let { it * it }
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }
}

// ── Extensiones de serialización ────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toPerfilNeural(): PerfilNeural? = try {
    val id = this.id
    val nombre = getString("nombre") ?: return null
    val claseStr = getString("clase") ?: return null
    val clase = runCatching { ClaseRPG.valueOf(claseStr) }.getOrNull() ?: return null
    val objetivoStr = getString("objetivo") ?: return null
    val objetivo = runCatching { ObjetivoVida.valueOf(objetivoStr) }.getOrNull() ?: return null
    val nivelStr = getString("nivel") ?: NivelNeural.INICIADO.name
    val nivel = runCatching { NivelNeural.valueOf(nivelStr) }.getOrDefault(NivelNeural.INICIADO)
    val xp = (getLong("xpActual") ?: 0L).toInt()
    val rep = (getLong("reputacion") ?: 100L).toInt()
    val misiones = (getLong("misionesCompletadas") ?: 0L).toInt()
    val monedas = (getLong("monedas") ?: 500L).toInt()
    val inventario = (get("inventario") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    val equipMap = (get("equipamiento") as? Map<*, *>)
    val equipamiento = if (equipMap != null) EquipamientoAvatar(
        cabeza = equipMap["cabeza"] as? String,
        cara = equipMap["cara"] as? String,
        torso = equipMap["torso"] as? String,
        piernas = equipMap["piernas"] as? String,
        pies = equipMap["pies"] as? String,
        accesorio = equipMap["accesorio"] as? String
    ) else EquipamientoAvatar()
    PerfilNeural(
        id = id, nombre = nombre, clase = clase, objetivo = objetivo, nivel = nivel,
        xpActual = xp, reputacion = rep, misionesCompletadas = misiones,
        monedas = monedas, inventario = inventario, equipamiento = equipamiento
    )
} catch (_: Exception) { null }

private fun PerfilNeural.toMap(): Map<String, Any?> = mapOf(
    "nombre" to nombre, "clase" to clase.name, "objetivo" to objetivo.name,
    "nivel" to nivel.name, "xpActual" to xpActual, "reputacion" to reputacion,
    "misionesCompletadas" to misionesCompletadas, "monedas" to monedas,
    "inventario" to inventario, "equipamiento" to equipamiento.toMap()
)

private fun EquipamientoAvatar.toMap(): Map<String, Any?> = mapOf(
    "cabeza" to cabeza, "cara" to cara, "torso" to torso,
    "piernas" to piernas, "pies" to pies, "accesorio" to accesorio
)

private fun com.google.firebase.firestore.DocumentSnapshot.toPrendaRopa(): PrendaRopa? = try {
    val id = this.id
    val nombre = getString("nombre") ?: return null
    val categoriaStr = getString("categoria") ?: return null
    val categoria = runCatching { CategoriaPrenda.valueOf(categoriaStr) }.getOrNull() ?: return null
    val rarezaStr = getString("rareza") ?: Rareza.COMUN.name
    val rareza = runCatching { Rareza.valueOf(rarezaStr) }.getOrDefault(Rareza.COMUN)
    val precio = (getLong("precioBase") ?: 100L).toInt()
    val colorHex = getString("colorHex") ?: "#FFFFFF"
    val descripcion = getString("descripcion") ?: ""
    val assetRef = getString("assetRef")
    val marcaRef = getString("marcaRef")
    PrendaRopa(id, nombre, descripcion, categoria, rareza, precio, colorHex,
        assetRef = assetRef, marcaRef = marcaRef)
} catch (_: Exception) { null }

private fun com.google.firebase.firestore.DocumentSnapshot.toJugadorEnVivo(): JugadorEnVivo? = try {
    val uid = getString("uid") ?: return null
    val nombre = getString("nombre") ?: "Jugador"
    val claseStr = getString("clase") ?: return null
    val clase = runCatching { ClaseRPG.valueOf(claseStr) }.getOrNull() ?: return null
    val lat = getDouble("lat") ?: return null
    val lng = getDouble("lng") ?: return null
    val nivelStr = getString("nivel") ?: NivelNeural.INICIADO.name
    val nivel = runCatching { NivelNeural.valueOf(nivelStr) }.getOrDefault(NivelNeural.INICIADO)
    JugadorEnVivo(uid, nombre, clase, lat, lng, nivel)
} catch (_: Exception) { null }
