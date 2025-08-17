package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.TextView
import com.kaz.tvplaylistify.R

class OverlayService : Service() {

    companion object {
        const val EXTRA_ROOM_CODE = "EXTRA_ROOM_CODE"
        const val ACTION_UPDATE_CODE = "ACTION_UPDATE_CODE"
        private const val CHANNEL_ID = "room_overlay"
        private const val NOTIF_ID = 42
    }

    private lateinit var wm: WindowManager
    private var overlayView: View? = null
    private var params: WindowManager.LayoutParams? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        startForeground(NOTIF_ID, buildNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        val action = intent?.action
        val code = intent?.getStringExtra(EXTRA_ROOM_CODE) ?: "----"

        if (action == ACTION_UPDATE_CODE) {
            updateCode(code)
        } else {
            showOverlay(code)
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Overlay de sala",
                NotificationManager.IMPORTANCE_MIN
            )
            nm.createNotificationChannel(ch)
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_overlay)
            .setContentTitle("Código de sala visible")
            .setContentText("Mostrando logo y código sobre otras apps")
            .build()
    }

    private fun showOverlay(roomCode: String) {
        try {
            if (overlayView != null) {
                Log.d("OverlayService", "updateCode -> $roomCode")
                updateCode(roomCode)
                return
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 24
                y = 24
            }

            Log.d("OverlayService", "Inflating room_overlay_badge...")
            overlayView = LayoutInflater.from(this)
                .inflate(R.layout.room_overlay_badge, null, false)

            val tv = overlayView!!.findViewById<TextView>(R.id.tv_room_code)
            tv.text = roomCode

            wm.addView(overlayView, params)
            Log.d("OverlayService", "Overlay añadido correctamente con code=$roomCode")
        } catch (t: Throwable) {
            Log.e("OverlayService", "Fallo al mostrar overlay", t)
            stopSelf()
        }
    }


    private fun updateCode(roomCode: String) {
        val tv = overlayView?.findViewById<TextView>(R.id.tv_room_code)
        tv?.text = roomCode
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { runCatching { wm.removeView(it) } }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
