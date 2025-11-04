package com.mortex.composeRocket.game.presentation.components.screen.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.util.toRange
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.core.ext.randomFloat
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val rocket = ImageBitmap.imageResource(R.drawable.ic_red_rocket)

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    // Rocket Y position
    val rocketY = remember { Animatable(0f) }

    // Simple flame particles: keep random seeds for flicker
    val flames = remember {
        List(20) {
            FlameParticle(
                offsetX = (-20..20).random().toFloat(),
                baseSize = (8..20).random().toFloat()
            )
        }
    }

    LaunchedEffect(Unit) {
        val heightPx = with(density) { screenHeight.toPx() }
        rocketY.snapTo(heightPx + 200f)

        // Animate rocket moving up
        rocketY.animateTo(
            targetValue = -200f,
            animationSpec = tween(
                durationMillis = 2000,
                easing = LinearEasing
            )
        )

        delay(200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.TopCenter
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val rocketX = size.width / 2
            val rocketTop = rocketY.value

            // --- Draw flames ---
            flames.forEach { f ->
                // flicker effect by using frame time
                val flicker = (5..15).random().toFloat()
                val sizeF = f.baseSize + flicker
                val alpha = (0.4f..0.9f).toRange().randomFloat()

                drawCircle(
                    color = Color(0xFFFFA500).copy(alpha = alpha), // orange
                    radius = sizeF,
                    center = Offset(
                        x = rocketX + f.offsetX,
                        y = rocketTop + rocket.height + 30f + (5..15).random()
                    )
                )
                drawCircle(
                    color = Color.Red.copy(alpha = alpha * 0.7f),
                    radius = sizeF / 2,
                    center = Offset(
                        x = rocketX + f.offsetX / 2,
                        y = rocketTop + rocket.height + 50f + (10..20).random()
                    )
                )
            }

            // --- Draw rocket ---
            drawImage(
                image = rocket,
                dstOffset = IntOffset(
                    (rocketX - rocket.width / 2).toInt(),
                    rocketTop.toInt()
                )
            )
        }
    }
}

private data class FlameParticle(
    val offsetX: Float,
    val baseSize: Float
)

