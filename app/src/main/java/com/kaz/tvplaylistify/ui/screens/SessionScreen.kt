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
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var guests by remember { mutableStateOf(listOf<String>()) }
    var errorLoading by remember { mutableStateOf(false) }

    // Cargar la sesión con reintentos
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            var success = false
            var attempts = 0

            while (!success && attempts < 5) {
                try {
                    val response = RetrofitInstance.api.getSession(sessionId)

                    withContext(Dispatchers.Main) {
                        code = response.body()?.code ?: ""
                        owner = response.body()?.host ?: ""
                        guests = response.body()?.guests?.keys?.toList() ?: listOf()
                        success = true
                        errorLoading = false
                    }
                } catch (e: Exception) {
                    attempts++
                    Log.e("SessionScreen", "❌ Error obteniendo sesión (intento $attempts)", e)
                    delay(1500) // Esperar 1.5 segundos entre reintentos
                }
            }

            if (!success) {
                withContext(Dispatchers.Main) {
                    errorLoading = true
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (errorLoading) {
            Text(
                text = "❌ No se pudo cargar la sala",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleLarge
            )
            return@Column
        }

        Text("Código de conexión:", style = MaterialTheme.typography.titleMedium)
        Text(code, style = MaterialTheme.typography.displaySmall)

        Spacer(modifier = Modifier.height(24.dp))
        Text("Conectados", style = MaterialTheme.typography.titleLarge)

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            item { Text("🎮 Owner: $owner", style = MaterialTheme.typography.bodyLarge) }

            if (guests.isNotEmpty()) {
                item { Text("\n🙋 Invitados:") }
                items(guests) { guest -> Text("- $guest") }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            val serviceIntent = Intent(context, VideoPlaybackService::class.java)
            context.startForegroundService(serviceIntent)
        }) {
            Text("▶ Reproducir Playlist")
        }
    }
}
