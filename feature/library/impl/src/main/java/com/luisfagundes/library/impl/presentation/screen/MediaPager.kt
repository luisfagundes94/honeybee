package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.core.net.toUri
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.components.VideoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val SWIPE_LIMIT = -350f
private const val SWIPE_DISMISS_OFFSET = -1_500f
private const val SWIPE_DISMISS_ANIMATION_MILLIS = 300
private const val SWIPE_ALPHA_DISTANCE = 1_200f
private const val MIN_MEDIA_ALPHA = 0.2f

internal fun Modifier.mediaSwipeGesture(
    mediaId: Long,
    swipeOffset: androidx.compose.animation.core.Animatable<Float, *>,
    coroutineScope: CoroutineScope,
    onSwipeUp: () -> Unit
) = pointerInput(mediaId) {
    detectVerticalDragGestures(
        onDragEnd = {
            coroutineScope.launch {
                if (swipeOffset.value < SWIPE_LIMIT) {
                    swipeOffset.animateTo(
                        SWIPE_DISMISS_OFFSET,
                        tween(SWIPE_DISMISS_ANIMATION_MILLIS)
                    )
                    onSwipeUp()
                } else {
                    swipeOffset.animateTo(0f, spring())
                }
            }
        },
        onVerticalDrag = { change, dragAmount ->
            if (dragAmount < 0 || swipeOffset.value < 0) {
                change.consume()
                coroutineScope.launch {
                    swipeOffset.snapTo(swipeOffset.value + dragAmount)
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
    swipeOffset: androidx.compose.animation.core.Animatable<Float, *>,
    onAspectRatioChanged: (Float) -> Unit,
    onPhotoClick: () -> Unit
) {
    val largeCornerShape = RoundedCornerShape(MaterialTheme.spacing.largeCorner)
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default)
    ) {
        Card(
            shape = largeCornerShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0f)
            ),
            modifier = mediaPagerCardModifier(
                aspectRatio = aspectRatio,
                swipeOffset = swipeOffset,
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

private fun mediaPagerCardModifier(
    aspectRatio: Float?,
    swipeOffset: androidx.compose.animation.core.Animatable<Float, *>,
    shape: RoundedCornerShape
): Modifier {
    val sizeModifier = aspectRatio?.let { Modifier.aspectRatio(it) } ?: Modifier.fillMaxSize()
    return sizeModifier.graphicsLayer {
        translationY = swipeOffset.value
        alpha = (1f + (swipeOffset.value / SWIPE_ALPHA_DISTANCE)).coerceIn(MIN_MEDIA_ALPHA, 1f)
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
