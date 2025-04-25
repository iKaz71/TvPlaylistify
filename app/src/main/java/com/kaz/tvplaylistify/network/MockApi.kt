package com.kaz.tvplaylistify.network

import com.kaz.tvplaylistify.model.Cancion

object MockApi {

    fun obtenerCancionesEnCola(): List<Cancion> {
        return listOf(
            Cancion(
                id = "dQw4w9WgXcQ",
                titulo = "Rick Astley - Never Gonna Give You Up",
                usuario = "anfitrión",
                thumbnailUrl = "https://img.youtube.com/vi/dQw4w9WgXcQ/0.jpg"
            ),
            Cancion(
                id = "hTWKbfoikeg",
                titulo = "Nirvana - Smells Like Teen Spirit",
                usuario = "anfitrión",
                thumbnailUrl = "https://img.youtube.com/vi/hTWKbfoikeg/0.jpg"
            )
        )
    }
}
