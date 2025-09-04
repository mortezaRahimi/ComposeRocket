package com.mortex.composeRocket.game.presentation.components.screen.menu

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mortex.composeRocket.game.presentation.components.screen.login.LoginViewModel

@Composable
fun GameMenu(
    visible: Boolean,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit, // navigate back to main menu if you have one
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(),
        exit = scaleOut()
    ) {
        // Scrim behind the panel
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            // Click scrim to resume quickly (optional)
            Box(
                Modifier
                    .matchParentSize()
//                    .clickable { onResume() }
            )

            // Center panel
            ElevatedCard(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(min = 260.dp)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Paused", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Resume") }

                    Spacer(Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onRestart,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Restart") }

                    Spacer(Modifier.height(8.dp))

                    TextButton(
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Logout") }
                }
            }
        }
    }
}
