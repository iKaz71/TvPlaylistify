// SessionScreen.kt
package com.kaz.tvplaylistify.ui.screens

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaz.tvplaylistify.api.RetrofitInstance
import com.kaz.tvplaylistify.service.VideoPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(sessionId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var code by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf(listOf<String>()) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            repeat(5) { attempt ->
                try {
                    val result = RetrofitInstance.api.getSession(sessionId).body()
                    result?.let { data ->
                        withContext(Dispatchers.Main) {
                            code = data.code ?: ""
                            owner = data.host ?: ""
                            guests = data.guests?.keys?.toList() ?: listOf()
                            error = false
                        }
                        return@launch
                    }
                } catch (e: Exception) {
                    Log.e("SessionScreen", "Error obteniendo sala (intento ${attempt + 1})", e)
                    delay(1500)
                }
            }
            error = true
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error) {
            Text("❌ No se pudo cargar la sala", color = MaterialTheme.colorScheme.error)
            return
        }

        Text("Código de conexión:", style = MaterialTheme.typography.titleMedium)
        Text(code, style = MaterialTheme.typography.displaySmall)
        Spacer(Modifier.height(24.dp))
        Text("Conectados", style = MaterialTheme.typography.titleLarge)

        LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
            item { Text("🎮 Owner: $owner") }
            if (guests.isNotEmpty()) {
                item { Text("\n🙋 Invitados:") }
                items(guests) { Text("- $it") }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            val intent = Intent(context, VideoPlaybackService::class.java).apply {
                putExtra("EXTRA_SESSION_ID", sessionId) // <- muy importante
            }
            Log.d("SessionScreen", "🛰 Enviando intent con sessionId = $sessionId")
            context.startForegroundService(intent)
        }) {
            Text("▶ Reproducir Playlist")
        }

    }
}
