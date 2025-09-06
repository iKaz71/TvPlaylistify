package com.kaz.tvplaylistify.model

data class SessionCreateResponse(
    val sessionId: String,
    val code: String,
    val adminWord: String? = null   //  viene del backend para mostrar en TV
)
