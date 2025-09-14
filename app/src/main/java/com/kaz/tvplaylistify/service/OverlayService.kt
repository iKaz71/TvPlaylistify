package com.kaz.tvplaylistify.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import com.kaz.tvplaylistify.R
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

class OverlayService : Service() {

    companion object {
        const val EXTRA_ROOM_CODE = "EXTRA_ROOM_CODE"
        const val EXTRA_ROOM_NAME = "EXTRA_ROOM_NAME"
        const val ACTION_UPDATE_CODE = "ACTION_UPDATE_CODE"

        private const val CHANNEL_ID = "room_overlay"
        private const val NOTIF_ID = 42
        private const val LANDING_URL = "https://ikaz71.github.io/Playlistify-Android/"
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
        val roomCode = intent?.getStringExtra(EXTRA_ROOM_CODE) ?: "----"
        val roomName = intent?.getStringExtra(EXTRA_ROOM_NAME) ?: "Kahuna"

        if (action == ACTION_UPDATE_CODE) {
            updateCode(roomCode)
        } else {
            showOverlay(roomName, roomCode)
        }
        return START_STICKY
    }

    private fun buildNotification(): Notification {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Overlay de sala", NotificationManager.IMPORTANCE_MIN
            )
            nm.createNotificationChannel(ch)
        }
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_overlay)
            .setContentTitle("Código de sala visible")
            .setContentText("Mostrando información sobre otras apps")
            .build()
    }

    private fun showOverlay(roomName: String, roomCode: String) {
        try {
            if (overlayView != null) {
                updateTitleAndCode(roomName, roomCode)
                return
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else WindowManager.LayoutParams.TYPE_PHONE

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            Log.d("OverlayService", "Inflating overlay_root...")
            overlayView = LayoutInflater.from(this)
                .inflate(R.layout.overlay_root, null, false)

            // Título + código
            updateTitleAndCode(roomName, roomCode)

            // QR
            overlayView!!.findViewById<ImageView>(R.id.img_qr)
                .setImageBitmap(generateQr(LANDING_URL, 640, 640))

            val tv = overlayView!!.findViewById<TextView>(R.id.tv_bottom_message)
            tv.isSelected = true


            wm.addView(overlayView, params)
            Log.d("OverlayService", "Overlay añadido (sala=$roomName, code=$roomCode)")
        } catch (t: Throwable) {
            Log.e("OverlayService", "Fallo al mostrar overlay", t)
            stopSelf()
        }
    }

    private fun updateTitleAndCode(roomName: String, roomCode: String) {
        overlayView?.findViewById<TextView>(R.id.tv_room_title)?.text =
            "Playlistify"
        overlayView?.findViewById<TextView>(R.id.tv_room_code)?.text =
            "Código: $roomCode"
    }

    private fun updateCode(roomCode: String) {
        overlayView?.findViewById<TextView>(R.id.tv_room_code)?.text = "Código: $roomCode"
    }

    private fun generateQr(text: String, w: Int, h: Int): Bitmap {
        val matrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, w, h)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        for (x in 0 until w) for (y in 0 until h) {
            bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
        }
        return bmp
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayView?.let { runCatching { wm.removeView(it) } }
        overlayView = null
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
