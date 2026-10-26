
package com.example.studygroupfinder.data.remote

import kotlinx.serialization.Serializable

// One page of sessions returned by the API.
@Serializable
data class StudySessionPageDto(
    val content: List<StudySessionDto>,
    val limit: Int,
    val offset: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val nextOffset: Int?
)

// Information about one study session.
@Serializable
data class StudySessionDto(
    val sessionId: Long,
    val creatorId: Long,
    val courseId: Long,
    val buildingId: Long?,
    val deliveryMode: String,
    val startTime: String,
    val endTime: String,
    val capacity: Int,
    val description: String?,
    val status: String,
    val participantCount: Int,
    val course: StudySessionCourseDto,
    val building: StudySessionBuildingDto?,
    val creator: StudySessionCreatorDto,
    val targetedMajors: List<StudySessionMajorDto>
)

// Course information inside a session.
@Serializable
data class StudySessionCourseDto(
    val courseId: Long,
    val code: String,
    val name: String
)

// Building can be missing for remote sessions.
@Serializable
data class StudySessionBuildingDto(
    val buildingId: Long,
    val name: String
)

// The student who created the session.
@Serializable
data class StudySessionCreatorDto(
    val userId: Long,
    val name: String
)

// A major targeted by the session.
@Serializable
data class StudySessionMajorDto(
    val majorId: Long,
    val name: String
)
