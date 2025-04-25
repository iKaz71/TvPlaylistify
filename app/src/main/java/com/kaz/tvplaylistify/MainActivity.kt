package com.kaz.tvplaylistify

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.kaz.tvplaylistify.service.OverlayService
import com.kaz.tvplaylistify.ui.screens.SessionScreen
import com.kaz.tvplaylistify.ui.theme.TVPlaylistifyTheme
import com.kaz.tvplaylistify.util.SessionManager

class MainActivity : ComponentActivity() {

    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        overlayPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Settings.canDrawOverlays(this)) {
                startService(Intent(this, OverlayService::class.java))
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
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
                    }
                }

                sessionId?.let {
                    SessionScreen(sessionId = it)
                } ?: run {
                    Surface {
                        Text("Inicializando sesión...", style = MaterialTheme.typography.headlineSmall)
                    }
                }
            }
        }
    }
}
