package com.kaz.tvplaylistify.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.FirebaseDatabase
import androidx.compose.ui.text.font.FontWeight


@Composable
fun SessionScreen(sessionId: String) {
    var sessionCode by remember { mutableStateOf("----") }
    val connectedUsers = listOf("Tú (Owner)", "iPhone de Dani", "Android de Paco")

    // 🔄 Obtener el código desde Firebase
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/code")
        ref.get().addOnSuccessListener { snapshot ->
            sessionCode = snapshot.getValue(String::class.java) ?: "----"
            Log.d("SessionScreen", "📟 Código de sesión cargado: $sessionCode")
        }.addOnFailureListener {
            Log.e("SessionScreen", "❌ Error al obtener código de sesión", it)
        }
    }

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
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color.DarkGray, shape = RoundedCornerShape(20.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Código de conexión", color = Color.Gray, fontSize = 14.sp)
                Text(sessionCode, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)

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
                    Text("• $user", color = Color.LightGray, fontSize = 16.sp, modifier = Modifier.align(Alignment.Start))
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
                Text("▶ Reproducir Playlist", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
