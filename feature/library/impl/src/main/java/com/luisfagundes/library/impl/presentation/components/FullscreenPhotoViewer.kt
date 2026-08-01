package com.luisfagundes.library.impl.presentation.components

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import kotlinx.coroutines.launch

private const val PHOTO_ZOOM_RESET_THRESHOLD = 1.05f
private const val PHOTO_DOUBLE_TAP_SCALE = 3f

@Composable
internal fun FullscreenPhotoViewer(
    photoUri: Uri,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        PhotoViewerSurface(
            photoUri = photoUri,
            onDismissRequest = onDismissRequest,
            modifier = modifier
        )
    }
}

@Composable
private fun PhotoViewerSurface(
    photoUri: Uri,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim)
    ) {
        val scale = remember { Animatable(1f) }
        val offsetX = remember { Animatable(0f) }
        val offsetY = remember { Animatable(0f) }

        PhotoViewerImage(
            photoUri = photoUri,
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            coroutineScope = coroutineScope,
            onDismissRequest = onDismissRequest,
            maxWidth = constraints.maxWidth,
            maxHeight = constraints.maxHeight
        )
        PhotoViewerCloseButton(
            onDismissRequest = onDismissRequest,
            modifier = Modifier.align(Alignment.TopStart)
        )
    }
}

@Composable
private fun PhotoViewerImage(
    photoUri: Uri,
    scale: Animatable<Float, *>,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onDismissRequest: () -> Unit,
    maxWidth: Int,
    maxHeight: Int
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .photoDoubleTapGesture(
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                coroutineScope = coroutineScope,
                maxWidth = maxWidth,
                maxHeight = maxHeight,
                onDismissRequest = onDismissRequest
            )
            .photoTransformGesture(
                scale = scale,
                offsetX = offsetX,
                offsetY = offsetY,
                coroutineScope = coroutineScope,
                maxWidth = maxWidth,
                maxHeight = maxHeight
            )
    ) {
        AsyncImage(
            model = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale.value,
                    scaleY = scale.value,
                    translationX = offsetX.value,
                    translationY = offsetY.value
                )
        )
    }
}

@Composable
private fun PhotoViewerCloseButton(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onDismissRequest,
        modifier = modifier
            .statusBarsPadding()
            .padding(MaterialTheme.spacing.default)
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f), CircleShape)
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = stringResource(R.string.close),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Modifier.photoDoubleTapGesture(
    scale: Animatable<Float, *>,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    maxWidth: Int,
    maxHeight: Int,
    onDismissRequest: () -> Unit
) = pointerInput(Unit) {
    detectTapGestures(
        onDoubleTap = { tapOffset ->
            coroutineScope.launch {
                if (scale.value > PHOTO_ZOOM_RESET_THRESHOLD) {
                    launch { scale.animateTo(1f, spring()) }
                    launch { offsetX.animateTo(0f, spring()) }
                    launch { offsetY.animateTo(0f, spring()) }
                } else {
                    animatePhotoZoom(
                        scale = scale,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        tapX = tapOffset.x,
                        tapY = tapOffset.y,
                        maxWidth = maxWidth,
                        maxHeight = maxHeight,
                        coroutineScope = this
                    )
                }
            }
        },
        onTap = { onDismissRequest() }
    )
}

private fun kotlinx.coroutines.CoroutineScope.animatePhotoZoom(
    scale: Animatable<Float, *>,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    tapX: Float,
    tapY: Float,
    maxWidth: Int,
    maxHeight: Int,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val centerX = maxWidth / 2f
    val centerY = maxHeight / 2f
    val dx = tapX - centerX
    val dy = tapY - centerY
    val extraWidth = (PHOTO_DOUBLE_TAP_SCALE - 1) * maxWidth
    val extraHeight = (PHOTO_DOUBLE_TAP_SCALE - 1) * maxHeight
    val maxX = extraWidth / 2f
    val maxY = extraHeight / 2f
    val targetOffsetX = (-dx * (PHOTO_DOUBLE_TAP_SCALE - 1)).coerceIn(-maxX, maxX)
    val targetOffsetY = (-dy * (PHOTO_DOUBLE_TAP_SCALE - 1)).coerceIn(-maxY, maxY)

    coroutineScope.launch { scale.animateTo(PHOTO_DOUBLE_TAP_SCALE, spring()) }
    coroutineScope.launch { offsetX.animateTo(targetOffsetX, spring()) }
    coroutineScope.launch { offsetY.animateTo(targetOffsetY, spring()) }
}

private fun Modifier.photoTransformGesture(
    scale: Animatable<Float, *>,
    offsetX: Animatable<Float, *>,
    offsetY: Animatable<Float, *>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    maxWidth: Int,
    maxHeight: Int
) = pointerInput(Unit) {
    detectTransformGestures { _, pan, zoom, _ ->
        coroutineScope.launch {
            val newScale = (scale.value * zoom).coerceIn(1f, 5f)
            scale.snapTo(newScale)
            val maxX = (newScale - 1) * maxWidth / 2f
            val maxY = (newScale - 1) * maxHeight / 2f
            offsetX.snapTo((offsetX.value + pan.x).coerceIn(-maxX, maxX))
            offsetY.snapTo((offsetY.value + pan.y).coerceIn(-maxY, maxY))
        }
    }
}
