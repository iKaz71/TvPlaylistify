package com.kaz.tvplaylistify.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SessionScreen() {
    val context = LocalContext.current
    val sessionCode = "8799" // ejemplo fijo
    val connectedUsers = listOf("Tú (Owner)", "iPhone de Dani", "Android de Paco")

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Logo temporal
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.DarkGray, shape = RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Código de conexión",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = sessionCode,
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Usuarios conectados:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                connectedUsers.forEach { user ->
                    Text(
                        text = "• $user",
                        color = Color.LightGray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Button(
                onClick = { /* lógica para iniciar playlist */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3EA6FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = "▶ Reproducir Playlist",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
