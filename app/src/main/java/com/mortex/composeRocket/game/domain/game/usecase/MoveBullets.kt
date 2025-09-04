package com.mortex.composeRocket.game.domain.game.usecase

import com.mortex.composeRocket.game.domain.game.model.Bullet
import com.mortex.composeRocket.game.domain.game.model.GameCfg

object MoveBullets {
    operator fun invoke(bullets: List<Bullet>) =
        bullets.map { it.copy(y = it.y - GameCfg.BULLET_SPEED) }
            .filter { it.y > 0f }
}