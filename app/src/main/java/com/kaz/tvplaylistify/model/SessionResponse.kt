package com.kaz.tvplaylistify.model

data class SessionResponse(
    val code: String = "",
    val sessionId: String = "",
    val host: String = "",
    val guests: Map<String, Boolean> = emptyMap(),
    val pendingRequests: Map<String, Boolean> = emptyMap()
)
