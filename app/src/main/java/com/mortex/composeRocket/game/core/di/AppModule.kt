package com.mortex.composeRocket.game.core.di

import android.app.Application
import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.data.auth.dataSource.FirebaseAuthDataSource
import com.mortex.composeRocket.game.data.auth.repository.AuthRepositoryImpl
import com.mortex.composeRocket.game.domain.auth.repository.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Provides
    @Singleton
    fun provideGoogleSignInClient(
        @ApplicationContext context: Context
    ): GoogleSignInClient{
        val gso =GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context,gso)

    }

    @Provides @Singleton
    fun provideFirebaseAuth(app: Application): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides @Singleton
    fun provideAuthDataSource(auth: FirebaseAuth , client: GoogleSignInClient): FirebaseAuthDataSource =
        FirebaseAuthDataSource(auth,client)

    @Provides @Singleton
    fun provideAuthRepository(ds: FirebaseAuthDataSource): AuthRepository =
        AuthRepositoryImpl(ds)
}