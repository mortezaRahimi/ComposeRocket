package com.mortex.composeRocket.game.data.repository

import com.mortex.composeRocket.game.domain.model.Star
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class Repository {

    fun getStars(screenWidth: Float, screenHeight: Float): Flow<List<Star>> = flow {
        emit(
            List(50) {
                Star(
                    (0..screenWidth.toInt()).random().toFloat(),
                    (0..screenHeight.toInt()).random().toFloat(),
                    speed = (1..6).random().toFloat() // parallax speeds
                )
            }
        )
    }.flowOn(Dispatchers.Default)
}