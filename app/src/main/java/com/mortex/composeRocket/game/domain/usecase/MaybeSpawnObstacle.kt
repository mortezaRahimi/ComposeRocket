package com.mortex.composeRocket.game.domain.usecase

import android.graphics.RectF
import com.mortex.composeRocket.game.domain.model.GameCfg
import com.mortex.composeRocket.game.domain.model.Obstacle

object MaybeSpawnObstacle {
    operator fun invoke(obstacles: List<Obstacle>, w: Int, now: Long, obstacleImages: IntArray): List<Obstacle> {
        if ((0..100).random() >= 4) return obstacles
        val x = (100..(w - 200)).random().toFloat()
        val fast = (0..100).random() < 20
        val speed = if (fast) GameCfg.OBST_FAST else GameCfg.OBST_SLOW
        val img = obstacleImages.random()
        val id = now   // simple unique id; OK for a game
        val rect = RectF(x, 0f, x + GameCfg.OB_SIZE, GameCfg.OB_SIZE)
        return obstacles + Obstacle(
            id = id,
            rect = rect,
            angle = 0f,
            rotationSpeed = (2..6).random().toFloat(),
            fallSpeed = speed,
            health = 2,
            fast = fast,
            imageId = img
        )
    }
}