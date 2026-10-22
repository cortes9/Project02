package com.example.studygroupfinder

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext
import com.example.studygroupfinder.ui.auth.LoginScreen
import com.example.studygroupfinder.ui.theme.StudyGroupFinderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StudyGroupFinderTheme {
                val context = LocalContext.current
                LoginScreen(
                    onLoginSuccess = {
                        Toast.makeText(context, "Welcome to Study Group Finder!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}
