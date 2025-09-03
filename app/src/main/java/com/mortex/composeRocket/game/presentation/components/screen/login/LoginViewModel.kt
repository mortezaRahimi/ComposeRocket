package com.mortex.composeRocket.game.presentation.components.screen.login

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState(loading = true))
    val ui: StateFlow<AuthUiState> = _ui

    private val auth = Firebase.auth

    init {
        auth.currentUser?.let { u ->
            _ui.value = AuthUiState(
                loading = false,
                userName = u.displayName,
                photoUrl = u.photoUrl?.toString(),
                isSignedIn = true
            )
        } ?: run { _ui.value = AuthUiState(loading = false) }
    }

    fun onGoogleCredential(idToken: String) {
        _ui.update { it.copy(loading = true, error = null) }
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val u = auth.currentUser
                    _ui.value = AuthUiState(
                        loading = false,
                        userName = u?.displayName,
                        photoUrl = u?.photoUrl?.toString(),
                        isSignedIn = true
                    )
                } else {
                    _ui.update {
                        it.copy(
                            loading = false,
                            error = task.exception?.localizedMessage ?: "Sign-in failed",
                            isSignedIn = false
                        )
                    }
                }
            }
    }

    fun signOut(onDone: () -> Unit = {}) {
        auth.signOut()
        _ui.value = AuthUiState()
        onDone()
    }
}