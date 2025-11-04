package com.mortex.composeRocket.game.core.ext

import android.util.Range


fun Range<Float>.randomFloat(): Float {
    val list = listOf(lower..upper)
    return list.random().start

}