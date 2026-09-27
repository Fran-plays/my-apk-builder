package com.petmorph.ai.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

interface PetMorphApi {
    @retrofit2.http.GET("api/health")
    suspend fun health(): retrofit2.Response<HealthResponse>

    @retrofit2.http.POST("api/ai/chat")
    suspend fun chat(@retrofit2.http.Body req: ChatRequest): retrofit2.Response<ChatResponse>

    @retrofit2.http.POST("api/tts")
    suspend fun tts(@retrofit2.http.Body req: TtsRequest): retrofit2.Response<TtsResponse>

    @retrofit2.http.POST("api/image/process")
    suspend fun processImage(@retrofit2.http.Body req: ImageProcessRequest): retrofit2.Response<ImageProcessResponse>
}

data class HealthResponse(val status: String)
data class ChatRequest(
    val characterId: String?, val name: String, val personality: String,
    val message: String, val maxLength: Int = 140, val language: String = "auto",
)
data class ChatResponse(val reply: String)
data class TtsRequest(val text: String, val voice: String? = null)
data class TtsResponse(val audioBase64: String?)
data class ImageProcessRequest(val imageBase64: String)
data class ImageProcessResponse(val imageBase64: String?, val error: String? = null)

class ApiClient(baseUrl: String) {
    private val logging = HttpLoggingInterceptor().apply {
        level = if (com.petmorph.ai.BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BASIC
        else HttpLoggingInterceptor.Level.NONE
    }
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    val api: PetMorphApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetMorphApi::class.java)
}
