package com.mortex.composeRocket.game.domain.auth

sealed class AuthFailure(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    object Canceled : AuthFailure("Sign-in canceled")
    object Network : AuthFailure("Network error")
     class Unknown(cause: Throwable? = null) : AuthFailure("Unknown auth error", cause)
}