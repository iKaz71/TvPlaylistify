package com.kaz.tvplaylistify.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.kaz.tvplaylistify.model.Cancion

@Composable
fun QueueScreen(
    canciones: List<Cancion>,
    currentIndex: Int,
    onSelectSong: (Int) -> Unit,
    onPlay: (Cancion) -> Unit
) {
    Column(modifier = Modifier.padding(24.dp)) {
        Text(
            text = "Cola de reproducción",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            itemsIndexed(canciones) { index, cancion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    onClick = {
                        onSelectSong(index)
                        onPlay(cancion)
                    }
                ) {
                    Row(modifier = Modifier.padding(16.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(cancion.thumbnailUrl),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = cancion.titulo,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Agregado por: ${cancion.usuario}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
