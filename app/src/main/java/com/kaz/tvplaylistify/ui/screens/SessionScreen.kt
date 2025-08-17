package com.kaz.tvplaylistify.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaz.tvplaylistify.R
import com.kaz.tvplaylistify.util.SessionManager
import com.google.firebase.database.*
import net.glxn.qrgen.android.QRCode

@Composable
fun SessionScreen(sessionId: String) {
    var sessionCode by remember { mutableStateOf("----") }
    val persistentHosts = remember { mutableStateListOf<Pair<String, String>>() } // (nombre, uid)
    val context = LocalContext.current

    // Refs de Firebase
    val codeRef = remember(sessionId) {
        FirebaseDatabase.getInstance().getReference("sessions/$sessionId/code")
    }
    val usuariosRef = remember(sessionId) {
        FirebaseDatabase.getInstance().getReference("sessions/$sessionId/usuarios")
    }

    // Escucha inicial + en tiempo real del código de la sala
    LaunchedEffect(sessionId) {
        // Carga inicial
        codeRef.get().addOnSuccessListener { snapshot ->
            val code = snapshot.getValue(String::class.java) ?: "----"
            sessionCode = code
            // ✅ Persistimos para que el Overlay lo pueda leer
            SessionManager.saveRoomCode(context, code)
        }
    }

    // Suscripción en tiempo real (por si cambia el código)
    DisposableEffect(sessionId) {
        val codeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val code = snapshot.getValue(String::class.java) ?: "----"
                sessionCode = code
                // ✅ Persistimos cada actualización
                SessionManager.saveRoomCode(context, code)
            }
            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        }
        codeRef.addValueEventListener(codeListener)

        onDispose {
            codeRef.removeEventListener(codeListener)
        }
    }

    // Escuchamos cambios en usuarios de la sesión (admins)
    DisposableEffect(sessionId) {
        val usuariosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hosts = mutableListOf<Pair<String, String>>() // (nombre, uid)
                snapshot.children.forEach { userSnap ->
                    val rol = userSnap.child("rol").getValue(String::class.java) ?: ""
                    val nombre = userSnap.child("nombre").getValue(String::class.java)
                        ?: userSnap.key.orEmpty()
                    val uid = userSnap.key.orEmpty()
                    if (rol == "admin") {
                        hosts.add(nombre to uid)
                    }
                }
                persistentHosts.clear()
                persistentHosts.addAll(hosts)
            }
            override fun onCancelled(error: DatabaseError) { /* no-op */ }
        }
        usuariosRef.addValueEventListener(usuariosListener)

        onDispose {
            usuariosRef.removeEventListener(usuariosListener)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F)),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Panel izquierdo: datos de sala y administradores ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_playlistify),
                    contentDescription = "Logo Playlistify",
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Playlistify",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6FD8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Código de conexión", color = Color.Gray, fontSize = 14.sp)
                Text(
                    sessionCode,
                    color = Color.White,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "⭐ Administradores:",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Podrán administrar la sala incluso si cambia el código de conexión.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (persistentHosts.isEmpty()) {
                    Text("• (Vacío)", color = Color.Gray, fontSize = 16.sp)
                } else {
                    persistentHosts.forEach { (hostName, hostUid) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                tint = Color.Unspecified,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(hostName, color = Color.White, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { quitarRolAnfitrionPersistente(sessionId, hostUid) }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_remove),
                                    contentDescription = "Eliminar admin",
                                    tint = Color.Red,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // --- Panel derecho: QR code ---
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Escanea este código desde la app móvil para convertirte en admin",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(bottom = 24.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                        .background(Color.Black, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    QrSection(
                        qrContent = sessionId,
                        modifier = Modifier.size(240.dp)
                    )
                }
            }
        }
    }
}

// Funcion para quitar rol "admin" en Firebase y dejar el usuario solo como "anfitrion"
fun quitarRolAnfitrionPersistente(sessionId: String, uid: String) {
    val usuariosRef = FirebaseDatabase.getInstance()
        .getReference("sessions/$sessionId/usuarios")
    usuariosRef.child(uid).child("rol").setValue("anfitrion")
}

// --- QR Section ---
@Composable
fun QrSection(
    qrContent: String,
    modifier: Modifier = Modifier
) {
    val qrBitmap by remember(qrContent) {
        mutableStateOf<Bitmap?>(
            QRCode
                .from(qrContent)
                .withSize(420, 420)
                .bitmap()
        )
    }
    qrBitmap?.let { bitmap ->
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Código QR",
            modifier = modifier
        )
    }
}
