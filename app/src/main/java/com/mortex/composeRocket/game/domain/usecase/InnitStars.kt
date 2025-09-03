package com.mortex.composeRocket.game.domain.usecase

import com.mortex.composeRocket.game.domain.model.Star

object InitStars {
    operator fun invoke(w: Int, h: Int, count: Int = 50) =
        List(count) {
            Star(
                x = (0..w).random().toFloat(),
                y = (0..h).random().toFloat(),
                speed = (1..6).random().toFloat()
            )
        }
}