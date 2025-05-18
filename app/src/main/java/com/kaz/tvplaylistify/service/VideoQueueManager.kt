package com.kaz.tvplaylistify.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import com.kaz.tvplaylistify.util.YouTubeLauncher

data class Video(val id: String, val durationMs: Long)

object VideoQueueManager {

    private val videos = mutableListOf<Video>()
    private val keys = mutableListOf<String>()
    private var currentIndex = 0
    private var context: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var reproduciendo = false
    private var sessionId: String? = null

    fun inicializar(ctx: Context, codigo: String, sessionId: String) {
        context = ctx
        this.sessionId = sessionId

        Log.d("VideoQueueManager", "🚀 Inicializando cola completa en sesión: $sessionId")

        val queueRef = FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)

        // Leer cola completa inicialmente
        queueRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("VideoQueueManager", "📋 Lectura inicial de la cola")
                snapshot.children.forEach { child ->
                    agregarVideoDesdeSnapshot(child)
                }
                reproducirSiguiente()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("VideoQueueManager", "❌ Error al leer cola inicial", error.toException())
            }
        })

        // Escuchar nuevos elementos agregados
        queueRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("VideoQueueManager", "➕ Nuevo video detectado: ${snapshot.key}")
                if (agregarVideoDesdeSnapshot(snapshot)) {
                    reproducirSiguiente()
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {
                Log.e("VideoQueueManager", "❌ Error al escuchar nuevos videos", error.toException())
            }
        })
    }

    private fun agregarVideoDesdeSnapshot(snapshot: DataSnapshot): Boolean {
        val videoId = snapshot.child("id").getValue(String::class.java)
        val durationIso = snapshot.child("duration").getValue(String::class.java)
        val durationMs = durationIso?.let { parseDurationToMillis(it) } ?: 0L

        if (!videoId.isNullOrBlank() && durationMs > 0) {
            videos.add(Video(videoId, durationMs))
            keys.add(snapshot.key ?: "")
            Log.d("VideoQueueManager", "🎵 Video agregado a la lista: $videoId (${durationMs}ms)")
            return true
        } else {
            Log.w("VideoQueueManager", "⚠️ Video inválido: $videoId con duración: $durationIso")
            return false
        }
    }

    fun reproducirSiguiente() {
        val ctx = context ?: return

        if (videos.isEmpty()) {
            Log.d("VideoQueueManager", "🕐 No hay videos en la lista interna")
            return
        }

        if (currentIndex >= videos.size) {
            Log.d("VideoQueueManager", "🛑 Fin de la lista. YouTube continuará solo.")
            reproduciendo = false
            return
        }

        if (reproduciendo) {
            Log.d("VideoQueueManager", "⏳ Ya hay un video en reproducción")
            return
        }

        val video = videos[currentIndex]
        Log.d("VideoQueueManager", "▶ Reproduciendo video ${currentIndex + 1}/${videos.size}: ${video.id}")
        YouTubeLauncher.launchYoutube(ctx, video.id)
        actualizarPlaybackState(video)
        reproduciendo = true

        handler.postDelayed({
            eliminarVideoActualDeFirebase()
            currentIndex++
            reproduciendo = false
            reproducirSiguiente()
        }, video.durationMs)
    }

    private fun actualizarPlaybackState(video: Video) {
        val sessionId = sessionId ?: return
        val playbackRef = FirebaseDatabase.getInstance().getReference("playbackState").child(sessionId)

        val update = mapOf(
            "playing" to true,
            "currentVideo/id" to video.id,
            "currentVideo/duration" to formatDurationToIso(video.durationMs)
        )

        playbackRef.updateChildren(update)
            .addOnSuccessListener { Log.d("VideoQueueManager", "📡 playbackState actualizado con ${video.id}") }
            .addOnFailureListener { Log.e("VideoQueueManager", "❌ Error actualizando playbackState", it) }
    }

    private fun eliminarVideoActualDeFirebase() {
        val sessionId = sessionId ?: return
        if (currentIndex >= keys.size) return
        val key = keys[currentIndex]

        FirebaseDatabase.getInstance()
            .getReference("queues")
            .child(sessionId)
            .child(key)
            .removeValue()
            .addOnSuccessListener {
                Log.d("VideoQueueManager", "🧹 Video eliminado de Firebase: $key")
            }
            .addOnFailureListener {
                Log.e("VideoQueueManager", "❌ Error al eliminar video de Firebase", it)
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
