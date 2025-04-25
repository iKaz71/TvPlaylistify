package com.kaz.tvplaylistify.api

import com.kaz.tvplaylistify.model.SessionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("session/{id}")
    suspend fun getSession(@Path("id") sessionId: String): Response<SessionResponse>
}