
package com.example.studygroupfinder.ui.sessions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studygroupfinder.data.remote.StudySessionDto
import com.example.studygroupfinder.data.repository.StudySessionRepository
import com.example.studygroupfinder.data.repository.StudySessionRepositoryImpl
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Different states the session list can display.
sealed interface SessionListState {
    data object Loading : SessionListState
    data object Empty : SessionListState
    data class Success(val sessions: List<StudySessionDto>) : SessionListState
    data class Error(val message: String) : SessionListState
}

// Different states the details screen can display.
sealed interface SessionDetailState {
    data object Loading : SessionDetailState
    data class Success(val session: StudySessionDto) : SessionDetailState
    data class Error(val message: String) : SessionDetailState
}

// Loads sessions and prepares them for the screens.
class StudySessionViewModel(
    private val repository: StudySessionRepository = StudySessionRepositoryImpl()
) : ViewModel() {

    private val _listState = MutableStateFlow<SessionListState>(SessionListState.Loading)
    val listState = _listState.asStateFlow()

    private val _detailState = MutableStateFlow<SessionDetailState>(SessionDetailState.Loading)
    val detailState = _detailState.asStateFlow()

    private val _courseFilter = MutableStateFlow("")
    val courseFilter = _courseFilter.asStateFlow()

    private var listJob: Job? = null
    private var detailJob: Job? = null

    // Load sessions, canceling any older list request.
    fun loadSessions(courseCode: String = _courseFilter.value) {
        _courseFilter.value = courseCode.trim()
        listJob?.cancel()

        listJob = viewModelScope.launch {
            _listState.value = SessionListState.Loading

            try {
                val page = repository.getSessions(_courseFilter.value)

                _listState.value = if (page.content.isEmpty()) {
                    SessionListState.Empty
                } else {
                    SessionListState.Success(page.content)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _listState.value = SessionListState.Error(
                    e.message ?: "Unable to load study sessions."
                )
            }
        }
    }

    // Load one session for the details screen.
    fun loadSessionDetail(sessionId: Long) {
        detailJob?.cancel()

        detailJob = viewModelScope.launch {
            _detailState.value = SessionDetailState.Loading

            try {
                val session = repository.getSession(sessionId)
                _detailState.value = SessionDetailState.Success(session)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _detailState.value = SessionDetailState.Error(
                    e.message ?: "Unable to load session details."
                )
            }
        }
    }
}
