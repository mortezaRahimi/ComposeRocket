package com.mortex.composeRocket.game.presentation.components.screen.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mortex.composeRocket.R
import com.mortex.composeRocket.game.domain.auth.repository.AuthRepository
import com.mortex.composeRocket.game.domain.game.model.GameCfg
import com.mortex.composeRocket.game.domain.game.model.GameState
import com.mortex.composeRocket.game.domain.game.usecase.Collisions
import com.mortex.composeRocket.game.domain.game.usecase.InitStars
import com.mortex.composeRocket.game.domain.game.usecase.MaybeSpawnObstacle
import com.mortex.composeRocket.game.domain.game.usecase.MoveBullets
import com.mortex.composeRocket.game.domain.game.usecase.MoveObstacles
import com.mortex.composeRocket.game.domain.game.usecase.MoveStars
import com.mortex.composeRocket.game.domain.game.usecase.ShootIfReady
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
class RocketViewModel @Inject constructor(private val authRepository: AuthRepository): ViewModel() {
    private val _state = MutableStateFlow(GameState())
    val state: StateFlow<GameState> = _state

    private var loopJob: Job? = null


    init {
        _state.update { it.copy(user = authRepository.currentUser()) }
    }
    fun openMenu() {
        _state.update { it.copy(isPaused = true, isMenuOpen = true) }
    }
    fun closeMenu() {
        _state.update { it.copy(isMenuOpen = false, isPaused = false) }
    }
    fun pause() {
        _state.update { it.copy(isPaused = true) }
    }
    fun resume() {
        _state.update { it.copy(isPaused = false) }
    }

    fun onExit(){
        viewModelScope.launch {
            authRepository.signOut()
        }

    }

    fun restartGame() {
        val w = _state.value.width
        val h = _state.value.height
        _state.value = _state.value.copy(
            // re-init the gameplay fields as you already do in your Replay
            width = w,
            height = h,
            rocketX = w / 2f,
            rocketY = h / 2f,
            stars = InitStars(w, h),
            bullets = emptyList(),
            obstacles = emptyList(),
            score = 0,
            tiltAngle = 0f,
            gameOver = false,
            isPaused = false,
            isMenuOpen = false,
            lastShotMs = 0L
        )
        // ensure loop keeps running; if you stop it elsewhere, call startLoop() here
    }


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
                val s = _state.value

                // Skip updating when paused or game over (but keep the loop responsive)
                if (s.isPaused || s.gameOver || s.width == 0) {
                    delay(16L)
                    continue
                }

                val now = System.currentTimeMillis()

                _state.update { cur ->
                    val (bullets, lastShot) = ShootIfReady(cur, now)
                    val progressed = cur.copy(
                        stars = MoveStars(cur.stars, cur.height),
                        bullets = MoveBullets(bullets),
                        obstacles = MoveObstacles(
                            MaybeSpawnObstacle(
                                cur.obstacles,
                                cur.width,
                                now,
                                intArrayOf(
                                    R.drawable.ic_stone,
                                    R.drawable.ic_stone_gray,
                                    R.drawable.ic_stone_red
                                )
                            ),
                            cur.height
                        ),
                        lastShotMs = lastShot
                    )
                    Collisions(progressed, now)
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