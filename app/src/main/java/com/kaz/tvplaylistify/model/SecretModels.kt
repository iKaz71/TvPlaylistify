package com.kaz.tvplaylistify.model


data class SecretChangeRequest(val newWord: String)
data class SecretEnableRequest(val enabled: Boolean)

data class SecretResponse(
    val ok: Boolean,
    val message: String? = null,
    val adminWord: String? = null   // backend regresa la nueva palabra para que la muestres
)

data class GenericMessage(
    val ok: Boolean,
    val message: String? = null
)

