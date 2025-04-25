package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.util.Log
import com.kaz.tvplaylistify.NextVideoActivity

class VideoPlaybackService : Service() {

    private val TAG = "VideoPlaybackService"
    private val handler = Handler(Looper.getMainLooper())

    // 🔂 Lista provisional para pruebas
    private val testPlaylist = listOf(
        "dQw4w9WgXcQ",  // Rick Astley
        "hTWKbfoikeg",  // Nirvana
        "Ckom3gf57Yw"   // Muse
    )

    private var currentIndex = 0

    override fun onCreate() {
        super.onCreate()
        startForegroundWithNotification()
        reproducirVideoActual()
    }

    private fun startForegroundWithNotification() {
        val channelId = "tv_playlistify_channel"
        val channelName = "Reproducción TVPlaylistify"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
        manager.createNotificationChannel(channel)

        val notification: Notification = Notification.Builder(this, channelId)
            .setContentTitle("TvPlaylistify en reproducción")
            .setContentText("Controlando la playlist de YouTube")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .build()

        startForeground(1, notification)
    }

    private fun reproducirVideoActual() {
        if (currentIndex >= testPlaylist.size) {
            Log.d(TAG, "Fin de la lista de reproducción")
            stopSelf()
            return
        }

        val videoId = testPlaylist[currentIndex]
        val duracionMs = 10_000L // ⏱ Duración simulada de 10 segundos

        Log.d(TAG, "Lanzando video $videoId (duración: 10s)")

        val intent = Intent(this, NextVideoActivity::class.java).apply {
            putExtra("VIDEO_ID", videoId)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(intent)

        handler.postDelayed({
            currentIndex++
            Log.d(TAG, "Avanzando al siguiente video (índice = $currentIndex)")
            reproducirVideoActual()
        }, duracionMs)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
