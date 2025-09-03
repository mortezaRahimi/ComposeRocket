package com.mortex.composeRocket.game.domain.model

import android.graphics.RectF
import androidx.compose.ui.graphics.ImageBitmap

data class Obstacle(
    val id: Long,
    val rect: RectF,
    val angle: Float,
    val rotationSpeed: Float,
    val fallSpeed: Float,
    val health: Int,
    val fast: Boolean,
    val lastHitTime: Long = 0L,
    val imageId: Int // reference to drawable id; UI resolves bitmap
)