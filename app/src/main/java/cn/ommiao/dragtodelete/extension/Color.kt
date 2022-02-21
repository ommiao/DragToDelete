package cn.ommiao.dragtodelete.extension

import androidx.compose.ui.graphics.Color

fun String.toColor(): Color {
    return Color("ff$this".toLong(16))
}
