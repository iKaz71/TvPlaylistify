package com.kaz.tvplaylistify.util

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object TvHostManager {
    private const val TAG = "TvHostManager"

    fun marcarComoAnfitrion(tvId: String, userId: String, esAnfitrion: Boolean) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("tvHosts")
            .child(tvId)
            .child(userId)

        if (esAnfitrion) {
            ref.setValue(true).addOnSuccessListener {
                Log.d(TAG, "✅ $userId marcado como anfitrión persistente")
            }.addOnFailureListener {
                Log.e(TAG, "❌ Error al marcar anfitrión", it)
            }
        } else {
            ref.removeValue().addOnSuccessListener {
                Log.d(TAG, "🗑 $userId eliminado de anfitriones persistentes")
            }.addOnFailureListener {
                Log.e(TAG, "❌ Error al eliminar anfitrión", it)
            }
        }
    }

    fun obtenerAnfitriones(tvId: String, callback: (Set<String>) -> Unit) {
        FirebaseDatabase.getInstance()
            .getReference("tvHosts")
            .child(tvId)
            .get()
            .addOnSuccessListener { snapshot ->
                val anfitriones = snapshot.children.mapNotNull { it.key }.toSet()
                Log.d(TAG, "📥 Anfitriones leídos: $anfitriones")
                callback(anfitriones)
            }
            .addOnFailureListener {
                Log.e(TAG, "❌ Error al leer anfitriones persistentes", it)
                callback(emptySet())
            }
    }
}
