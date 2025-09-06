package com.kaz.tvplaylistify.api

import com.kaz.tvplaylistify.model.SessionCreateResponse
import com.kaz.tvplaylistify.model.SessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import com.kaz.tvplaylistify.model.Cancion
import com.kaz.tvplaylistify.model.GenericMessage
import com.kaz.tvplaylistify.model.SecretChangeRequest
import com.kaz.tvplaylistify.model.SecretEnableRequest
import com.kaz.tvplaylistify.model.SecretResponse


interface ApiService {
    @GET("session/{id}")
    suspend fun getSession(@Path("id") sessionId: String): Response<SessionResponse>

    @POST("session/create")
    suspend fun createSession(@Body body: Map<String, String>): Response<SessionCreateResponse>

    @GET("queue/{sessionId}")
    suspend fun getQueue(@Path("sessionId") sessionId: String): Response<Map<String, Cancion>>

    @GET("queueOrder/{sessionId}")
    suspend fun getQueueOrder(@Path("sessionId") sessionId: String): Response<List<String>>

    @POST("queue/remove")
    suspend fun removeSong(@Body body: Map<String, String>): retrofit2.Response<Unit>

    // 🔐 Cambiar palabra manualmente
    @POST("session/{sessionId}/secret/change")
    suspend fun changeSecret(
        @Path("sessionId") sessionId: String,
        @Body req: SecretChangeRequest
    ): Response<SecretResponse>

    // 🔁 Rotar palabra (toma otra del pool)
    @POST("session/{sessionId}/secret/rotate")
    suspend fun rotateSecret(
        @Path("sessionId") sessionId: String
    ): Response<SecretResponse>

    // 🟢/🔴 Habilitar / deshabilitar elevación por palabra
    @POST("session/{sessionId}/secret/enable")
    suspend fun enableSecret(
        @Path("sessionId") sessionId: String,
        @Body req: SecretEnableRequest
    ): Response<GenericMessage>

}
