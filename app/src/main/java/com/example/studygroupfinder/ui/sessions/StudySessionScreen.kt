
package com.example.studygroupfinder.ui.sessions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.studygroupfinder.data.remote.StudySessionDto

// Format the backend's UTC timestamp for display.
private fun displayTime(value: String): String {
    val clean = value.replace("T", " ").removeSuffix("Z")
    return if (value.endsWith("Z")) "$clean UTC" else clean
}

// Display a readable location.
private fun sessionLocation(session: StudySessionDto): String {
    return when (session.deliveryMode) {
        "REMOTE" -> "Remote"
        "ASYNC" -> "Asynchronous / Online"
        else -> session.building?.name ?: "Location not specified"
    }
}

// Display the first page of study sessions.
@Composable
fun StudySessionListScreen(
    viewModel: StudySessionViewModel,
    onSessionClick: (Long) -> Unit
) {
    val state by viewModel.listState.collectAsState()
    val savedFilter by viewModel.courseFilter.collectAsState()
    var filterText by remember { mutableStateOf(savedFilter) }

    // Fetch the first page when this screen opens.
    LaunchedEffect(Unit) {
        viewModel.loadSessions()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Study Sessions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                label = { Text("Course code") },
                placeholder = { Text("Example: DEMO-CS101") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { viewModel.loadSessions(filterText) }) {
                    Text("Search")
                }

                OutlinedButton(
                    onClick = {
                        filterText = ""
                        viewModel.loadSessions("")
                    }
                ) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val current = state) {
                is SessionListState.Loading -> {
                    LoadingMessage()
                }

                is SessionListState.Empty -> {
                    Column {
                        Text("No study sessions found.")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { viewModel.loadSessions() }) {
                            Text("Refresh")
                        }
                    }
                }

                is SessionListState.Error -> {
                    ErrorMessage(
                        message = current.message,
                        onRetry = { viewModel.loadSessions() }
                    )
                }

                is SessionListState.Success -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = current.sessions,
                            key = { it.sessionId }
                        ) { session ->
                            StudySessionCard(
                                session = session,
                                onClick = { onSessionClick(session.sessionId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// A clickable card for one study session.
@Composable
private fun StudySessionCard(
    session: StudySessionDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = session.course.code,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = session.course.name,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text("Starts: ${displayTime(session.startTime)}")
            Text("Location: ${sessionLocation(session)}")
            Text("Participants: ${session.participantCount}/${session.capacity}")

            Text(
                text = "Status: ${session.status}",
                color = if (session.status == "OPEN") {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("Tap to view details")
        }
    }
}

// Show all information about one study session.
@Composable
fun StudySessionDetailsScreen(
    sessionId: Long,
    viewModel: StudySessionViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.detailState.collectAsState()

    // Load the selected session from the backend.
    LaunchedEffect(sessionId) {
        viewModel.loadSessionDetail(sessionId)
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Back to Sessions")
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (val current = state) {
                is SessionDetailState.Loading -> {
                    LoadingMessage()
                }

                is SessionDetailState.Error -> {
                    ErrorMessage(
                        message = current.message,
                        onRetry = { viewModel.loadSessionDetail(sessionId) }
                    )
                }

                is SessionDetailState.Success -> {
                    val session = current.session

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text = session.course.code,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.titleMedium
                            )

                            Text(
                                text = session.course.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        item {
                            DetailItem("Creator", session.creator.name)
                            DetailItem("Description", session.description ?: "No description")
                            DetailItem("Start", displayTime(session.startTime))
                            DetailItem("End", displayTime(session.endTime))
                            DetailItem("Delivery mode", session.deliveryMode)
                            DetailItem("Location", sessionLocation(session))
                            DetailItem("Capacity", session.capacity.toString())
                            DetailItem("Participants", session.participantCount.toString())
                            DetailItem("Status", session.status)
                        }

                        item {
                            Text(
                                text = "Targeted Majors",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            if (session.targetedMajors.isEmpty()) {
                                Text("No majors listed")
                            } else {
                                session.targetedMajors.forEach { major ->
                                    Text("• ${major.name}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Reusable label and value.
@Composable
private fun DetailItem(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Reusable loading indicator.
@Composable
private fun LoadingMessage() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text("Loading study sessions...")
    }
}

// Reusable error message with retry.
@Composable
private fun ErrorMessage(
    message: String,
    onRetry: () -> Unit
) {
    Column {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
