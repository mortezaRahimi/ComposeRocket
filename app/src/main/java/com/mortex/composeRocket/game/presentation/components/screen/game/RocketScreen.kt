package com.mortex.composeRocket.game.presentation.components.screen.game

import android.graphics.Paint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.size.Scale
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.domain.game.model.GameCfg
import com.mortex.composeRocket.game.presentation.components.screen.menu.GameMenu

@Composable
fun RocketScreen(
    viewModel: RocketViewModel = hiltViewModel(),
    onLogout: () -> Unit,
) {

    val ui by viewModel.state.collectAsState()

    // Load images once; ViewModel only exposes drawable IDs
    val rocketBitmap = ImageBitmap.imageResource(R.drawable.rocket_transparent)
    val bulletPainter = painterResource(R.drawable.ic_bullet)
    val obstacleBitmaps = mapOf(
        R.drawable.ic_stone to ImageBitmap.imageResource(R.drawable.ic_stone),
        R.drawable.ic_stone_gray to ImageBitmap.imageResource(R.drawable.ic_stone_gray),
        R.drawable.ic_stone_red to ImageBitmap.imageResource(R.drawable.ic_stone_red),
    )

    var measured by remember { mutableStateOf(false) }

    BackHandler(enabled = true) {
        if (!ui.gameOver)
            viewModel.openMenu()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .onSizeChanged {
                if (!measured) {
                    measured = true
                    viewModel.onViewportReady(it.width, it.height)
                }
            }
            // Only capture gestures when NOT paused/menu
            .then(
                if (!ui.isPaused && !ui.isMenuOpen)
                    Modifier.pointerInput(ui.gameOver) {
                        if (!ui.gameOver) {
                            detectDragGestures(
                                onDrag = { change, drag ->
                                    change.consume()
                                    viewModel.onDrag(drag.x, drag.y)
                                },
                                onDragEnd = { viewModel.onDragEnd() },
                                onDragCancel = { viewModel.onDragEnd() }
                            )
                        }
                    }
                else Modifier
            )
    ) {
        // Canvas render only
        Canvas(Modifier.fillMaxSize()) {
            // stars
            ui.stars.forEach { drawCircle(White, 3f, Offset(it.x, it.y)) }

            // rocket
            if (!ui.gameOver) {
                rotate(ui.tiltAngle, pivot = Offset(ui.rocketX, ui.rocketY)) {
                    drawImage(
                        rocketBitmap,
                        dstOffset = IntOffset(
                            (ui.rocketX - GameCfg.ROCKET_HALF).toInt(),
                            (ui.rocketY - GameCfg.ROCKET_HALF).toInt()
                        ),
                        dstSize = IntSize(200, 200)
                    )
                }
            } else {
                drawContext.canvas.nativeCanvas.drawText(
                    "💀", ui.rocketX, ui.rocketY,
                    Paint().apply {
                        textSize = 120f; color = android.graphics.Color.YELLOW; textAlign =
                        Paint.Align.CENTER
                    }
                )
            }

            // obstacles
            val now = System.currentTimeMillis()
            ui.obstacles.forEach { ob ->
                val cx = ob.rect.left + ob.rect.width() / 2
                val cy = ob.rect.top + ob.rect.height() / 2
                val isHitFlash = (now - ob.lastHitTime) < 20L
                val img = obstacleBitmaps[ob.imageId]!!
                rotate(ob.angle, pivot = Offset(cx, cy)) {
                    drawImage(
                        img,
                        dstOffset = IntOffset(ob.rect.left.toInt(), ob.rect.top.toInt()),
                        dstSize = IntSize(ob.rect.width().toInt(), ob.rect.height().toInt()),
                        colorFilter = when {
                            isHitFlash -> ColorFilter.tint(Red)
                            ob.fast -> ColorFilter.lighting(multiply = White, add = Color.Gray)
                            else -> null
                        }
                    )
                }
            }

            // bullets
            ui.bullets.forEach { b ->
                with(bulletPainter) {
                    translate(b.x - 20f, b.y - 20f) { draw(size = Size(90f, 90f)) }
                }
            }
        }

        // HUD: score + pause button
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                shape = CircleShape, modifier = Modifier
                    .size(45.dp)
            ) {
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = ui.user?.photoUrl ?: "",
                    contentDescription = ui.user?.email,
                    contentScale = ContentScale.Fit
                )
            }
            Text("Score: ${ui.score}", color = White, fontSize = 20.sp)
        }

        // Pause button (hidden when already showing menu or game over)
        if (!ui.gameOver) {
            FilledTonalButton(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd), onClick = viewModel::openMenu
            ) {
                Text("MENU")
            }
        }

        // Game Over overlay (unchanged)
        if (ui.gameOver) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("GAME OVER", color = Red, fontSize = 36.sp)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { viewModel.restartGame() }) { Text("Replay") }
            }
        }

        // --- Animated Menu overlay ---
        GameMenu(
            visible = ui.isMenuOpen,
            onResume = viewModel::closeMenu,
            onRestart = viewModel::restartGame,
            onExit = {
                viewModel.onExit()
                onLogout()
            }
        )
    }
}

