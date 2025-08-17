package com.kaz.tvplaylistify

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.kaz.tvplaylistify.util.OverlayController   // opcional
import com.kaz.tvplaylistify.util.SessionManager     // opcional

class NextVideoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_next_video) // No es necesario si no muestras UI

        val videoId = intent.getStringExtra("VIDEO_ID")
        if (videoId.isNullOrBlank()) {
            Log.w("NextVideoActivity", "VIDEO_ID nulo o vacío; cerrando.")
            finish(); return
        }

        // OPCIONAL: enciende overlay aquí por si este flujo no pasó por VideoQueueManager
        runCatching {
            val code = SessionManager.getRoomCode(this) ?: "----"
            if (OverlayController.hasPermission(this)) {
                OverlayController.start(this, code)
            } else {
                Log.w("NextVideoActivity", "Sin permiso de overlay; omitiendo.")
            }
        }

        val url = "https://www.youtube.com/watch?v=$videoId"
        val intents = listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.google.android.youtube.tv"),
            Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.google.android.youtube"),
            Intent(Intent.ACTION_VIEW, Uri.parse(url))
        ).map {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        var launched = false
        for (i in intents) {
            try {
                startActivity(i); launched = true; break
            } catch (e: ActivityNotFoundException) {
                // probar siguiente fallback
            } catch (e: Exception) {
                Log.e("NextVideoActivity", "Error al lanzar YouTube", e)
            }
        }

        if (!launched) {
            Log.e("NextVideoActivity", "No hay app para manejar el intent de YouTube.")
            // OPCIONAL: apaga overlay si no se pudo lanzar
            OverlayController.stop(this)
        }

        finish()
    }
}
