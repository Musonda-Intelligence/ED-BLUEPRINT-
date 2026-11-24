package com.edblueprint.app.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

/** Endpoints are relative to EDB_API_BASE_URL. See docs/API_CONTRACT.md. */
interface ApiService {

    // ----- Authentication -----

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // ----- Learning content -----

    @GET("topics")
    suspend fun topics(): TopicsResponse

    @GET("topics/{id}/materials")
    suspend fun topicMaterials(@Path("id") id: Long): MaterialsResponse

    @GET("materials/recent")
    suspend fun recentMaterials(): MaterialsResponse

    @GET("papers")
    suspend fun papers(): MaterialsResponse

    /** The server answers 403 if the material is premium and the subscription is not active. */
    @Streaming
    @GET("materials/{id}/file")
    suspend fun materialFile(@Path("id") id: Long): ResponseBody

    // ----- Quizzes -----

    @GET("quizzes")
    suspend fun quizzes(): QuizzesResponse

    @GET("quizzes/{id}")
    suspend fun quiz(@Path("id") id: Long): QuizDetailResponse

    @POST("quizzes/{id}/submit")
    suspend fun submitQuiz(@Path("id") id: Long, @Body body: SubmitRequest): AttemptResponse

    @GET("quiz-attempts")
    suspend fun attempts(): AttemptsResponse
}
