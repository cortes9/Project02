
package com.example.studygroupfinder.data.repository

import com.example.studygroupfinder.data.remote.StudySessionApi
import com.example.studygroupfinder.data.remote.StudySessionDto
import com.example.studygroupfinder.data.remote.StudySessionPageDto

// Describes the session data the app can request.
interface StudySessionRepository {
    suspend fun getSessions(courseCode: String? = null): StudySessionPageDto

    suspend fun getSession(sessionId: Long): StudySessionDto
}

// Reads session information through the Spring Boot API.
class StudySessionRepositoryImpl(
    private val api: StudySessionApi = StudySessionApi()
) : StudySessionRepository {

    override suspend fun getSessions(courseCode: String?): StudySessionPageDto {
        return api.getSessions(courseCode)
    }

    override suspend fun getSession(sessionId: Long): StudySessionDto {
        return api.getSession(sessionId)
    }
}
