package com.kaz.tvplaylistify.util

import android.util.Log
import com.google.firebase.database.FirebaseDatabase

object PersistentHostManager {

    fun guardarAnfitriones(sessionId: String, hosts: List<String>, onResult: (Boolean) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("hosts/default/$sessionId")
        ref.setValue(hosts)
            .addOnSuccessListener {
                Log.d("PersistentHostManager", "✅ Anfitriones guardados en Firebase")
                onResult(true)
            }
            .addOnFailureListener {
                Log.e("PersistentHostManager", "❌ Error al guardar anfitriones", it)
                onResult(false)
            }
    }

    fun obtenerAnfitriones(sessionId: String, onResult: (List<String>) -> Unit) {
        val ref = FirebaseDatabase.getInstance().getReference("hosts/default/$sessionId")
        ref.get()
            .addOnSuccessListener { snapshot ->
                val lista = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                onResult(lista)
            }
            .addOnFailureListener {
                Log.e("PersistentHostManager", "❌ Error al obtener anfitriones", it)
                onResult(emptyList())
            }
    }
}
