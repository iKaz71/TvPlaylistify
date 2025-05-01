package com.kaz.tvplaylistify.receiver

import android.util.Log
import com.kaz.tvplaylistify.model.Cancion
import org.json.JSONObject

object MessageHandler {

    private const val TAG = "MessageHandler"

    fun procesarMensaje(mensaje: String): Cancion? {
        return try {
            val json = JSONObject(mensaje)
            val tipo = json.getString("tipo")

            if (tipo == "nueva_cancion") {
                val id = json.getString("id")
                val titulo = json.getString("titulo")
                val usuario = json.getString("usuario")
                val thumbnail = json.getString("thumbnailUrl")

                Cancion(id = id, titulo = titulo, usuario = usuario, thumbnailUrl = thumbnail)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al procesar el mensaje: ${e.message}", e)
            null
        }
    }
}
