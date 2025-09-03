package com.mortex.composeRocket.game.presentation.components.screen.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.domain.model.GameCfg
import com.mortex.composeRocket.game.domain.model.Star
import com.mortex.composeRocket.game.domain.usecase.Collisions
import com.mortex.composeRocket.game.domain.usecase.InitStars
import com.mortex.composeRocket.game.domain.usecase.MaybeSpawnObstacle
import com.mortex.composeRocket.game.domain.usecase.MoveBullets
import com.mortex.composeRocket.game.domain.usecase.MoveObstacles
import com.mortex.composeRocket.game.domain.usecase.MoveStars
import com.mortex.composeRocket.game.domain.usecase.ShootIfReady
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RocketViewModel @Inject constructor(): ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private var loopJob: Job? = null

    fun onViewportReady(wPx: Int, hPx: Int) {
        // initialize stars & rocket pos
        _state.update {
            it.copy(
                width = wPx,
                height = hPx,
                rocketX = wPx / 2f,
                rocketY = hPx / 2f,
                stars = InitStars(wPx, hPx)
            )
        }
        startLoop()
    }

    fun onDrag(deltaX: Float, deltaY: Float) {
        _state.update { s ->
            if (s.gameOver) return@update s
            val rx = (s.rocketX + deltaX).coerceIn(GameCfg.ROCKET_HALF, s.width - GameCfg.ROCKET_HALF)
            val ry = (s.rocketY + deltaY).coerceIn(GameCfg.ROCKET_HALF, s.height - GameCfg.ROCKET_HALF)
            s.copy(
                rocketX = rx,
                rocketY = ry,
                tiltAngle = when {
                    deltaX > 0 -> 15f
                    deltaX < 0 -> -15f
                    else -> 0f
                },
                shooting = true
            )
        }
    }

    fun onDragEnd() {
        _state.update { it.copy(shooting = false, tiltAngle = 0f) }
    }

    fun onReplay() {
        val w = _state.value.width
        val h = _state.value.height
        _state.value = GameState(
            width = w, height = h,
            rocketX = w / 2f, rocketY = h / 2f,
            stars = InitStars(w, h)
        )
        startLoop()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()

                _state.update { s ->
                    if (s.gameOver || s.width == 0) return@update s

                    val (bullets, lastShot) = ShootIfReady(s, now)
                    val moved = s.copy(
                        stars = MoveStars(s.stars, s.height),
                        bullets = MoveBullets(bullets),
                        obstacles = MoveObstacles(
                            MaybeSpawnObstacle(s.obstacles, s.width, now, intArrayOf(
                                R.drawable.ic_stone, R.drawable.ic_stone_gray, R.drawable.ic_stone_red
                            )),
                            s.height
                        ),
                        lastShotMs = lastShot
                    )
                    Collisions(moved, now)
                }

                delay(16L)
            }
        }
    }

    override fun onCleared() {
        loopJob?.cancel()
        super.onCleared()
    }
}