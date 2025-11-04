package com.mortex.composeRocket.game.presentation.components.screen.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.domain.auth.AuthFailure
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.jvm.java

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {

    val user by viewModel.userFlow.collectAsStateWithLifecycle() // emits current user or null


    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(user) {
        if (user != null) {
            isLoading = false
            error = null
            onSignedIn()
        }
    }

    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken.isNullOrBlank()) {
                isLoading = false
                error = "No ID token returned"
            } else {
                // Hand off to ViewModel (Firebase Auth)
                isLoading = true
                error = null

                // fire-and-forget; ViewModel updates userFlow on success
                // if you want inline error reporting, await the Result in a coroutine:
                viewModel.viewModelScope.launch {
                    val r = viewModel.signInWithIdToken(idToken)
                    r.onFailure { e ->
                        isLoading = false
                        error = when (e) {
                            is AuthFailure.Canceled -> "Sign-in canceled"
                            is AuthFailure.Network -> "Network error"
                            else -> e.message ?: "Sign-in failed"
                        }
                    }
                }
            }
        } catch (e: ApiException) {
            isLoading = false
            // 12501 = SIGN_IN_CANCELLED
            error = if (e.statusCode == 12501) "Sign-in canceled" else (e.localizedMessage
                ?: "Google sign-in failed")
        }
    }


    Scaffold(
        snackbarHost = {
            SnackbarHost(remember { SnackbarHostState() }) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                // Sign-in button
                Button(
                    onClick = {
                        error = null
                        isLoading = true
                        signInLauncher.launch(viewModel.getGoogleClient().signInIntent)
                    },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .padding(end = 8.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    Text("Continue with Google")
                }

                // Optional secondary actions
                // TextButton(onClick = { /* privacy policy */ }) { Text("Privacy Policy") }

                Spacer(Modifier.height(12.dp))
                error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

}