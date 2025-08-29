package com.mortex.composeRocket.game

import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.mortex.ComposeRocket.R

data class Star(var x: Float, var y: Float, val speed: Float)
data class Bullet(var x: Float, var y: Float, var used: Boolean = false)
data class Obstacle(
    val rect: RectF,
    val health: Int = 2,
    val image: ImageBitmap,
    var angle: Float = 0f,
    val rotationSpeed: Float = listOf(-4f, -2f, 2f, 4f).random(),
    val fallSpeed: Float = 8f,
    val fast: Boolean = false,             // mark fast ones
    var lastHitTime: Long = 0L             // for red flash
)

@Composable
fun Game(padding: PaddingValues) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp.toPx()
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp.toPx()

    var emojiX by remember { mutableFloatStateOf(screenWidth / 2) }
    var emojiY by remember { mutableFloatStateOf(screenHeight / 2) }

    var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
    var bullets by remember { mutableStateOf(listOf<Bullet>()) }
    var stars by remember {
        mutableStateOf(
            List(50) {
                Star(
                    (0..screenWidth.toInt()).random().toFloat(),
                    (0..screenHeight.toInt()).random().toFloat(),
                    speed = (1..6).random().toFloat() // parallax speeds
                )
            }
        )
    }

    var gameOver by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var shooting by remember { mutableStateOf(false) }
    var tiltAngle by remember { mutableStateOf(0f) }

    val bulletPainter = painterResource(id = R.drawable.ic_bullet)
    val rocketBitmap = ImageBitmap.imageResource(id = R.drawable.ic_red_rocket)
    val obstacleImages = listOf(
        ImageBitmap.imageResource(id = R.drawable.ic_stone),
        ImageBitmap.imageResource(id = R.drawable.ic_stone_gray),
        ImageBitmap.imageResource(id = R.drawable.ic_stone_red),
    )

    var lastShotTime by remember { mutableLongStateOf(0L) }
    val fireRateMs = 150L

    // Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            if (!gameOver) {
                // 🌌 Move stars
                stars = stars.map {
                    val newY = it.y + it.speed
                    if (newY > screenHeight) {
                        Star((0..screenWidth.toInt()).random().toFloat(), 0f, it.speed) // respawn at top
                    } else {
                        it.copy(y = newY)
                    }
                }

                // ⬇️ Move obstacles
                obstacles = obstacles.map { ob ->
                    ob.copy(
                        rect = RectF(
                            ob.rect.left,
                            ob.rect.top + ob.fallSpeed,
                            ob.rect.right,
                            ob.rect.bottom + ob.fallSpeed
                        ),
                        angle = (ob.angle + ob.rotationSpeed) % 360f
                    )
                }.filter { it.rect.top < screenHeight }

                // Spawn obstacles
                if ((0..100).random() < 4) {
                    val size = 170f
                    val xPos = (100..(screenWidth - 200).toInt()).random().toFloat()
                    val img = obstacleImages.random()
                    val fast = (0..100).random() < 20
                    val speed = if (fast) 16f else 8f

                    obstacles = obstacles + Obstacle(
                        rect = RectF(xPos, 0f, xPos + size, size),
                        health = 2,
                        image = img,
                        fallSpeed = speed,
                        fast = fast
                    )
                }

                // 🔫 Shoot bullets
                val now = System.currentTimeMillis()
                if (shooting && now - lastShotTime > fireRateMs) {
                    bullets = bullets + Bullet(emojiX, emojiY - 60)
                    lastShotTime = now
                }

                // Move bullets
                bullets = bullets.map { it.copy(y = it.y - 15f) }
                    .filter { it.y > 0 }

                // 🚨 Collision check
                val emojiRect = RectF(emojiX - 60, emojiY - 60, emojiX + 60, emojiY + 60)
                if (obstacles.any { RectF.intersects(it.rect, emojiRect) }) {
                    gameOver = true
                }

                // ✅ Bullet vs Obstacle
                val updatedObstacles = obstacles.toMutableList()
                val updatedBullets = bullets.toMutableList()

                for (i in updatedObstacles.indices) {
                    val ob = updatedObstacles[i]
                    for (j in updatedBullets.indices) {
                        val b = updatedBullets[j]
                        if (!b.used) {
                            val bulletRect = RectF(b.x - 10, b.y - 20, b.x + 10, b.y)
                            if (RectF.intersects(ob.rect, bulletRect)) {
                                updatedObstacles[i] = ob.copy(
                                    health = ob.health - 1,
                                    lastHitTime = now
                                )
                                updatedBullets[j] = b.copy(used = true)
                            }
                        }
                    }
                }

                // Remove destroyed
                val survivors = updatedObstacles.filter {
                    if (it.health <= 0) {
                        score++
                        false
                    } else true
                }
                bullets = updatedBullets.filter { !it.used }
                obstacles = survivors
            }
            delay(16L)
        }
    }

    // UI + Canvas
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(Black)
            .pointerInput(gameOver) {
                if (!gameOver) {
                    detectDragGestures(
                        onDragStart = { shooting = true },
                        onDragEnd = { shooting = false; tiltAngle = 0f },
                        onDragCancel = { shooting = false; tiltAngle = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            emojiX = (emojiX + dragAmount.x).coerceIn(60f, screenWidth - 60f)
                            emojiY = (emojiY + dragAmount.y).coerceIn(60f, screenHeight - 60f)
                            tiltAngle = when {
                                dragAmount.x > 0 -> 15f
                                dragAmount.x < 0 -> -15f
                                else -> 0f
                            }
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 🌌 Stars
            stars.forEach { star ->
                drawCircle(White, radius = 3f, center = Offset(star.x, star.y))
            }

            // 🚀 Player
            if (!gameOver) {
                rotate(degrees = tiltAngle, pivot = Offset(emojiX, emojiY)) {
                    drawImage(
                        image = rocketBitmap,
                        dstOffset = IntOffset((emojiX - 60f).toInt(), (emojiY - 60f).toInt()),
                        dstSize = IntSize(200, 200)
                    )
                }
            } else {
                drawContext.canvas.nativeCanvas.apply {
                    drawText("💀", emojiX, emojiY, Paint().apply {
                        textSize = 120f
                        color = android.graphics.Color.YELLOW
                        textAlign = Paint.Align.CENTER
                    })
                }
            }

            // ⬇️ Obstacles
            val now = System.currentTimeMillis()
            obstacles.forEach { ob ->
                val centerX = ob.rect.left + ob.rect.width() / 2
                val centerY = ob.rect.top + ob.rect.height() / 2
                val flashDuration = 20L
                val isHitFlash = (now - ob.lastHitTime) < flashDuration

                rotate(degrees = ob.angle, pivot = Offset(centerX, centerY)) {
                    drawImage(
                        image = ob.image,
                        dstOffset = IntOffset(ob.rect.left.toInt(), ob.rect.top.toInt()),
                        dstSize = IntSize(ob.rect.width().toInt(), ob.rect.height().toInt()),
                        colorFilter = when {
                            isHitFlash -> ColorFilter.tint(Red)
                            ob.fast -> ColorFilter.lighting(
                                multiply = Color.White,
                                add = Color.Gray
                            ) // fast ones tinted
                            else -> null
                        }
                    )
                }
            }

            // 🔥 Bullets
            bullets.forEach { b ->
                with(bulletPainter) {
                    translate(left = b.x - 20f, top = b.y - 20f) {
                        draw(size = Size(90f, 90f))
                    }
                }
            }
        }

        // 🏆 Score
        Text(
            "Score: $score",
            color = White,
            fontSize = 24.sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        )

        // 💀 Game Over
        if (gameOver) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("GAME OVER", color = Red, fontSize = 36.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = {
                    emojiX = screenWidth / 2
                    emojiY = screenHeight / 2
                    obstacles = emptyList()
                    bullets = emptyList()
                    gameOver = false
                    score = 0
                    shooting = false
                    tiltAngle = 0f
                    lastShotTime = 0L
                    stars = List(50) {
                        Star(
                            (0..screenWidth.toInt()).random().toFloat(),
                            (0..screenHeight.toInt()).random().toFloat(),
                            speed = (1..6).random().toFloat()
                        )
                    }
                }) {
                    Text("Replay")
                }
            }
        }
    }
}

@Composable
fun Dp.toPx(): Float {
    val density = LocalDensity.current
    return with(density) { this@toPx.toPx() }
}



