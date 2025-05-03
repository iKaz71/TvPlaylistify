package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log

object YouTubeLauncher {

    fun launchYoutube(context: Context, videoId: String) {
        val youtubeAppIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.youtube.com/watch?v=$videoId")
            `package` = "com.google.android.youtube.tv"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        Log.d("YouTubeLauncher", "🎬 Intentando lanzar YouTube con ID: $videoId")

        try {
            // ✅ Verifica si la app de YouTube para TV está instalada
            val pm = context.packageManager
            val activities = pm.queryIntentActivities(youtubeAppIntent, PackageManager.MATCH_DEFAULT_ONLY)

            if (activities.isNotEmpty()) {
                context.startActivity(youtubeAppIntent)
                Log.d("YouTubeLauncher", "✅ Video lanzado en YouTube TV")
            } else {
                Log.w("YouTubeLauncher", "⚠️ App YouTube TV no encontrada. Usando navegador.")

                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://www.youtube.com/watch?v=$videoId")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallbackIntent)
                Log.d("YouTubeLauncher", "🌐 Video lanzado en navegador")
            }
        } catch (e: Exception) {
            Log.e("YouTubeLauncher", "❌ Error al intentar lanzar YouTube", e)
        }
    }
}
