package com.kaz.tvplaylistify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.kaz.tvplaylistify.util.Constants.EXTRA_VIDEO_ID
import com.kaz.tvplaylistify.util.YouTubeLauncher

class CastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)

        if (videoId.isNullOrBlank()) {
            Log.w("CastReceiver", "Video ID nulo o vacío recibido.")
            return
        }

        Log.d("CastReceiver", "Broadcast recibido con ID: $videoId")
        Log.d("CastReceiver", "Enviando a YouTubeLauncher...")
        YouTubeLauncher.launchYoutube(context, videoId)

    }
}
