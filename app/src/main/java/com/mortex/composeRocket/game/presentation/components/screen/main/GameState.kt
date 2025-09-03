package com.mortex.composeRocket.game.presentation.components.screen.main

import com.mortex.composeRocket.game.domain.model.Bullet
import com.mortex.composeRocket.game.domain.model.Obstacle
import com.mortex.composeRocket.game.domain.model.Star


data class GameState(
    val width: Int = 0,
    val height: Int = 0,
    val rocketX: Float = 0f,
    val rocketY: Float = 0f,
    val tiltAngle: Float = 0f,
    val stars: List<Star> = emptyList(),
    val bullets: List<Bullet> = emptyList(),
    val obstacles: List<Obstacle> = emptyList(),
    val score: Int = 0,
    val gameOver: Boolean = false,
    val shooting: Boolean = false,
    val lastShotMs: Long = 0L
)
