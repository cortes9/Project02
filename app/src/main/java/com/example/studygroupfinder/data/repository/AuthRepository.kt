package com.example.studygroupfinder.data.repository

import android.util.Log
import com.example.studygroupfinder.data.remote.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.user.UserInfo

interface AuthRepository {
    suspend fun signInWithGoogleIdToken(googleIdToken: String, rawNonce: String? = null): Result<UserInfo>
    suspend fun getCurrentUser(): UserInfo?
    suspend fun signOut(): Result<Unit>
}

class AuthRepositoryImpl : AuthRepository {
    private val auth = SupabaseClient.client.auth

    override suspend fun signInWithGoogleIdToken(googleIdToken: String, rawNonce: String?): Result<UserInfo> {
        return runCatching {
            Log.d("AuthRepository", "Exchanging ID token with Supabase Auth...")
            auth.signInWith(IDToken) {
                idToken = googleIdToken
                provider = Google
                if (rawNonce != null) {
                    nonce = rawNonce
                }
            }
            val user = auth.currentUserOrNull() ?: throw IllegalStateException("User is null after sign in")
            Log.d("AuthRepository", "Successfully authenticated user: ${user.email ?: user.id}")
            user
        }.onFailure { ex ->
            Log.e("AuthRepository", "Supabase signInWith failed: ${ex.localizedMessage}", ex)
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
