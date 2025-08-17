package com.kaz.tvplaylistify.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.kaz.tvplaylistify.service.OverlayService

object OverlayController {

    fun hasPermission(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun requestPermission(context: Context) {
        val i = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }

    fun start(context: Context, roomCode: String) {
        val i = Intent(context, OverlayService::class.java).apply {
            putExtra(OverlayService.EXTRA_ROOM_CODE, roomCode)
        }
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i)
        else context.startService(i)
    }

    fun stop(context: Context) {
        context.stopService(Intent(context, OverlayService::class.java))
    }

    fun update(context: Context, roomCode: String) {
        val i = Intent(context, OverlayService::class.java).apply {
            action = OverlayService.ACTION_UPDATE_CODE
            putExtra(OverlayService.EXTRA_ROOM_CODE, roomCode)
        }
        if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(i)
        else context.startService(i)
    }
}
