package com.kaz.tvplaylistify.util

import android.content.Context
import android.util.Log
import com.kaz.tvplaylistify.api.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SessionManager {

    fun obtenerOcrearSesion(uid: String, context: Context, onResult: (sessionId: String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = RetrofitInstance.api.createSession(mapOf("uid" to uid))
                val sessionId = response.sessionId
                Log.d("SessionManager", "🎉 Sesión creada: $sessionId")
                onResult(sessionId)
            } catch (e: Exception) {
                Log.e("SessionManager", "❌ Error al crear sesión", e)
            }
        }
    }
}
