package com.kaz.tvplaylistify.cast

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.images.WebImage
import com.kaz.tvplaylistify.model.Cancion

object CastManager {

    private const val TAG = "CastManager"

    fun getCurrentSession(context: Context): CastSession? {
        return try {
            CastContext.getSharedInstance(context).sessionManager.currentCastSession
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo sesión de Cast: ${e.message}", e)
            null
        }
    }

    fun reproducirCancion(context: Context, cancion: Cancion) {
        val session = getCurrentSession(context)
        val client = session?.remoteMediaClient

        if (client != null) {
            try {
                val metadata = MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE).apply {
                    putString(MediaMetadata.KEY_TITLE, cancion.titulo)
                    addImage(WebImage(Uri.parse(cancion.thumbnailUrl)))
                }

                val mediaInfo = MediaInfo.Builder("https://www.youtube.com/tv")
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setContentType("video/mp4")
                    .setMetadata(metadata)
                    .setCustomData(org.json.JSONObject().apply {
                        put("videoId", cancion.id)
                    })
                    .build()

                client.load(mediaInfo, true, 0)
            } catch (e: Exception) {
                Log.e(TAG, "Error enviando video a Chromecast: ${e.message}", e)
            }
        } else {
            Log.w(TAG, "No hay cliente de media disponible")
        }
    }
}
