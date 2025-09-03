package com.mortex.composeRocket.game.domain.usecase

import android.graphics.RectF
import com.mortex.composeRocket.game.domain.model.Obstacle

object MoveObstacles {
    operator fun invoke(obstacles: List<Obstacle>, h: Int) =
        obstacles.map { ob ->
            ob.copy(
                rect = RectF(ob.rect.left, ob.rect.top + ob.fallSpeed, ob.rect.right, ob.rect.bottom + ob.fallSpeed),
                angle = (ob.angle + ob.rotationSpeed) % 360f
            )
        }.filter { it.rect.top < h }
}