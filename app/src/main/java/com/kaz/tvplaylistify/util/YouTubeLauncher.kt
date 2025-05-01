package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

object YouTubeLauncher {

    fun launchYoutube(context: Context, videoId: String) {
        Log.d("YouTubeLauncher", "🎬 Lanzando video en YouTube: $videoId")
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.youtube.com/watch?v=$videoId")
                `package` = "com.google.android.youtube.tv"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            Log.d("YouTubeLauncher", "✅ Intent enviado correctamente")
        } catch (e: Exception) {
            Log.e("YouTubeLauncher", "❌ Error al lanzar YouTube", e)
        }
    }
}
