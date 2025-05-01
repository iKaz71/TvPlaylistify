package com.kaz.tvplaylistify.network.firebase

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.database.*
import com.kaz.tvplaylistify.receiver.CastReceiver
import com.kaz.tvplaylistify.util.Constants.ACTION_LANZAR_VIDEO
import com.kaz.tvplaylistify.util.Constants.EXTRA_VIDEO_ID

object FirebaseQueueListener {

    private var databaseReference: DatabaseReference? = null
    private var newVideoListener: ((String, Long) -> Unit)? = null

    fun empezarAEscucharQueue(context: Context, sessionId: String) {
        Log.d("FirebaseQueueListener", "🛰 Escuchando queue de sesión: $sessionId")

        databaseReference = FirebaseDatabase.getInstance()
            .getReference("sessions")
            .child(sessionId)
            .child("queue")

        databaseReference?.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                Log.d("FirebaseQueueListener", "📡 Nodo agregado: ${snapshot.key} → ${snapshot.value}")

                val videoId = snapshot.child("id").getValue(String::class.java)
                val durationIso = snapshot.child("duration").getValue(String::class.java)
                val durationMs = durationIso?.let { parseDurationToMillis(it) } ?: 0L

                val titulo = snapshot.child("titulo").getValue(String::class.java)
                Log.d("FirebaseQueueListener", "🎵 Video detectado: $videoId (${durationMs}ms) - $titulo")

                if (!videoId.isNullOrBlank()) {
                    newVideoListener?.invoke(videoId, durationMs)


                } else {
                    Log.w("FirebaseQueueListener", "⚠️ videoId nulo o vacío, omitiendo...")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseQueueListener", "❌ Cancelado: ${error.message}", error.toException())
            }
        })
    }

    fun setOnNewVideoListener(listener: (String, Long) -> Unit) {
        newVideoListener = listener
    }

    private fun parseDurationToMillis(iso: String): Long {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L

        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0

        return ((h * 3600 + m * 60 + s) * 1000).toLong()
    }
}
