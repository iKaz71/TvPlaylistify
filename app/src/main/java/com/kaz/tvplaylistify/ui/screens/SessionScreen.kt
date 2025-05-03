package com.kaz.tvplaylistify.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.database.*
import com.kaz.tvplaylistify.R
import com.kaz.tvplaylistify.model.UsuarioConectado
import com.kaz.tvplaylistify.util.PersistentHostManager

@Composable
fun SessionScreen(sessionId: String) {
    val context = LocalContext.current
    var sessionCode by remember { mutableStateOf("----") }
    val persistentHosts = remember { mutableStateListOf<String>() }
    val connectedUsers = remember { mutableStateListOf<UsuarioConectado>() }

    var showAddDialog by remember { mutableStateOf(false) }
    val seleccionados = remember { mutableStateMapOf<String, Boolean>() }

    // 🔄 Obtener código de sesión
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/code")
        ref.get().addOnSuccessListener { snapshot ->
            sessionCode = snapshot.getValue(String::class.java) ?: "----"
            Log.d("SessionScreen", "📟 Código de sesión cargado: $sessionCode")
        }.addOnFailureListener {
            Log.e("SessionScreen", "❌ Error al obtener código de sesión", it)
        }
    }

    // 🔄 Obtener anfitriones persistentes
    LaunchedEffect(Unit) {
        PersistentHostManager.obtenerAnfitriones(sessionId) { list ->
            persistentHosts.clear()
            persistentHosts.addAll(list)
        }
    }

    // 🔄 Escuchar usuarios conectados (solo para el diálogo)
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/connectedUsers")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                connectedUsers.clear()
                snapshot.children.mapNotNullTo(connectedUsers) {
                    it.getValue(UsuarioConectado::class.java)
                }
                connectedUsers.forEach { user ->
                    if (!seleccionados.containsKey(user.deviceName)) {
                        seleccionados[user.deviceName] = false
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("SessionScreen", "❌ Error al cargar usuarios conectados", error.toException())
            }
        })
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
                Image(
                    painter = painterResource(id = R.drawable.logo_playlistify),
                    contentDescription = "Logo Playlistify",
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Playlistify",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6FD8)
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Código de conexión", color = Color.Gray, fontSize = 14.sp)
                Text(sessionCode, color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Bold)

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "⭐ Anfitriones para este dispositivo:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (persistentHosts.isEmpty()) {
                    Text("• (Vacío)", color = Color.Gray, fontSize = 16.sp)
                } else {
                    persistentHosts.forEach { host ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = true,
                                onCheckedChange = {
                                    persistentHosts.remove(host)
                                    PersistentHostManager.guardarAnfitriones(sessionId, persistentHosts.toList()) {}
                                },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3EA6FF))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(host, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { showAddDialog = true }) {
                    Text(
                        text = "+ Administrar anfitrión manualmente",
                        color = Color(0xFF3EA6FF),
                        fontSize = 14.sp
                    )
                }
            }

            // 🎬 Botón para iniciar reproducción (desde TV)
            Button(
                onClick = {
                    val ref = FirebaseDatabase.getInstance()
                        .getReference("sessions/$sessionId/playback/play")

                    ref.setValue(true)
                        .addOnSuccessListener {
                            Log.d("SessionScreen", "▶ Reproducción iniciada desde TV")
                        }
                        .addOnFailureListener {
                            Log.e("SessionScreen", "❌ Error al iniciar reproducción desde TV", it)
                        }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3EA6FF)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("▶ Reproducir Playlist", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nuevo anfitrión", color = Color(0xFFFF6FD8)) },
            text = {
                if (connectedUsers.isEmpty()) {
                    Text("No hay usuarios conectados.")
                } else {
                    Column {
                        connectedUsers.forEach { user ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = seleccionados[user.deviceName] == true,
                                    onCheckedChange = { isChecked ->
                                        seleccionados[user.deviceName] = isChecked
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3EA6FF))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(user.deviceName, color = Color.White)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val nuevos = seleccionados.filterValues { it }.keys
                    persistentHosts.addAll(nuevos)
                    PersistentHostManager.guardarAnfitriones(sessionId, persistentHosts.toList()) {}
                    seleccionados.clear()
                    showAddDialog = false
                }) {
                    Text("Agregar")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    seleccionados.clear()
                    showAddDialog = false
                }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
