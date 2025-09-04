package com.mortex.composeRocket.game.domain.game.usecase

import com.mortex.composeRocket.game.domain.game.model.Star

object MoveStars {
    operator fun invoke(stars: List<Star>, h: Int) =
        stars.map { s -> s.copy(y = if (s.y + s.speed > h) 0f else s.y + s.speed) }
}