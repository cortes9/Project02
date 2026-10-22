package com.example.studygroupfinder.ui.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studygroupfinder.BuildConfig
import com.example.studygroupfinder.data.repository.AuthRepository
import com.example.studygroupfinder.data.repository.AuthRepositoryImpl
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            if (user != null) {
                _uiState.value = LoginUiState.Success(user)
            }
        }
    }

    fun handleGoogleSignIn(context: Context) {
        val webClientId = BuildConfig.WEB_CLIENT_ID
        if (webClientId.isBlank() || webClientId.contains("your-google-web-client-id")) {
            _uiState.value = LoginUiState.Error("Please configure WEB_CLIENT_ID in env.local")
            return
        }

        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading

            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential

                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val authResult = authRepository.signInWithGoogleIdToken(idToken)
                authResult.onSuccess { user ->
                    _uiState.value = LoginUiState.Success(user)
                }.onFailure { exception ->
                    _uiState.value = LoginUiState.Error(exception.localizedMessage ?: "Supabase sign-in failed")
                }
            } catch (e: GetCredentialException) {
                _uiState.value = LoginUiState.Error("Google Sign-In failed: ${e.localizedMessage}")
            } catch (e: Exception) {
                _uiState.value = LoginUiState.Error("An error occurred: ${e.localizedMessage}")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            authRepository.signOut()
            _uiState.value = LoginUiState.Idle
        }
    }
}
