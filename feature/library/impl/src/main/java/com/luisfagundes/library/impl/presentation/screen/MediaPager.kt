package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.components.VideoPlayer
import kotlinx.coroutines.CoroutineScope

internal fun Modifier.mediaSwipeGesture(
    mediaId: Long,
    swipeState: MediaSwipeState,
    trashIconBounds: Rect?,
    coroutineScope: CoroutineScope,
    onSwipeUp: () -> Unit
) = pointerInput(mediaId, swipeState, trashIconBounds) {
    detectVerticalDragGestures(
        onDragStart = { swipeState.startDrag() },
        onDragEnd = { swipeState.returnToStart(coroutineScope) },
        onDragCancel = { swipeState.returnToStart(coroutineScope) },
        onVerticalDrag = { change, dragAmount ->
            if (dragAmount < 0 || swipeState.dragOffset < 0) {
                change.consume()
                swipeState.dragBy(dragAmount)
                if (trashIconBounds != null && swipeState.hasReachedTrashThreshold(trashIconBounds)) {
                    swipeState.animateToTrash(trashIconBounds, coroutineScope, onSwipeUp)
                }
            }
        }
    )
}

@Composable
internal fun MediaPagerCard(
    media: Media,
    isPageSelected: Boolean,
    aspectRatio: Float?,
    swipeState: MediaSwipeState,
    trashIconBounds: Rect?,
    overlayModifier: Modifier,
    onAspectRatioChanged: (Float) -> Unit,
    onPhotoClick: () -> Unit
) {
    val largeCornerShape = RoundedCornerShape(MaterialTheme.spacing.largeCorner)
    val sizeModifier = aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize()
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default)
    ) {
        Box(
            modifier = sizeModifier.onGloballyPositioned {
                swipeState.cardBounds = Rect(it.positionInRoot(), it.size.toSize())
            }
        ) {
            Card(
                shape = largeCornerShape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
                ),
                modifier = overlayModifier.mediaPagerCardModifier(
                    swipeState = swipeState,
                    trashIconBounds = trashIconBounds,
                    shape = largeCornerShape
                )
            ) {
                MediaPagerMedia(
                    media = media,
                    isPageSelected = isPageSelected,
                    onAspectRatioChanged = onAspectRatioChanged,
                    onPhotoClick = onPhotoClick
                )
            }
        }
    }
}

private fun Modifier.mediaPagerCardModifier(
    swipeState: MediaSwipeState,
    trashIconBounds: Rect?,
    shape: RoundedCornerShape
): Modifier {
    return fillMaxSize().graphicsLayer {
        translationY = swipeState.dragOffset
        val mediaBounds = swipeState.cardBounds
        val targetBounds = swipeState.trashBounds ?: trashIconBounds
        val dragScale = targetBounds?.let(swipeState::dragScale) ?: 1f
        scaleX = dragScale
        scaleY = dragScale
        if (mediaBounds != null && targetBounds != null) {
            val progress = swipeState.trashProgress.value
            val targetScale = minOf(
                targetBounds.width / mediaBounds.width,
                targetBounds.height / mediaBounds.height
            ).coerceIn(0f, 1f)
            scaleX = lerp(dragScale, targetScale, progress)
            scaleY = scaleX
            translationX = (targetBounds.center.x - mediaBounds.center.x) * progress
            translationY = lerp(
                swipeState.dragOffset,
                targetBounds.center.y - mediaBounds.center.y,
                progress
            )
        }
        this.shape = shape
        clip = true
    }
}

@Composable
private fun MediaPagerMedia(
    media: Media,
    isPageSelected: Boolean,
    onAspectRatioChanged: (Float) -> Unit,
    onPhotoClick: () -> Unit
) {
    if (media.isVideo) {
        VideoPlayer(
            videoUri = media.uri.toUri(),
            isPageSelected = isPageSelected,
            modifier = Modifier.fillMaxSize(),
            onVideoSizeChanged = onAspectRatioChanged
        )
    } else {
        AsyncImage(
            model = media.uri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            onSuccess = { state ->
                val size = state.painter.intrinsicSize
                if (size.width > 0 && size.height > 0) {
                    onAspectRatioChanged(size.width / size.height)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onPhotoClick
                )
        )
    }
}
