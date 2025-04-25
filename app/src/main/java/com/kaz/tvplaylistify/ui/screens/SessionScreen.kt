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

import com.kaz.tvplaylistify.service.VideoPlaybackService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.kaz.tvplaylistify.api.RetrofitInstance
import com.kaz.tvplaylistify.model.SessionResponse



@Composable
fun SessionScreen(sessionId: String) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var code by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf("") }
    var hosts by remember { mutableStateOf(listOf<String>()) }
    var guests by remember { mutableStateOf(listOf<String>()) }

    // Llamada a la API para obtener la sesión
    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val retrofitResponse = RetrofitInstance.api.getSession(sessionId)
                val response = RetrofitInstance.api.getSession(sessionId)
                if (response != null) {
                    code = response.code.toString()
                    owner = response.host
                    hosts = response.hosts?.keys?.map { it } ?: listOf()
                    guests = response.guests?.keys?.map { it } ?: listOf()
                } else {
                    Log.e("SessionScreen", "Respuesta vacía de getSession")
                }
            } catch (e: Exception) {
                Log.e("SessionScreen", "Error al obtener sesión", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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

            if (hosts.isNotEmpty()) {
                item { Text("\n👑 Anfitriones:") }
                items(hosts) { host -> Text("- $host") }
            }

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
