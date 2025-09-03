package com.mortex.composeRocket.game.presentation.components.screen.login

data class AuthUiState(
    val loading: Boolean = false,
    val userName: String? = null,
    val photoUrl: String? = null,
    val error: String? = null,
    val isSignedIn: Boolean = false
)