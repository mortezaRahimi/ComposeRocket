package com.mortex.composeRocket.game.domain.auth.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.mortex.composeRocket.game.domain.auth.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** Emits the current user (or null) and updates on changes */
    fun authState(): Flow<AuthUser?>

    /** Signs in with a Google ID token (from GIS) and returns the user on success */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<AuthUser>

    /** Returns the current user snapshot (or null) without subscribing */
    fun currentUser(): AuthUser?

    /** Sign the user out (local session only) */
    suspend fun signOut(): Result<Unit>

    /** Optional: permanently revoke access on Google side if you need it */
    suspend fun revokeAccess(): Result<Unit>

    fun getClient(): GoogleSignInClient
}