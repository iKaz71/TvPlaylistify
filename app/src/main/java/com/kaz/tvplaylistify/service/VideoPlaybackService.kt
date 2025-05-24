package com.kaz.tvplaylistify.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.database.*
import com.kaz.tvplaylistify.R
import com.kaz.tvplaylistify.receiver.CastReceiver
import com.kaz.tvplaylistify.util.Constants.ACTION_LANZAR_VIDEO
import com.kaz.tvplaylistify.util.Constants.EXTRA_VIDEO_ID

class VideoPlaybackService : Service() {

    private val CHANNEL_ID = "video_playback_channel"
    private var playbackListener: ValueEventListener? = null
    private var playbackRef: DatabaseReference? = null

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
                        escucharPlaybackState(sessionId)
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

    private fun escucharPlaybackState(sessionId: String) {
        playbackRef = FirebaseDatabase.getInstance()
            .getReference("playbackState/$sessionId")

        playbackListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isPlaying = snapshot.child("playing").getValue(Boolean::class.java) ?: false
                val currentVideoId = snapshot.child("currentVideo/id").getValue(String::class.java)
                val durationIso = snapshot.child("currentVideo/duration").getValue(String::class.java)
                val durationMs = parseDurationToMillis(durationIso)

                if (isPlaying && !currentVideoId.isNullOrBlank()) {
                    Log.d("PlaybackManager", "🎬 Reproducción iniciada automáticamente: $currentVideoId ($durationMs ms)")

                    val intent = Intent(applicationContext, CastReceiver::class.java).apply {
                        action = ACTION_LANZAR_VIDEO
                        putExtra(EXTRA_VIDEO_ID, currentVideoId)
                    }
                    applicationContext.sendBroadcast(intent)

                    if (durationMs > 0) {
                        Thread {
                            try {
                                Thread.sleep(durationMs - 1500)

                                val queueRef = FirebaseDatabase.getInstance()
                                    .getReference("queues")
                                    .child(sessionId)

                                queueRef.orderByKey().limitToFirst(1).get()
                                    .addOnSuccessListener { queueSnapshot ->
                                        val siguiente = queueSnapshot.children.firstOrNull()
                                        if (siguiente != null) {
                                            Log.d("PlaybackManager", "⏭ Hay otra canción, se notificará a VideoQueueManager")
                                            VideoQueueManager.reproducirSiEsNecesario()
                                        } else {
                                            Log.d("PlaybackManager", "⛔ Cola vacía, marcando reproducción como detenida")
                                            FirebaseDatabase.getInstance()
                                                .getReference("playbackState")
                                                .child(sessionId)
                                                .child("playing")
                                                .setValue(false)
                                        }
                                    }

                            } catch (e: InterruptedException) {
                                Log.e("PlaybackManager", "❌ Interrumpido durante espera", e)
                            }
                        }.start()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlaybackManager", "❌ Error escuchando playbackState", error.toException())
            }
        }

        playbackRef?.addValueEventListener(playbackListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        playbackListener?.let {
            playbackRef?.removeEventListener(it)
        }
    }

    private fun parseDurationToMillis(iso: String?): Long {
        if (iso.isNullOrBlank()) return 0L
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L
        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0
        return ((h * 3600 + m * 60 + s) * 1000).toLong()
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
