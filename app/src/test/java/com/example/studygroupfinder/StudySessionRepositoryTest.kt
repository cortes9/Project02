
package com.example.studygroupfinder

import com.example.studygroupfinder.data.remote.StudySessionApi
import com.example.studygroupfinder.data.repository.StudySessionRepositoryImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class StudySessionRepositoryTest {

    // Sample session using the real backend JSON structure.
    private val sessionJson = """
        {
          "sessionId": -13401,
          "creatorId": -13301,
          "courseId": -13101,
          "buildingId": null,
          "deliveryMode": "REMOTE",
          "startTime": "2026-10-24T09:00:00Z",
          "endTime": "2026-10-24T10:30:00Z",
          "capacity": 4,
          "description": "Programming practice",
          "status": "OPEN",
          "participantCount": 1,
          "course": {
            "courseId": -13101,
            "code": "DEMO-CS101",
            "name": "Demo Introduction to Programming"
          },
          "building": null,
          "creator": {
            "userId": -13301,
            "name": "Demo Avery Admin"
          },
          "targetedMajors": [
            {
              "majorId": -13001,
              "name": "Demo Computer Science"
            }
          ]
        }
    """.trimIndent()

    // Creates a fake HTTP client so tests do not contact Render.
    private fun repositoryWithResponse(
        responseBody: String,
        status: HttpStatusCode = HttpStatusCode.OK,
        onRequest: (String) -> Unit = {}
    ): StudySessionRepositoryImpl {

        val engine = MockEngine { request ->
            onRequest(request.url.toString())

            respond(
                content = responseBody,
                status = status,
                headers = headersOf(
                    HttpHeaders.ContentType,
                    "application/json"
                )
            )
        }

        val client = HttpClient(engine) {
            expectSuccess = true

            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        return StudySessionRepositoryImpl(
            StudySessionApi(
                client = client,
                baseUrl = "https://example.test"
            )
        )
    }

    @Test
    fun loadsStudySessionsSuccessfully() = runBlocking {
        // Check that the API loads a valid session.
        val json = """
            {
              "content": [$sessionJson],
              "limit": 20,
              "offset": 0,
              "totalElements": 1,
              "hasNext": false,
              "nextOffset": null
            }
        """.trimIndent()

        val repository = repositoryWithResponse(json)
        val result = repository.getSessions()

        assertEquals(1, result.content.size)
        assertEquals(-13401L, result.content.first().sessionId)
        assertEquals("DEMO-CS101", result.content.first().course.code)
    }

    @Test
    fun handlesEmptySessionList() = runBlocking {
        // Check that an empty list is handled correctly.
        val json = """
            {
              "content": [],
              "limit": 20,
              "offset": 0,
              "totalElements": 0,
              "hasNext": false,
              "nextOffset": null
            }
        """.trimIndent()

        val repository = repositoryWithResponse(json)
        val result = repository.getSessions()

        assertTrue(result.content.isEmpty())
    }

    @Test
    fun handlesApiError() = runBlocking {
        // Check that a server error gives a useful message.
        val repository = repositoryWithResponse(
            responseBody = "{}",
            status = HttpStatusCode.ServiceUnavailable
        )

        try {
            repository.getSessions()
            fail("Expected a server error")
        } catch (e: IllegalStateException) {
            assertTrue(e.message!!.contains("temporarily unavailable"))
        }
    }

    @Test
    fun forwardsCourseFilter() = runBlocking {
        // Check that the course filter is sent to the API.
        var requestedUrl = ""

        val json = """
            {
              "content": [],
              "limit": 20,
              "offset": 0,
              "totalElements": 0,
              "hasNext": false,
              "nextOffset": null
            }
        """.trimIndent()

        val repository = repositoryWithResponse(
            responseBody = json,
            onRequest = { requestedUrl = it }
        )

        repository.getSessions("DEMO-CS101")

        assertTrue(requestedUrl.contains("course_code=DEMO-CS101"))
        assertTrue(requestedUrl.contains("limit=20"))
        assertTrue(requestedUrl.contains("offset=0"))
    }

    @Test
    fun loadsSessionDetailsSuccessfully() = runBlocking {
        // Check that session details load correctly.
        var requestedUrl = ""

        val repository = repositoryWithResponse(
            responseBody = sessionJson,
            onRequest = { requestedUrl = it }
        )

        val session = repository.getSession(-13401L)

        assertTrue(
            requestedUrl.endsWith("/api/v1/study-sessions/-13401")
        )

        assertEquals("Demo Avery Admin", session.creator.name)
        assertEquals("REMOTE", session.deliveryMode)
        assertEquals(
            "Demo Computer Science",
            session.targetedMajors.first().name
        )
        assertEquals(null, session.building)
    }
}
