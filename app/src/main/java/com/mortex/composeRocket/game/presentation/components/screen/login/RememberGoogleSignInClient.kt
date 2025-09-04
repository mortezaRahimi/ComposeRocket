package com.mortex.composeRocket.game.presentation.components.screen.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@Composable
fun rememberGoogleSignInClient(
    webClientIdRes: Int
): GoogleSignInClient {
    val ctx = LocalContext.current
    // This builder call is @Composable-safe via remember
    val gso = remember {
        GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken(ctx.getString(webClientIdRes))
            .requestEmail()
            .build()
    }
    return remember { GoogleSignIn.getClient(ctx, gso) }
}