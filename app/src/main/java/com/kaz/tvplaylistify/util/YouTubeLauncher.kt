package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.kaz.tvplaylistify.model.Cancion
import com.kaz.tvplaylistify.service.VideoPlaybackService
import android.os.Handler
import android.os.Looper


object YouTubeLauncher {

    // Interno, no se accede directamente
    private var _currentIndex = 0

    // Accesible desde fuera con getter/setter seguros
    var currentIndex: Int
        get() = _currentIndex
        set(value) {
            _currentIndex = value
        }

    private val playlist = listOf(
        Cancion(
            id = "dQw4w9WgXcQ",
            titulo = "Rick Astley - Never Gonna Give You Up",
            usuario = "anfitrión",
            thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg"
        ),
        Cancion(
            id = "hTWKbfoikeg",
            titulo = "Nirvana - Smells Like Teen Spirit",
            usuario = "anfitrión",
            thumbnailUrl = "https://img.youtube.com/vi/hTWKbfoikeg/0.jpg"
        )
    )

    fun reproducirVideo(context: Context, videoId: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.youtube.com/watch?v=$videoId")
            setPackage("com.google.android.youtube.tv")
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
            )
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("YouTubeLauncher", "❌ Error al lanzar YouTube: ${e.message}", e)
        }
    }



    fun iniciarReproduccion(context: Context) {
        if (playlist.isNotEmpty()) {
            reproducirVideo(context, playlist[currentIndex].id)
        }

        val intent = Intent(context, VideoPlaybackService::class.java)
        context.startForegroundService(intent)
    }

    fun obtenerVideoPorIndice(index: Int): Cancion? {
        return playlist.getOrNull(index)
    }
    fun relanzarVideo(context: Context, videoId: String) {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)

        Handler(Looper.getMainLooper()).postDelayed({
            reproducirVideo(context, videoId)
        }, 1500) // Esperamos 1.5 segundos antes de relanzar
    }


    fun obtenerTotal(): Int = playlist.size
}
