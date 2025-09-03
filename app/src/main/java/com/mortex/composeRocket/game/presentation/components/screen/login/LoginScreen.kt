package com.mortex.composeRocket.game.presentation.components.screen.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.mortex.composeRocket.R
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onSignedIn: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {

    val ui by viewModel.ui.collectAsState()

    val googleClient = rememberGoogleSignInClient(R.string.default_web_client_id)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { res ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(res.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) viewModel.onGoogleCredential(idToken)
        } catch (e: ApiException) {
            // user cancelled or error
        }
    }

    // Navigate once signed in
    LaunchedEffect(ui.isSignedIn) {
        if (ui.isSignedIn) {
            delay(2000)
            onSignedIn()
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when {
            ui.loading -> CircularProgressIndicator()
            ui.isSignedIn -> {
                Row {
                    AsyncImage(
                        model = ui.photoUrl,
                        contentDescription = ui.userName
                    )
                    Text("Welcome ${ui.userName}")
                }
            }
            else -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ui.error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                }
                Button(onClick = { launcher.launch(googleClient.signInIntent) }) {
                    Text("Sign in with Google")
                }
            }
        }
    }

}