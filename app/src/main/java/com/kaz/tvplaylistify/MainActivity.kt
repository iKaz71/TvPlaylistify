package com.kaz.tvplaylistify

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.kaz.tvplaylistify.network.firebase.FirebaseQueueListener
import com.kaz.tvplaylistify.service.OverlayService
import com.kaz.tvplaylistify.service.VideoPlaybackService
import com.kaz.tvplaylistify.ui.screens.SessionScreen
import com.kaz.tvplaylistify.ui.theme.TVPlaylistifyTheme
import com.kaz.tvplaylistify.util.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Permiso de overlay para los controles flotantes
        val overlayLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            overlayLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        } else {
            startService(Intent(this, OverlayService::class.java))
        }

        setContent {
            TVPlaylistifyTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val uidTV = "tv-${android.os.Build.SERIAL ?: "default"}"
                    SessionManager.obtenerOcrearSesion(uidTV, this@MainActivity) { id ->
                        sessionId = id
                        Log.d("MainActivity", "Sesión lista: $id")

                        /* 1️⃣ Arrancamos YA el servicio de reproducción.
                         *    Así el servicio registra el listener ANTES de que
                         *    Firebase emita los «childAdded».                  */
                        val svcIntent = Intent(this@MainActivity, VideoPlaybackService::class.java)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(svcIntent)
                        } else startService(svcIntent)

                        /* 2️⃣ Ahora sí, empezamos a escuchar la cola. */
                        FirebaseQueueListener.empezarAEscucharQueue(
                            context = this@MainActivity,
                            sessionId = id
                        )
                    }
                }

                // UI
                sessionId?.let { SessionScreen(sessionId = it) } ?: Surface {
                    Text("Inicializando sesión…", style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
    }
}
