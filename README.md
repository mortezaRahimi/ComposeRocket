Building a Simple Arcade Shooter in Jetpack Compose

[![Let's Learn Together](http://img.youtube.com/vi/uzrzJvJSHgI/0.jpg)](https://youtube.com/shorts/uzrzJvJSHgI "Let's Learn Together")

Have you ever wanted to build a fun little game like Flappy Bird or Space Invaders, but using Jetpack Compose?

In this post, I’ll walk through how I built a casual Rocket Shooter Game — with a rocket, falling obstacles, bullets, stars, and a score system.

Let’s have some fun with Compose :)

Let’s go deep in code.

Data Models
data class Star(var x: Float, var y: Float, val speed: Float)
data class Bullet(var x: Float, var y: Float, var used: Boolean = false)
data class Obstacle(
    val rect: RectF,
    val health: Int = 2,
    val image: ImageBitmap,
    var angle: Float = 0f,
    val rotationSpeed: Float = listOf(-4f, -2f, 2f, 4f).random(),
    val fallSpeed: Float = 8f,
    val fast: Boolean = false,
    var lastHitTime: Long = 0L
)
👉 Why?

Star: for background animation.

Bullet: simple upward projectiles.

Obstacle: images that fall, spin, and need multiple hits before destruction.

Game State
var emojiX by remember { mutableStateOf(screenWidth / 2) }
var emojiY by remember { mutableStateOf(screenHeight / 2) }
var obstacles by remember { mutableStateOf(listOf<Obstacle>()) }
var bullets by remember { mutableStateOf(listOf<Bullet>()) }
var stars by remember { mutableStateOf(List(50) { ... }) }
var score by remember { mutableStateOf(0) }
var gameOver by remember { mutableStateOf(false) }
👉 Why?
We keep everything in Compose state, so the UI automatically redraws whenever the game changes.

Game Loop
LaunchedEffect(Unit) {
    while (true) {
        if (!gameOver) {
            // move stars vertically
            // move obstacles down
            // spawn new ones
            // shoot bullets on cooldown
            // check collisions
        }
        delay(16L) // ~60 FPS
    }
}
👉 Why?
This infinite coroutine simulates a 60 FPS loop — just like a game engine.

Player Control
detectDragGestures(
    onDragStart = { shooting = true },
    onDragEnd = { shooting = false; tiltAngle = 0f },
    onDrag = { change, dragAmount ->
        emojiX = (emojiX + dragAmount.x).coerceIn(60f, screenWidth - 60f)
        emojiY = (emojiY + dragAmount.y).coerceIn(60f, screenHeight - 60f)
        tiltAngle = if (dragAmount.x > 0) 15f else -15f
    }
)
👉 Why?
Drag to move the rocket. While holding → rocket auto-fires bullets. We also tilt the rocket slightly for a nice feel.

Drawing the Scene
Canvas {
    // stars (falling vertically)
    stars.forEach { drawCircle(Color.White, radius = 3f, center = Offset(it.x, it.y)) }

    // rocket (rotated when dragging)
    rotate(degrees = tiltAngle, pivot = Offset(emojiX, emojiY)) {
        drawImage(rocketBitmap, dstOffset = IntOffset((emojiX-60).toInt(), (emojiY-60).toInt()), dstSize = IntSize(200, 200))
    }

    // obstacles (spin + flash red on hit)
    obstacles.forEach { ob -> ... }

    // bullets (vector or PNG icons)
    bullets.forEach { b -> ... }
}
👉 Why?
We use Compose Canvas as our game screen: stars → rocket → obstacles → bullets.

Collision Logic
if (RectF.intersects(ob.rect, bulletRect)) {
    ob.health -= 1
    ob.lastHitTime = now
    if (ob.health <= 0) score++
}
👉 Why?

If a bullet hits → obstacle flashes red.

Needs 2 hits before being destroyed.

Score goes up only when fully destroyed.

Game Over & Replay
if (obstacles.any { RectF.intersects(it.rect, emojiRect) }) {
    gameOver = true
}
👉 Why?

If rocket touches any obstacle → Game Over.

Show “Replay” button to reset state and start fresh.

Final Touches
🚀 Drag the rocket to dodge and shoot.
⭐ Falling stars create a space effect.
⚡ Obstacles spin, flash, and fall at different speeds.
🔫 Hold drag → rocket fires bullets.
💀 Hit obstacle → Game Over + Replay.
Here you can find the project.

have fun ;) .



