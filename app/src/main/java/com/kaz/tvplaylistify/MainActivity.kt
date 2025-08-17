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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kaz.tvplaylistify.service.VideoPlaybackService
import com.kaz.tvplaylistify.ui.screens.SessionScreen
import com.kaz.tvplaylistify.ui.theme.TVPlaylistifyTheme
import com.kaz.tvplaylistify.util.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Solo solicitamos permiso de overlay. NO iniciamos el servicio aquí.
        val overlayLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            // Si conceden el permiso, no hacemos nada más aquí.
            // El overlay se encenderá cuando inicie la reproducción (VideoQueueManager).
            if (Settings.canDrawOverlays(this)) {
                Log.d("MainActivity", "Permiso de overlay concedido.")
            } else {
                Log.w("MainActivity", "Permiso de overlay denegado.")
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            overlayLauncher.launch(
                Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
            )
        }

        setContent {
            TVPlaylistifyTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    // Identificador de la TV (si quieres, luego migramos a ANDROID_ID)
                    val uidTV = "tv-${Build.SERIAL ?: "default"}"

                    SessionManager.obtenerOcrearSesion(uidTV, this@MainActivity) { id ->
                        sessionId = id
                        Log.d("MainActivity", "Sesión lista: $id")

                        // Inicia tu servicio de reproducción/escucha de cola
                        val svcIntent = Intent(this@MainActivity, VideoPlaybackService::class.java)
                            .putExtra("EXTRA_SESSION_ID", id)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(svcIntent)
                        } else {
                            startService(svcIntent)
                        }
                    }
                }

                sessionId?.let {
                    SessionScreen(sessionId = it)
                } ?: Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color.Red)
                    }
                }
            }
        }
    }
}
