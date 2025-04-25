package com.kaz.tvplaylistify.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.*
import android.widget.TextView
import com.kaz.tvplaylistify.R

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: View

    override fun onCreate() {
        super.onCreate()

        Log.d("OverlayService", "Overlay iniciado correctamente")

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val inflater = LayoutInflater.from(this)
        overlayView = inflater.inflate(R.layout.floating_overlay, null)

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        val message = overlayView.findViewById<TextView>(R.id.overlay_message)
        message.alpha = 1f  // Visibilidad forzada para prueba

        windowManager.addView(overlayView, layoutParams)

        // Auto-destruye después de 2 segundos
        Handler().postDelayed({
            stopSelf()
        }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) {
            windowManager.removeView(overlayView)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
