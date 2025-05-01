package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class VideoPlaybackService : Service() {

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(1, createNotification("Esperando video..."))
        Log.d("VideoPlaybackService", "✅ Servicio inicializado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra("EXTRA_SESSION_ID")
        Log.d("VideoPlaybackService", "📦 sessionId recibido en onStartCommand: $sessionId")

        if (!sessionId.isNullOrBlank()) {
            Log.d("VideoPlaybackService", "🛰 Iniciando VideoQueueManager con sessionId: $sessionId")
            VideoQueueManager.inicializar(this, sessionId)
        } else {
            Log.w("VideoPlaybackService", "⚠️ sessionId nulo en onStartCommand, pero continuaré esperando...")
        }

        return START_REDELIVER_INTENT
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, "tvplaylistify_channel")
            .setContentTitle("TvPlaylistify")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "tvplaylistify_channel",
                "TvPlaylistify Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("VideoPlaybackService", "❌ Servicio destruido")
    }
}
