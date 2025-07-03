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
import com.kaz.tvplaylistify.service.OverlayService
import com.kaz.tvplaylistify.service.VideoPlaybackService
import com.kaz.tvplaylistify.ui.screens.SessionScreen
import com.kaz.tvplaylistify.ui.theme.TVPlaylistifyTheme
import com.kaz.tvplaylistify.util.SessionManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val overlayLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            overlayLauncher.launch(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
            )
        } else {
            startService(Intent(this, OverlayService::class.java))
        }

        setContent {
            TVPlaylistifyTheme {
                var sessionId by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(Unit) {
                    val uidTV = "tv-${Build.SERIAL ?: "default"}"
                    SessionManager.obtenerOcrearSesion(uidTV, this@MainActivity) { id ->
                        sessionId = id
                        Log.d("MainActivity", "🎬 Sesión lista: $id")

                        val svcIntent = Intent(this@MainActivity, VideoPlaybackService::class.java).apply {
                            putExtra("EXTRA_SESSION_ID", id)
                        }

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
