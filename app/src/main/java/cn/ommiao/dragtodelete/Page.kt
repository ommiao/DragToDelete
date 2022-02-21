package cn.ommiao.dragtodelete

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cn.ommiao.dragtodelete.data.Item
import cn.ommiao.dragtodelete.data.itemsList
import cn.ommiao.dragtodelete.extension.toColor
import cn.ommiao.dragtodelete.ui.theme.DragToDeleteTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import kotlinx.coroutines.delay

val statusBarColor = Color(0xFFF1F1F1)

const val TAG = "log-ommiao"

val itemSize = 56.dp

val globalPositionsMap = mutableMapOf<Item, Offset>()

@Composable
fun Page() {
    SystemUiController()
    DragToDeleteTheme {
        ProvideWindowInsets {
            val insets = LocalWindowInsets.current
            val statusBarHeight = with(LocalDensity.current) {
                insets.statusBars.top.toDp()
            }
            val dragState: MutableState<DragState> = remember {
                mutableStateOf(DragState.Idle)
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Column {
                    MyAppBar(statusBarHeight)
                    ItemsList(dragState)
                }
                ActiveItem(dragState)
            }
        }
    }
}

@Composable
private fun ActiveItem(dragState: MutableState<DragState>) {
    val activeIndex = dragState.value.index
    val scale = animateFloatAsState(
        targetValue = when (dragState.value) {
            is DragState.Dragging -> 1.25f
            is DragState.DraggingToIdle -> 1.0f
            DragState.Idle -> 1.0f
            is DragState.IdleToDragging -> 1.25f
        },
        animationSpec = tween(300)
    )
    if (activeIndex > -1) {
        Log.d(TAG, "ActiveItem: scale -> ${scale.value}")
        val activeItem = itemsList[activeIndex]
        val animateOffsetX = animateIntAsState(
            targetValue = dragState.value.offset.x.toInt()
        )
        val animateOffsetY = animateIntAsState(
            targetValue = dragState.value.offset.y.toInt()
        )
        Item(
            color = activeItem.color.toColor(),
            modifier = Modifier
                .offset {
                    IntOffset(
                        if (dragState.value is DragState.DraggingToIdle) animateOffsetX.value else dragState.value.offset.x.toInt(),
                        if (dragState.value is DragState.DraggingToIdle) animateOffsetY.value else dragState.value.offset.y.toInt()
                    )
                }
                .scale(scale.value)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemsList(dragState: MutableState<DragState>) {
    LazyVerticalGrid(
        cells = GridCells.Fixed(4),
        modifier = Modifier.navigationBarsPadding(),
        contentPadding = PaddingValues(16.dp)
    ) {
        val activeIndex = dragState.value.index
        items(itemsList.size) { index ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.alpha(if (activeIndex == index) 0f else 1f)
            ) {
                val item = itemsList[index]
                Item(
                    color = item.color.toColor(),
                    modifier = Modifier
                        .onGloballyPositioned {
                            globalPositionsMap += item to it.positionInWindow()
                        }
                        .pointerInput(Unit) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = {
                                    Log.d(TAG, "onDragStart: $it")
                                    dragState.value = DragState.IdleToDragging(
                                        index = index,
                                        offset = globalPositionsMap[item] ?: Offset(0f, 0f)
                                    )
                                },
                                onDragEnd = {
                                    Log.d(TAG, "onDragEnd")
                                    dragState.value = DragState.DraggingToIdle(
                                        index = index,
                                        offset = globalPositionsMap[item] ?: Offset(0f, 0f)
                                    )
                                },
                                onDragCancel = {
                                    Log.d(TAG, "onDragCancel")
                                }
                            ) { change, dragAmount ->
                                dragState.value =
                                    DragState.Dragging(
                                        index = index,
                                        offset = dragAmount + dragState.value.offset
                                    )
                                change.consumeAllChanges()
                                Log.d(TAG, "onDrag: $dragAmount")
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun Item(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .size(itemSize)
            .background(color)
    )
}

@Composable
private fun MyAppBar(statusBarHeight: Dp) {
    TopAppBar(
        contentPadding = PaddingValues(top = statusBarHeight),
        backgroundColor = statusBarColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Android")
        }
    }
}

@Composable
private fun SystemUiController() {
    val systemUiController = rememberSystemUiController()
    val darkIcons = true
    SideEffect {
        systemUiController.setStatusBarColor(color = Color.Transparent, darkIcons = darkIcons)
    }
}

sealed class DragState(open val index: Int, open val offset: Offset) {
    object Idle : DragState(-1, Offset(0f, 0f))
    data class IdleToDragging(override val index: Int, override val offset: Offset) :
        DragState(index, offset)

    data class Dragging(override val index: Int, override val offset: Offset) :
        DragState(index, offset)

    data class DraggingToIdle(override val index: Int, override val offset: Offset) :
        DragState(index, offset)
}