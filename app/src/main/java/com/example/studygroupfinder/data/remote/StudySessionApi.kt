
package com.example.studygroupfinder.data.remote

import com.example.studygroupfinder.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.IOException

// Creates a Ktor client for the Spring Boot API.
fun createStudySessionHttpClient(): HttpClient {
    return HttpClient(Android) {
        expectSuccess = true

        // Convert JSON responses into Kotlin objects.
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        // Avoid waiting forever if the server is slow.
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 30000
        }
    }
}

// Handles requests to the study-session endpoints.
class StudySessionApi(
    private val client: HttpClient = createStudySessionHttpClient(),
    private val baseUrl: String = BuildConfig.STUDY_SESSION_API_BASE_URL
) {
    // Get the first 20 sessions, optionally filtered by course code.
    suspend fun getSessions(courseCode: String? = null): StudySessionPageDto {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/v1/study-sessions") {
                parameter("limit", 20)
                parameter("offset", 0)

                // Ktor safely encodes the course-code parameter.
                if (!courseCode.isNullOrBlank()) {
                    parameter("course_code", courseCode.trim())
                }
            }.body()
        } catch (e: ResponseException) {
            throw apiError(e)
        } catch (e: IOException) {
            throw IOException("Unable to connect to the server. Check your internet connection.", e)
        }
    }

    // Get one session using its ID.
    suspend fun getSession(sessionId: Long): StudySessionDto {
        return try {
            client.get("${baseUrl.trimEnd('/')}/api/v1/study-sessions/$sessionId")
                .body()
        } catch (e: ResponseException) {
            throw apiError(e)
        } catch (e: IOException) {
            throw IOException("Unable to connect to the server. Check your internet connection.", e)
        }
    }

    // Convert HTTP failures into readable messages.
    private fun apiError(error: ResponseException): Exception {
        val status = error.response.status.value

        val message = when (status) {
            404 -> "This study session could not be found."
            500, 502, 503, 504 -> "The server is temporarily unavailable. Try again."
            else -> "Request failed (HTTP $status). Please try again."
        }

        return IllegalStateException(message, error)
    }
}
