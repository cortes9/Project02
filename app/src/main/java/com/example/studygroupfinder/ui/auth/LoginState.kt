package com.example.studygroupfinder.ui.auth

import io.github.jan.supabase.auth.user.UserInfo

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val user: UserInfo) : LoginUiState
    data class Error(val message: String) : LoginUiState
}
