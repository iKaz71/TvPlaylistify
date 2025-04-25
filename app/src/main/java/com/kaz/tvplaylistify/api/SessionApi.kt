package com.kaz.tvplaylistify.api

import com.kaz.tvplaylistify.model.SessionCreateResponse
import com.kaz.tvplaylistify.model.SessionResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface SessionApi {
    @GET("session/{sessionId}")
    suspend fun getSession(@Path("sessionId") sessionId: String): SessionResponse

    @POST("session/create")
    suspend fun createSession(@Body body: Map<String, String>): SessionCreateResponse

}
