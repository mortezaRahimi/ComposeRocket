package com.mortex.composeRocket.game.presentation.navigation

sealed class Route(val path: String) {
    data object Auth : Route("AUTH")
    data object Game : Route("GAME")
    data object Menu : Route("MENU")
    data object Splash : Route("SPLASH")
}