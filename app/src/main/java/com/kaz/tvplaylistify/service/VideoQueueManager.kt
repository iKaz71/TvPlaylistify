package com.kaz.tvplaylistify.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.firebase.database.*
import com.kaz.tvplaylistify.util.YouTubeLauncher

data class Video(
    val id: String,
    val durationMs: Long
)

object VideoQueueManager {

    private val videos = mutableListOf<Video>()
    private var currentIndex = 0
    private var context: Context? = null
    private val handler = Handler(Looper.getMainLooper())
    private var reproduciendo = false

    fun inicializar(ctx: Context, sessionId: String) {
        context = ctx
        Log.d("VideoQueueManager", "🚀 Inicializando escucha de cola en sesión: $sessionId")

        FirebaseDatabase.getInstance()
            .getReference("sessions")
            .child(sessionId)
            .child("queue")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    val videoId = snapshot.child("id").getValue(String::class.java)
                    val durationIso = snapshot.child("duration").getValue(String::class.java)
                    val durationMs = durationIso?.let { parseDurationToMillis(it) } ?: 0L

                    if (!videoId.isNullOrBlank() && durationMs > 0) {
                        val video = Video(videoId, durationMs)
                        videos.add(video)
                        Log.d("VideoQueueManager", "🎵 Video agregado: ${video.id} (${video.durationMs}ms)")

                        reproducirSiguiente()
                    } else {
                        Log.w("VideoQueueManager", "⚠️ Video inválido o duración no válida")
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildRemoved(snapshot: DataSnapshot) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("VideoQueueManager", "❌ Error al escuchar cola: ${error.message}")
                }
            })
    }

    private fun reproducirSiguiente() {
        val ctx = context ?: return

        if (videos.isEmpty()) {
            Log.d("VideoQueueManager", "🕐 No hay videos en la cola aún")
            return
        }

        if (currentIndex >= videos.size) {
            Log.d("VideoQueueManager", "🛑 Fin de la cola. Dejamos que YouTube continúe solo.")
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
        reproduciendo = true

        handler.postDelayed({
            currentIndex++
            reproduciendo = false
            reproducirSiguiente()
        }, video.durationMs)
    }

    private fun parseDurationToMillis(iso: String): Long {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L

        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0

        return ((h * 3600 + m * 60 + s) * 1000).toLong()
    }
}
