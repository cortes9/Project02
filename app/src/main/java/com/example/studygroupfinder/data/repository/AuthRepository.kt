package com.example.studygroupfinder.data.repository

import com.example.studygroupfinder.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(googleIdToken: String): Result<UserInfo>
    suspend fun getCurrentUser(): UserInfo?
    suspend fun signOut(): Result<Unit>
}

class AuthRepositoryImpl : AuthRepository {
    private val auth = SupabaseClient.client.auth

    override suspend fun signInWithGoogleIdToken(googleIdToken: String): Result<UserInfo> {
        return runCatching {
            auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
            }
            auth.currentUserOrNull() ?: throw IllegalStateException("User null after sign in")
        }
    }

    override suspend fun getCurrentUser(): UserInfo? {
        return auth.currentUserOrNull()
    }

    override suspend fun signOut(): Result<Unit> {
        return runCatching {
            auth.signOut()
        }
    }
}
