package com.kaz.tvplaylistify.api

import com.kaz.tvplaylistify.model.SessionCreateResponse
import com.kaz.tvplaylistify.model.SessionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("session/{id}")
    suspend fun getSession(@Path("id") sessionId: String): Response<SessionResponse>

    @POST("session/create")
    suspend fun createSession(@Body body: Map<String, String>): Response<SessionCreateResponse>
}
