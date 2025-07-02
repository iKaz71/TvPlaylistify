package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kaz.tvplaylistify.NextVideoActivity

object VideoSender {

    private val TAG = "VideoSender"

    // Lista de reproducción con duración reducida para pruebas (10 segundos c/u)
    private val playlist = listOf(
        Pair("dQw4w9WgXcQ", 10_000),  // Rick Astley
        Pair("hTWKbfoikeg", 10_000), // Nirvana
        Pair("Ckom3gf57Yw", 10_000)  // Muse
    )

    private var currentIndex = 0
    private var handler: Handler? = null

    fun reproducirLista(context: Context) {
        Log.d(TAG, "▶️ Iniciando lista de reproducción completa")
        currentIndex = 0
        handler = Handler(Looper.getMainLooper())
        reproducirSiguiente(context)
    }

    fun reproducirSiguiente(context: Context) {
        if (currentIndex >= playlist.size) {
            Log.d(TAG, "✅ Fin de la lista de reproducción")
            return
        }

        val (videoId, duracion) = playlist[currentIndex]
        Log.d(TAG, "⏭ Preparando video: $videoId (${duracion / 1000}s)")

        val nextIntent = Intent(context, NextVideoActivity::class.java).apply {
            putExtra("VIDEO_ID", videoId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        Log.d(TAG, "🎬 Lanzando NextVideoActivity con videoId=$videoId")
        context.startActivity(nextIntent)

        // Esperamos la duración y avanzamos al siguiente
        handler?.postDelayed({
            currentIndex++
            Log.d(TAG, "⏭ Avanzando al siguiente video (índice = $currentIndex)")
            reproducirSiguiente(context)
        }, duracion.toLong())
    }

    fun detener() {
        handler?.removeCallbacksAndMessages(null)
        handler = null
        Log.d(TAG, "🛑 Reproducción detenida")
    }
}
