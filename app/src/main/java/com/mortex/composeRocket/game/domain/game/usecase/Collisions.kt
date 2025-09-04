package com.mortex.composeRocket.game.domain.game.usecase

import android.graphics.RectF
import com.mortex.composeRocket.game.domain.game.model.GameState


object Collisions {
    operator fun invoke(state: GameState, now: Long): GameState {
        val emojiRect = RectF(state.rocketX - 60, state.rocketY - 60, state.rocketX + 60, state.rocketY + 60)
        if (state.obstacles.any { RectF.intersects(it.rect, emojiRect) }) {
            return state.copy(gameOver = true)
        }

        val obs = state.obstacles.toMutableList()
        val bul = state.bullets.toMutableList()
        var score = state.score

        // bullet vs obstacle
        for (i in obs.indices) {
            val ob = obs[i]
            for (j in bul.indices) {
                val b = bul[j]
                val bRect = RectF(b.x - 10, b.y - 20, b.x + 10, b.y)
                if (RectF.intersects(ob.rect, bRect)) {
                    obs[i] = ob.copy(health = ob.health - 1, lastHitTime = now)
                    bul[j] = b.copy(y = -9999f) // mark for removal
                    break
                }
            }
        }
        val survivors = obs.filter {
            if (it.health <= 0) { score++; false } else true
        }
        val bullets = bul.filter { it.y > 0 }

        return state.copy(score = score, obstacles = survivors, bullets = bullets)
    }
}