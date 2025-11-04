package com.mortex.composeRocket.game.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mortex.composeRocket.game.presentation.components.screen.login.LoginScreen
import com.mortex.composeRocket.game.presentation.components.screen.game.RocketScreen
import com.mortex.composeRocket.game.presentation.components.screen.menu.GameMenu
import com.mortex.composeRocket.game.presentation.components.screen.splash.SplashScreen
import com.mortex.composeRocket.game.presentation.navigation.Route
import com.mortex.composeRocket.game.ui.theme.ComposeRocketTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RocketActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ComposeRocketTheme {
                Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier.padding(
                            paddingValues = innerPadding
                        )
                    ) {

                        val nav = rememberNavController()
                        NavHost(navController = nav, startDestination = Route.Splash.path) {

                            composable(Route.Splash.path) {
                                SplashScreen(
                                    onFinished = {
                                        nav.navigate(Route.Auth.path)
                                        {
                                            popUpTo(Route.Splash.path) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                )
                            }


                            composable(Route.Auth.path) {
                                LoginScreen(
                                    onSignedIn = {
                                        nav.navigate(Route.Game.path)
                                        {
                                            popUpTo(Route.Game.path) { inclusive = true }
                                            launchSingleTop = true
                                        }
                                    },
                                )
                            }

                            composable(Route.Game.path) {
                                RocketScreen(onLogout = {
                                    nav.navigate(Route.Auth.path) {
                                        popUpTo(Route.Auth.path) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                })

                            }

                        }
                    }
                }
            }
        }


    }

    override fun onDestroy() {
        super.onDestroy()
    }
}