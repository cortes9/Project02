
package com.example.studygroupfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.studygroupfinder.ui.auth.LoginScreen
import com.example.studygroupfinder.ui.auth.LoginViewModel
import com.example.studygroupfinder.ui.sessions.StudySessionDetailsScreen
import com.example.studygroupfinder.ui.sessions.StudySessionListScreen
import com.example.studygroupfinder.ui.sessions.StudySessionViewModel
import com.example.studygroupfinder.ui.theme.StudyGroupFinderTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Keep the ViewModels when Android recreates the screen.
        val loginViewModel =
            ViewModelProvider(this)[LoginViewModel::class.java]

        val sessionViewModel =
            ViewModelProvider(this)[StudySessionViewModel::class.java]

        setContent {
            StudyGroupFinderTheme {
                StudyGroupFinderApp(
                    loginViewModel = loginViewModel,
                    sessionViewModel = sessionViewModel
                )
            }
        }
    }
}

@Composable
private fun StudyGroupFinderApp(
    loginViewModel: LoginViewModel,
    sessionViewModel: StudySessionViewModel
) {
    // Remember which screen is open.
    var currentScreen by rememberSaveable {
        mutableStateOf("login")
    }

    // Remember which study session was selected.
    var selectedSessionId by rememberSaveable {
        mutableLongStateOf(0L)
    }

    // Handle the Android Back button.
    BackHandler(enabled = currentScreen != "login") {
        currentScreen = when (currentScreen) {
            "details" -> "sessions"
            else -> "login"
        }
    }

    when (currentScreen) {

        "login" -> {
            LoginScreen(
                viewModel = loginViewModel,

                // Continue after Google login succeeds.
                onLoginSuccess = {
                    currentScreen = "sessions"
                },

                // Browse study sessions without signing in.
                onBrowseSessions = {
                    currentScreen = "sessions"
                }
            )
        }

        "sessions" -> {
            StudySessionListScreen(
                viewModel = sessionViewModel,

                // Open the selected session's details.
                onSessionClick = { sessionId ->
                    selectedSessionId = sessionId
                    currentScreen = "details"
                }
            )
        }

        "details" -> {
            SessionDetailsWithJoinButton(
                sessionId = selectedSessionId,
                viewModel = sessionViewModel,
                onBack = {
                    currentScreen = "sessions"
                }
            )
        }
    }
}

@Composable
private fun SessionDetailsWithJoinButton(
    sessionId: Long,
    viewModel: StudySessionViewModel,
    onBack: () -> Unit
) {
    // Control the message shown when Join is pressed.
    var showJoinMessage by rememberSaveable(sessionId) {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Display the existing study session details.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            StudySessionDetailsScreen(
                sessionId = sessionId,
                viewModel = viewModel,
                onBack = onBack
            )
        }

        // Keep the Join button below the details.
        Button(
            onClick = {
                showJoinMessage = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Join Session")
        }
    }

    // Explain that real joining is not connected yet.
    if (showJoinMessage) {
        AlertDialog(
            onDismissRequest = {
                showJoinMessage = false
            },
            title = {
                Text("Join Session")
            },
            text = {
                Text(
                    "Joining requires Google sign-in and " +
                            "a connection to the backend join API. " +
                            "This feature is not available yet."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showJoinMessage = false
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
