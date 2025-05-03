package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.kaz.tvplaylistify.R

class VideoPlaybackService : Service() {

    private val CHANNEL_ID = "video_playback_channel"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra("EXTRA_SESSION_ID")

        startForeground(1, buildNotification())

        if (sessionId != null) {
            Log.d("VideoPlaybackService", "✅ Servicio inicializado")
            Log.d("VideoPlaybackService", "📦 sessionId recibido en onStartCommand: $sessionId")

            FirebaseDatabase.getInstance()
                .getReference("sessions")
                .child(sessionId)
                .child("code")
                .get()
                .addOnSuccessListener { snapshot ->
                    val codigo = snapshot.getValue(String::class.java)
                    if (codigo != null) {
                        Log.d("VideoPlaybackService", "📟 Código de sesión obtenido: $codigo")
                        VideoQueueManager.inicializar(applicationContext, codigo, sessionId)
                        escucharPlayTrigger(sessionId)
                    } else {
                        Log.e("VideoPlaybackService", "❌ No se encontró el código para sessionId: $sessionId")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("VideoPlaybackService", "❌ Error consultando código de sesión", e)
                }
        } else {
            Log.e("VideoPlaybackService", "❌ sessionId es nulo")
        }

        return START_STICKY
    }

    private fun escucharPlayTrigger(sessionId: String) {
        val ref = FirebaseDatabase.getInstance()
            .getReference("sessions/$sessionId/playback/play")

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val shouldPlay = snapshot.getValue(Boolean::class.java) ?: false
                if (shouldPlay) {
                    Log.d("PlaybackManager", "✅ Playback activado")
                    VideoQueueManager.reproducirSiguiente()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlaybackManager", "❌ Error escuchando playback", error.toException())
            }
        })
    }

    private fun buildNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Reproducción de Playlistify",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Playlistify TV")
            .setContentText("Reproduciendo canciones desde tu sala")
            .setSmallIcon(R.drawable.ic_playlist_play)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
