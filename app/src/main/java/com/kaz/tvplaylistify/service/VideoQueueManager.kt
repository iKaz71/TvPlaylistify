package com.kaz.tvplaylistify.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kaz.tvplaylistify.api.ApiService
import com.kaz.tvplaylistify.model.Cancion
import com.kaz.tvplaylistify.util.YouTubeLauncher
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.firebase.database.*
import com.kaz.tvplaylistify.util.OverlayController
import com.kaz.tvplaylistify.util.SessionManager

object VideoQueueManager {

    private var context: Context? = null
    private var sessionId: String? = null
    private val handler = Handler(Looper.getMainLooper())
    private var reproduciendo = false

    private var queueListener: ValueEventListener? = null
    private var queueRef: DatabaseReference? = null

    private val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://playlistify-api-production.up.railway.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun inicializar(ctx: Context, codigo: String, sessionId: String) {
        context = ctx
        this.sessionId = sessionId

        Log.d("VideoQueueManager", "Inicializando cola completa en sesión: $sessionId")

        queueListener?.let { queueRef?.removeEventListener(it) }

        queueRef = FirebaseDatabase.getInstance().getReference("queuesOrder/$sessionId")
        queueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("VideoQueueManager", "Listener: Cambio en la cola detectado")
                reproducirSiEsNecesario()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("VideoQueueManager", "Listener cancelado: ${error.message}")
            }
        }
        queueRef?.addValueEventListener(queueListener!!)

        reproducirSiEsNecesario()
    }

    fun reproducirSiEsNecesario() {
        val ctx = context ?: return
        val sid = sessionId ?: return

        if (reproduciendo) {
            Log.d("VideoQueueManager", "Aún hay video en reproducción, no se hará nada.")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("VideoQueueManager", "Llamando a getOrderedQueueAndOrder(api, $sid)")
                val (orderedSongs, orderList) = getOrderedQueueAndOrder(api, sid)
                Log.d("VideoQueueManager", "Canciones ordenadas recibidas (${orderedSongs.size})")

                val nextSong = orderedSongs.firstOrNull()
                val pushKey = orderList.firstOrNull()

                if (nextSong == null || pushKey == null) {
                    Log.d("VideoQueueManager", "Cola vacía, no hay videos para reproducir (no apagamos overlay si ya estaba).")
                    return@launch
                }

                val durationMs = parseDurationToMillis(nextSong.duration)
                if (nextSong.id.isBlank() || durationMs <= 0) {
                    Log.w("VideoQueueManager", "Video inválido detectado. Ignorando (overlay permanece si ya estaba).")
                    return@launch
                }

                // ✅ Encendemos overlay al iniciar la PRIMERA reproducción (o lo mantenemos encendido)
                val code = SessionManager.getRoomCode(ctx) ?: "----"
                if (OverlayController.hasPermission(ctx)) {
                    OverlayController.start(ctx, code)
                } else {
                    Log.w("VideoQueueManager", "No hay permiso de overlay (SYSTEM_ALERT_WINDOW).")
                }

                Log.d("VideoQueueManager", "▶ Reproduciendo video: ${nextSong.id}")
                YouTubeLauncher.launchYoutube(ctx, nextSong.id)

                updatePlaybackState(sid, nextSong)
                reproduciendo = true

                // Eliminar la canción luego de su duración
                handler.postDelayed({
                    CoroutineScope(Dispatchers.Main).launch {
                        val eliminado = withContext(Dispatchers.IO) {
                            eliminarCancionReproducida(api, sid, pushKey)
                        }
                        Log.d("VideoQueueManager", "¿Se eliminó la canción? $eliminado")
                        reproduciendo = false
                        Log.d("VideoQueueManager", "Tiempo finalizado, intentando reproducir siguiente...")
                        reproducirSiEsNecesario()
                    }
                }, durationMs + 1000L)

            } catch (e: Exception) {
                Log.e("VideoQueueManager", "Error al obtener la cola ordenada (overlay se mantiene si ya estaba)", e)
            }
        }
    }

    private fun updatePlaybackState(sessionId: String, cancion: Cancion) {
        FirebaseDatabase.getInstance()
            .getReference("playbackState/$sessionId")
            .setValue(
                mapOf(
                    "playing" to true,
                    "currentVideo" to cancion
                )
            )
    }

    private suspend fun eliminarCancionReproducida(
        api: ApiService,
        sessionId: String,
        pushKey: String,
        userId: String = "tv"
    ): Boolean {
        val params = mapOf(
            "sessionId" to sessionId,
            "pushKey" to pushKey,
            "userId" to userId
        )
        return try {
            val response = api.removeSong(params)
            Log.d("VideoQueueManager", "removeSong() → code=${response.code()}, body=${response.errorBody()?.string()}")
            if (response.isSuccessful) {
                Log.d("VideoQueueManager", "Canción eliminada correctamente: $pushKey")
                true
            } else {
                Log.e("VideoQueueManager", "Error eliminando canción: $pushKey (code: ${response.code()})")
                false
            }
        } catch (e: Exception) {
            Log.e("VideoQueueManager", "Excepción al eliminar canción: $pushKey", e)
            false
        }
    }

    private suspend fun getOrderedQueueAndOrder(
        api: ApiService,
        sessionId: String
    ): Pair<List<Cancion>, List<String>> {
        val queueResponse = withContext(Dispatchers.IO) { api.getQueue(sessionId) }
        val orderResponse = withContext(Dispatchers.IO) { api.getQueueOrder(sessionId) }

        val queueMap = queueResponse.body() ?: emptyMap<String, Cancion>()
        val orderList = orderResponse.body() ?: emptyList<String>()

        val cancionesOrdenadas = orderList.mapNotNull { queueMap[it] }
        return Pair(cancionesOrdenadas, orderList)
    }

    private fun parseDurationToMillis(iso: String): Long {
        val regex = Regex("""PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?""")
        val match = regex.find(iso) ?: return 0L
        val h = match.groupValues[1].toIntOrNull() ?: 0
        val m = match.groupValues[2].toIntOrNull() ?: 0
        val s = match.groupValues[3].toIntOrNull() ?: 0
        return ((h * 3600 + m * 60 + s) * 1000).toLong()
    }

    /** Llamar explícitamente cuando cierres la sala/sesión desde UI */
    fun apagarOverlayAlCerrarSesion() {
        context?.let { OverlayController.stop(it) }
    }
}
