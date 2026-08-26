package com.example.servora.data.remote

import com.example.servora.data.model.ProcessInfo
import com.example.servora.data.model.ServerMetrics
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Contract for the monitoring agent running on each server. Point a server's
 * `baseUrl` at an agent exposing these endpoints (a tiny psutil wrapper,
 * a Netdata proxy, or any custom service returning the same JSON shape).
 */
interface ServerApi {

    @GET("api/v1/metrics")
    suspend fun getMetrics(): ServerMetrics

    @GET("api/v1/processes")
    suspend fun getProcesses(): List<ProcessInfo>

    @POST("api/v1/actions")
    suspend fun performAction(@Body action: RemoteActionRequest)
}

@Serializable
data class RemoteActionRequest(
    val action: String,
    val target: String = "",
    val requestedAt: Long = System.currentTimeMillis()
)

object RemoteAction {
    const val REBOOT = "reboot"
    const val KILL_PROCESS = "kill_process"
}

@Singleton
class ApiFactory @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val clients = mutableMapOf<String, ServerApi>()

    @Synchronized
    fun create(baseUrl: String, apiKey: String?): ServerApi {
        val cacheKey = "$baseUrl|$apiKey"
        return clients.getOrPut(cacheKey) {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(ApiKeyInterceptor(apiKey))
                .build()

            val contentType = MediaType.parse("application/json")!!
            Retrofit.Builder()
                .baseUrl(baseUrl.ensureTrailingSlash())
                .client(client)
                .addConverterFactory(json.asConverterFactory(contentType))
                .build()
                .create(ServerApi::class.java)
        }
    }


    private fun String.ensureTrailingSlash(): String =
        if (endsWith("/")) this else "$this/"
}

private class ApiKeyInterceptor(private val apiKey: String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = if (apiKey.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("X-API-Key", apiKey)
                .build()
        }
        return chain.proceed(request)
    }
}
