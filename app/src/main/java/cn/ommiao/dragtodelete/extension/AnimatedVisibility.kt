package cn.ommiao.dragtodelete.extension

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun slideInFromBottom(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { it }
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun slideOutToBottom(): ExitTransition {
    return slideOutVertically(
        targetOffsetY = { it }
    )
}
