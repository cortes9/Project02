
package com.example.studygroupfinder

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

        // Keep ViewModels active when Android recreates the screen.
        val loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        val sessionViewModel = ViewModelProvider(this)[StudySessionViewModel::class.java]

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
    // Remember which screen the user is currently viewing.
    var currentScreen by rememberSaveable { mutableStateOf("login") }
    var selectedSessionId by rememberSaveable { mutableLongStateOf(0L) }

    // Return to the list from details, or to login from the list.
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

                // After successful login, show study sessions.
                onLoginSuccess = {
                    currentScreen = "sessions"
                },

                // Allow public browsing without Google login.
                onBrowseSessions = {
                    currentScreen = "sessions"
                }
            )
        }

        "sessions" -> {
            StudySessionListScreen(
                viewModel = sessionViewModel,
                onSessionClick = { sessionId ->
                    selectedSessionId = sessionId
                    currentScreen = "details"
                }
            )
        }

        "details" -> {
            StudySessionDetailsScreen(
                sessionId = selectedSessionId,
                viewModel = sessionViewModel,
                onBack = {
                    currentScreen = "sessions"
                }
            )
        }
    }
}
