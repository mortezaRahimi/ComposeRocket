package com.mortex.composeRocket.game.data.auth.repository

import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.mortex.composeRocket.game.data.auth.dataSource.FirebaseAuthDataSource
import com.mortex.composeRocket.game.domain.auth.model.AuthUser
import com.mortex.composeRocket.game.domain.auth.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val ds: FirebaseAuthDataSource) :
    AuthRepository {

    override fun authState(): Flow<AuthUser?> = ds.authState()

    override suspend fun signInWithGoogleIdToken(idToken: String) =
        ds.signInWithIdToken(idToken)

    override fun currentUser(): AuthUser? = ds.currentUser()

    override suspend fun signOut() = ds.signOut()

    override suspend fun revokeAccess() = ds.revokeAccess()
    override fun getClient(): GoogleSignInClient {
        return ds.getGoogleClient()
    }
}