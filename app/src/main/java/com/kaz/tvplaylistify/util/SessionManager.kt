package com.kaz.tvplaylistify.util

import android.content.Context
import android.util.Log
import com.kaz.tvplaylistify.api.RetrofitInstance
import com.kaz.tvplaylistify.model.SessionResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException

object SessionManager {

    private const val PREFS_NAME = "playlistify"
    private const val SESSION_ID_KEY = "sessionId"

    fun obtenerOcrearSesion(uid: String, context: Context, onResult: (sessionId: String) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedSessionId = prefs.getString(SESSION_ID_KEY, null)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (savedSessionId != null) {
                    val response = RetrofitInstance.api.getSession(savedSessionId)
                    onResult(savedSessionId)
                } else {
                    crearSesion(uid, context, onResult)
                }
            } catch (e: HttpException) {
                Log.w("SessionManager", "❌ Sesión no encontrada, creando nueva...")
                crearSesion(uid, context, onResult)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error inesperado", e)
            }
        }
    }

    private fun crearSesion(uid: String, context: Context, onResult: (sessionId: String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.createSession(mapOf("uid" to uid))
                val sessionId = response.sessionId
                Log.d("SessionManager", "🎉 Sesión creada: $sessionId")
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putString(SESSION_ID_KEY, sessionId).apply()
                onResult(sessionId)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error al crear sesión", e)
            }
        }
    }
}
