package com.kaz.tvplaylistify.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import com.kaz.tvplaylistify.model.Cancion
import com.kaz.tvplaylistify.util.YouTubeLauncher

object VideoQueueManager {

    private var context: Context? = null
    private var sessionId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var reproduciendo = false

    fun inicializar(ctx: Context, codigo: String, sessionId: String) {
        context = ctx
        this.sessionId = sessionId

        Log.d("VideoQueueManager", "🚀 Inicializando cola completa en sesión: $sessionId")

        val queueRef = FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)

        queueRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("VideoQueueManager", "📋 Lectura inicial de la cola")
                reproducirSiEsNecesario()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("VideoQueueManager", "❌ Error al leer cola inicial", error.toException())
            }
        })

        queueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("VideoQueueManager", "➕ Nuevo video detectado: ${snapshot.key}")
                reproducirSiEsNecesario()
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("VideoQueueManager", "❌ Error al escuchar nuevos videos", error.toException())
            }
        })
    }

    fun reproducirSiEsNecesario() {
        val ctx = context ?: return
        val sid = sessionId ?: return

        if (reproduciendo) {
            Log.d("VideoQueueManager", "⏳ Aún hay video en reproducción, no se hará nada.")
            return
        }

        val queueRef = FirebaseDatabase.getInstance().getReference("queues").child(sid)

        queueRef.get().addOnSuccessListener { snapshot ->
            val primerNodo = snapshot.children.firstOrNull() ?: run {
                Log.d("VideoQueueManager", "⛔ Cola vacía, no hay videos para reproducir")
                return@addOnSuccessListener
            }

            val id = primerNodo.child("id").getValue(String::class.java)
            val titulo = primerNodo.child("titulo").getValue(String::class.java) ?: ""
            val usuario = primerNodo.child("usuario").getValue(String::class.java) ?: ""
            val thumbnailUrl = primerNodo.child("thumbnailUrl").getValue(String::class.java) ?: ""
            val durationIso = primerNodo.child("duration").getValue(String::class.java)
            val durationMs = durationIso?.let { parseDurationToMillis(it) } ?: 0L
            val key = primerNodo.key ?: return@addOnSuccessListener

            if (id.isNullOrBlank() || durationMs <= 0) {
                Log.w("VideoQueueManager", "⚠️ Video inválido detectado. Ignorando.")
                return@addOnSuccessListener
            }

            val cancion = Cancion(id, titulo, usuario, thumbnailUrl)

            // 🧹 Eliminar inmediatamente el video para evitar repetirlo
            queueRef.child(key).removeValue()
                .addOnSuccessListener {
                    Log.d("VideoQueueManager", "🧹 Video eliminado de Firebase al iniciar: $key")
                }
                .addOnFailureListener {
                    Log.e("VideoQueueManager", "❌ Error al eliminar video antes de reproducir", it)
                }

            Log.d("VideoQueueManager", "▶ Reproduciendo video: ${cancion.id}")
            YouTubeLauncher.launchYoutube(ctx, cancion.id)
            actualizarPlaybackState(cancion, durationMs)
            reproduciendo = true

            handler.postDelayed({
                reproduciendo = false
                reproducirSiEsNecesario()
            }, durationMs + 1000L)
        }.addOnFailureListener {
            Log.e("VideoQueueManager", "❌ Error al obtener la cola actual desde Firebase", it)
        }
    }

    private fun actualizarPlaybackState(cancion: Cancion, durationMs: Long) {
        val sessionId = sessionId ?: return
        val playbackRef = FirebaseDatabase.getInstance().getReference("playbackState").child(sessionId)

        val update = mapOf(
            "playing" to true,
            "currentVideo/id" to cancion.id,
            "currentVideo/titulo" to cancion.titulo,
            "currentVideo/thumbnailUrl" to cancion.thumbnailUrl,
            "currentVideo/usuario" to cancion.usuario,
            "currentVideo/duration" to formatDurationToIso(durationMs)
        )

        playbackRef.updateChildren(update)
            .addOnSuccessListener {
                Log.d("VideoQueueManager", "📱 playbackState actualizado con ${cancion.id}")
            }
            .addOnFailureListener {
                Log.e("VideoQueueManager", "❌ Error actualizando playbackState", it)
            }
    }

    private fun parseDurationToMillis(iso: String): Long {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L
        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0
        return ((h * 3600 + m * 60 + s) * 1000).toLong()
    }

    private fun formatDurationToIso(ms: Long): String {
        val totalSeconds = ms / 1000
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return buildString {
            append("PT")
            if (h > 0) append("${h}H")
            if (m > 0) append("${m}M")
            if (s > 0 || (h == 0L && m == 0L)) append("${s}S")
        }
    }
}
