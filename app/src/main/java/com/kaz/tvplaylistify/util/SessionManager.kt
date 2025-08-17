package com.kaz.tvplaylistify.util

import android.content.Context
import android.util.Log
import com.kaz.tvplaylistify.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

object SessionManager {

    private const val PREFS_NAME = "playlistify"
    private const val SESSION_ID_KEY = "sessionId"
    private const val ROOM_CODE_KEY = "roomCode"

    fun saveRoomCode(context: Context, code: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(ROOM_CODE_KEY, code).apply()
    }

    fun getRoomCode(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(ROOM_CODE_KEY, null)
    }

    fun obtenerOcrearSesion(uid: String, context: Context, onResult: (sessionId: String) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSessionId = prefs.getString(SESSION_ID_KEY, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (savedSessionId != null) {
                    val response = RetrofitInstance.api.getSession(savedSessionId)

                    val sessionBody = response.body()
                    if (sessionBody != null && sessionBody.sessionId.isNotEmpty()) {
                        Log.d("SessionManager", "✅ Sesión válida encontrada: ${sessionBody.sessionId}")
                        onResult(savedSessionId)
                        return@launch
                    }
                }
                crearSesionConReintento(uid, context, onResult)
            } catch (e: HttpException) {
                Log.w("SessionManager", "⚠ Sesión no encontrada, creando nueva...")
                crearSesionConReintento(uid, context, onResult)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error inesperado", e)
                crearSesionConReintento(uid, context, onResult)
            }
        }
    }

    private suspend fun crearSesionConReintento(uid: String, context: Context, onResult: (sessionId: String) -> Unit) {
        var intentos = 0
        val maxIntentos = 3

        while (intentos < maxIntentos) {
            try {
                val response = RetrofitInstance.api.createSession(mapOf("uid" to uid))
                val createBody = response.body()

                if (createBody != null) {
                    val nuevoSessionId = createBody.sessionId
                    Log.d("SessionManager", "🎉 Sesión creada exitosamente: $nuevoSessionId")

                    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        .edit().putString(SESSION_ID_KEY, nuevoSessionId).apply()

                    onResult(nuevoSessionId)
                    return
                }
            } catch (e: IOException) {
                intentos++
                Log.w("SessionManager", "🌐 Timeout o error de red. Intento $intentos de $maxIntentos")
                delay(2000)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error al crear sesión", e)
                break
            }
        }

        Log.e("SessionManager", "🚨 No se pudo crear la sesión después de $maxIntentos intentos")
    }
}
