package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.kaz.tvplaylistify.network.firebase.FirebaseQueueListener
import com.kaz.tvplaylistify.util.YouTubeLauncher

class VideoPlaybackService : Service() {

    private val handler = Handler()
    private var isListening = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(
            1,
            createNotification("Esperando video...")
        )
        Log.d("VideoPlaybackService", "✅ Servicio inicializado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val sessionId = intent?.getStringExtra("EXTRA_SESSION_ID")
        Log.d("VideoPlaybackService", "📦 sessionId recibido en onStartCommand: $sessionId")

        if (sessionId != null) {
            FirebaseQueueListener.setOnNewVideoListener { videoId, durationMs ->
                reproducirVideo(videoId, durationMs)
            }
            FirebaseQueueListener.empezarAEscucharQueue(this, sessionId)
            Log.d("VideoPlaybackService", "🛰 Iniciando escucha de Firebase con sessionId: $sessionId")
        } else {
            Log.w("VideoPlaybackService", "⚠️ sessionId nulo en onStartCommand, pero continuaré esperando...")
        }

        return START_REDELIVER_INTENT
    }

    private fun reproducirVideo(videoId: String, durationMs: Long) {
        Log.d("VideoPlaybackService", "▶ Reproduciendo video: $videoId por $durationMs ms")

        YouTubeLauncher.launchYoutube(this, videoId)

        val notification = createNotification("Reproduciendo: $videoId")
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notification)

        handler.removeCallbacksAndMessages(null)
        handler.postDelayed({
            Log.d("VideoPlaybackService", "⏭ Video terminado, esperando siguiente...")
        }, durationMs)
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
            val serviceChannel = NotificationChannel(
                "tvplaylistify_channel",
                "TvPlaylistify Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        isListening = false
        Log.d("VideoPlaybackService", "❌ Servicio destruido")
    }
}
