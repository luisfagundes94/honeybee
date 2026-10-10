package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val SWIPE_TRASH_THRESHOLD = 0.50f
private const val SWIPE_TRASH_ANIMATION_MILLIS = 360
private const val MIN_MEDIA_DRAG_SCALE = 0.55f

internal class MediaSwipeState {
    var dragOffset by mutableFloatStateOf(0f)
        private set
    var isTrashing by mutableStateOf(false)
        private set
    var cardBounds by mutableStateOf<Rect?>(null)
    var trashBounds by mutableStateOf<Rect?>(null)
        private set
    val trashProgress = Animatable(0f)

    private var animationJob: Job? = null

    fun startDrag() {
        if (!isTrashing) animationJob?.cancel()
    }

    fun dragBy(amount: Float) {
        if (!isTrashing) dragOffset = (dragOffset + amount).coerceAtMost(0f)
    }

    fun hasReachedTrashThreshold(targetBounds: Rect): Boolean {
        return dragProgress(targetBounds) >= SWIPE_TRASH_THRESHOLD
    }

    fun dragScale(targetBounds: Rect): Float {
        val progress = (dragProgress(targetBounds) / SWIPE_TRASH_THRESHOLD).coerceIn(0f, 1f)
        return lerp(1f, MIN_MEDIA_DRAG_SCALE, progress)
    }

    private fun dragProgress(targetBounds: Rect): Float {
        val mediaBounds = cardBounds ?: return 0f
        val distanceToTrash = mediaBounds.center.y - targetBounds.center.y
        return if (distanceToTrash > 0f) -dragOffset / distanceToTrash else 0f
    }

    fun animateToTrash(
        targetBounds: Rect,
        coroutineScope: CoroutineScope,
        onSwipeUp: () -> Unit
    ) {
        if (isTrashing) return
        animationJob?.cancel()
        trashBounds = targetBounds
        isTrashing = true
        animationJob = coroutineScope.launch {
            trashProgress.animateTo(1f, tween(SWIPE_TRASH_ANIMATION_MILLIS))
            onSwipeUp()
        }
    }

    fun returnToStart(coroutineScope: CoroutineScope) {
        if (isTrashing) return
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            animate(dragOffset, 0f, animationSpec = spring()) { value, _ ->
                dragOffset = value.coerceAtMost(0f)
            }
        }
    }
}
