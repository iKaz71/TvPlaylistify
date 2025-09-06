package com.kaz.tvplaylistify.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.google.firebase.database.*
import com.kaz.tvplaylistify.R
import com.kaz.tvplaylistify.api.RetrofitInstance
import com.kaz.tvplaylistify.model.SecretChangeRequest
import com.kaz.tvplaylistify.model.SecretEnableRequest
import com.kaz.tvplaylistify.util.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.glxn.qrgen.android.QRCode // ← lo dejamos por si mantienes flag para mostrar QR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(sessionId: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // -------- Estados UI --------
    var sessionCode by remember { mutableStateOf("----") }
    val persistentHosts = remember { mutableStateListOf<Pair<String, String>>() } // (nombre, uid)

    // adminWord inicia null y se carga desde SessionManager al montar
    var adminWord by remember(sessionId) { mutableStateOf<String?>(null) }

    var secretVisible by remember { mutableStateOf(false) }
    var secretEnabled by remember { mutableStateOf(true) }

    var showEditDialog by remember { mutableStateOf(false) }
    var newWordText by remember { mutableStateOf("") }
    var loadingAction by remember { mutableStateOf(false) }

    // -------- Firebase refs --------
    val codeRef = remember(sessionId) {
        FirebaseDatabase.getInstance().getReference("sessions/$sessionId/code")
    }
    val usuariosRef = remember(sessionId) {
        FirebaseDatabase.getInstance().getReference("sessions/$sessionId/usuarios")
    }
    val secretEnabledRef = remember(sessionId) {
        FirebaseDatabase.getInstance().getReference("sessions/$sessionId/secret/enabled")
    }

    // Carga inicial de code + palabra guardada localmente
    LaunchedEffect(sessionId) {
        // Code
        codeRef.get().addOnSuccessListener { snapshot ->
            val code = snapshot.getValue(String::class.java) ?: "----"
            sessionCode = code
            SessionManager.saveRoomCode(context, code)
        }

        // adminWord (guardada cuando se creó/rotó/modificó)
        adminWord = SessionManager.getAdminWord(context, sessionId)
        if (adminWord.isNullOrBlank()) {
            delay(200) // pequeño reintento por si prefs aún no se escribían
            adminWord = SessionManager.getAdminWord(context, sessionId)
        }
    }

    // Suscripciones en tiempo real (code, usuarios, secret/enabled)
    DisposableEffect(sessionId) {
        val codeListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val code = snapshot.getValue(String::class.java) ?: "----"
                sessionCode = code
                SessionManager.saveRoomCode(context, code)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        codeRef.addValueEventListener(codeListener)

        val usuariosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val hosts = mutableListOf<Pair<String, String>>()
                snapshot.children.forEach { userSnap ->
                    val rol = userSnap.child("rol").getValue(String::class.java) ?: ""
                    val nombre = userSnap.child("nombre").getValue(String::class.java)
                        ?: userSnap.key.orEmpty()
                    val uid = userSnap.key.orEmpty()
                    if (rol == "admin") hosts.add(nombre to uid)
                }
                persistentHosts.clear()
                persistentHosts.addAll(hosts)
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        usuariosRef.addValueEventListener(usuariosListener)

        val enabledListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                secretEnabled = snapshot.getValue(Boolean::class.java) ?: true
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        secretEnabledRef.addValueEventListener(enabledListener)

        onDispose {
            codeRef.removeEventListener(codeListener)
            usuariosRef.removeEventListener(usuariosListener)
            secretEnabledRef.removeEventListener(enabledListener)
        }
    }

    // -------- UI --------
    Scaffold(
        containerColor = Color(0xFF0F0F0F),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- Panel izquierdo: título, código y admins ---
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
                    persistentHosts.forEach { (hostName, _) ->
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
                        }
                    }
                }
            }

            // --- Panel derecho: Palabra secreta y controles ---
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Introduce esta palabra en tu app móvil para convertirte en admin de esta sala",
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
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp))
                        .shadow(12.dp, RoundedCornerShape(18.dp))
                        .background(Color.Black, RoundedCornerShape(18.dp))
                        .padding(24.dp)
                        .width(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Palabra + ojito
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val shown: String = when {
                                !secretVisible -> "••••••••"
                                adminWord.isNullOrBlank() -> "(no disponible)"
                                else -> adminWord!!
                            }
                            Text(
                                text = shown,
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.width(12.dp))
                            IconButton(onClick = { secretVisible = !secretVisible }) {
                                Icon(
                                    imageVector = if (secretVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (secretVisible) "Ocultar" else "Mostrar",
                                    tint = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Botones Modificar / Rotar
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    newWordText = ""
                                    showEditDialog = true
                                },
                                enabled = !loadingAction
                            ) { Text("Modificar") }

                            Button(
                                onClick = {
                                    scope.launch {
                                        loadingAction = true
                                        try {
                                            val resp = RetrofitInstance.api.rotateSecret(sessionId)
                                            if (resp.isSuccessful) {
                                                val newWord = resp.body()?.adminWord
                                                if (!newWord.isNullOrBlank()) {
                                                    adminWord = newWord
                                                    SessionManager.saveAdminWord(context, sessionId, newWord)
                                                    snackbarHostState.showSnackbar("Palabra rotada")
                                                } else {
                                                    snackbarHostState.showSnackbar("Rotación sin palabra")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("Error al rotar: ${resp.code()}")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("Error de red al rotar")
                                        } finally {
                                            loadingAction = false
                                        }
                                    }
                                },
                                enabled = !loadingAction
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Rotar")
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Switch habilitar/deshabilitar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Admin por palabra", color = Color.White)
                            Spacer(Modifier.width(12.dp))
                            Switch(
                                checked = secretEnabled,
                                onCheckedChange = { checked ->
                                    scope.launch {
                                        loadingAction = true
                                        try {
                                            val resp = RetrofitInstance.api.enableSecret(
                                                sessionId,
                                                SecretEnableRequest(checked)
                                            )
                                            if (resp.isSuccessful) {
                                                secretEnabled = checked
                                                snackbarHostState.showSnackbar(
                                                    if (checked) "Habilitado" else "Deshabilitado"
                                                )
                                            } else {
                                                snackbarHostState.showSnackbar("Error al actualizar: ${resp.code()}")
                                            }
                                        } catch (_: Exception) {
                                            snackbarHostState.showSnackbar("Error de red al actualizar")
                                        } finally {
                                            loadingAction = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ---- Diálogo para Modificar palabra ----
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!loadingAction) showEditDialog = false },
            confirmButton = {
                TextButton(
                    enabled = newWordText.trim().isNotEmpty() && !loadingAction,
                    onClick = {
                        scope.launch {
                            loadingAction = true
                            try {
                                val resp = RetrofitInstance.api.changeSecret(
                                    sessionId,
                                    SecretChangeRequest(newWordText.trim())
                                )
                                if (resp.isSuccessful) {
                                    val newWord = resp.body()?.adminWord ?: newWordText.trim()
                                    adminWord = newWord
                                    SessionManager.saveAdminWord(context, sessionId, newWord)
                                    showEditDialog = false
                                    snackbarHostState.showSnackbar("Palabra actualizada")
                                } else {
                                    snackbarHostState.showSnackbar("Error al actualizar: ${resp.code()}")
                                }
                            } catch (_: Exception) {
                                snackbarHostState.showSnackbar("Error de red al actualizar")
                            } finally {
                                loadingAction = false
                            }
                        }
                    }
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(
                    enabled = !loadingAction,
                    onClick = { showEditDialog = false }
                ) { Text("Cancelar") }
            },
            title = { Text("Modificar palabra") },
            text = {
                Column {
                    Text("Escribe una nueva palabra fácil de dictar (p. ej. \"luna-roja\").")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newWordText,
                        onValueChange = { newWordText = it },
                        singleLine = true,
                        label = { Text("Nueva palabra") }
                    )
                }
            }
        )
    }
}

// (Opcional) Sección QR si quieres mantenerla detrás de un flag
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
