package com.mortex.composeRocket.game.domain.game.usecase

import com.mortex.composeRocket.game.domain.game.model.Bullet
import com.mortex.composeRocket.game.domain.game.model.GameCfg
import com.mortex.composeRocket.game.domain.game.model.GameState

object ShootIfReady {
    operator fun invoke(state: GameState, now: Long): Pair<List<Bullet>, Long> {
        if (!state.shooting) return state.bullets to state.lastShotMs
        if (now - state.lastShotMs < GameCfg.FIRE_RATE_MS) return state.bullets to state.lastShotMs
        val b = Bullet(id = now, x = state.rocketX, y = state.rocketY - 60f)
        return (state.bullets + b) to now
    }
}