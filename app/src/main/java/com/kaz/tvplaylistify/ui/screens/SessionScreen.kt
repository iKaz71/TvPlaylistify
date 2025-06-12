package com.kaz.tvplaylistify.ui.screens

import android.graphics.Bitmap
import android.util.Log
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
import com.google.firebase.database.FirebaseDatabase
import com.kaz.tvplaylistify.R
import com.kaz.tvplaylistify.util.PersistentHostManager
import net.glxn.qrgen.android.QRCode

@Composable
fun SessionScreen(sessionId: String) {
    val context = LocalContext.current
    var sessionCode by remember { mutableStateOf("----") }
    val persistentHosts = remember { mutableStateListOf<String>() }

    // Cargar código y anfitriones persistentes (solo local)
    LaunchedEffect(sessionId) {
        val ref = FirebaseDatabase.getInstance().getReference("sessions/$sessionId/code")
        ref.get().addOnSuccessListener { snapshot ->
            sessionCode = snapshot.getValue(String::class.java) ?: "----"
        }
        // Cargar anfitriones persistentes del almacenamiento local
        val hosts = PersistentHostManager.obtenerAnfitriones(context)
        persistentHosts.clear()
        persistentHosts.addAll(hosts)
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

            // --- Panel izquierdo: datos de sala y anfitriones ---
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
                Text(sessionCode, color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "⭐ Anfitriones persistentes:",
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
                    persistentHosts.forEach { host ->
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
                            Text(host, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { /* Lógica para agregar anfitrión */ },
                    border = BorderStroke(1.5.dp, Color(0xFFDDDFE2)),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(32.dp),
                    modifier = Modifier
                        .height(46.dp)
                        .width(240.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google_logo),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Agregar anfitrión",
                        color = Color(0xFF202124),
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }

            // --- Panel derecho: QR code ---
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Escanea este código desde la app móvil para convertirte en anfitrión persistente",
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
