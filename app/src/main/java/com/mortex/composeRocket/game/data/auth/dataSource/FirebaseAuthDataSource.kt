package com.mortex.composeRocket.game.data.auth.dataSource

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mortex.composeRocket.game.domain.auth.AuthFailure
import com.mortex.composeRocket.game.domain.auth.model.AuthUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthDataSource(
    private val auth: FirebaseAuth,
    private val googleClient: GoogleSignInClient
) {

    fun authState(): Flow<AuthUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { fb ->
            trySend(fb.currentUser?.toDomain())
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithIdToken(idToken: String): Result<AuthUser> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: throw AuthFailure.Unknown()
        user.toDomain()
    }.mapError()

    fun currentUser(): AuthUser? = auth.currentUser?.toDomain()

    suspend fun signOut(): Result<Unit> = runCatching {
        googleClient.signOut().await()
        auth.signOut()
    }.mapError()

    fun getGoogleClient(): GoogleSignInClient{
        return googleClient
    }

    /** Optional: if you also use GoogleSignInClient.revokeAccess() call it outside or inject it here */
    suspend fun revokeAccess(): Result<Unit> = runCatching {
        auth.signOut() // local sign out; real revoke should be done via GoogleSignInClient
    }.mapError()

    // ---- helpers ----
    private fun com.google.firebase.auth.FirebaseUser.toDomain() = AuthUser(
        uid = uid,
        displayName = displayName,
        email = email,
        photoUrl = photoUrl?.toString()
    )

    private fun <T> Result<T>.mapError(): Result<T> = this.fold(
        onSuccess = { Result.success(it) },
        onFailure = { e ->
            val mapped = when {
                e.message?.contains("12501") == true -> AuthFailure.Canceled   // ApiException SIGN_IN_CANCELLED
                e.message?.contains("NETWORK") == true -> AuthFailure.Network
                else -> AuthFailure.Unknown(e)
            }
            Result.failure(mapped)
        }
    )
}