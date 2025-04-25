package com.kaz.tvplaylistify.model

data class SessionResponse(
    val code: Int,
    val host: String,
    val hosts: Map<String, Boolean>?,
    val guests: Map<String, Boolean>?
)
