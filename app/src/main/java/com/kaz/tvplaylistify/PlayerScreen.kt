package com.kaz.tvplaylistify.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kaz.tvplaylistify.model.Cancion

@Composable
fun PlayerScreen(
    currentSong: Cancion?,
    onBack: () -> Unit,
    onPlayNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = currentSong?.titulo ?: "Sin título",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Agregado por: ${currentSong?.usuario ?: "desconocido"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(onClick = onBack) {
            Text("Volver")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onPlayNext) {
            Text("Siguiente")
        }
    }
}
