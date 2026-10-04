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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val PHOTO_ZOOM_RESET_THRESHOLD = 1.05f
private const val PHOTO_DOUBLE_TAP_SCALE = 3f

private data class PhotoViewerGestureState(
    val scale: Animatable<Float, *>,
    val offsetX: Animatable<Float, *>,
    val offsetY: Animatable<Float, *>,
    val viewportSize: IntSize
)

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
    val gestureState = remember(scale, offsetX, offsetY, maxWidth, maxHeight) {
        PhotoViewerGestureState(
            scale = scale,
            offsetX = offsetX,
            offsetY = offsetY,
            viewportSize = IntSize(maxWidth, maxHeight)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .photoDoubleTapGesture(
                gestureState = gestureState,
                coroutineScope = coroutineScope,
                onDismissRequest = onDismissRequest
            )
            .photoTransformGesture(
                gestureState = gestureState,
                coroutineScope = coroutineScope
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
    gestureState: PhotoViewerGestureState,
    coroutineScope: CoroutineScope,
    onDismissRequest: () -> Unit
) = pointerInput(Unit) {
    detectTapGestures(
        onDoubleTap = { tapOffset ->
            coroutineScope.launch {
                if (gestureState.scale.value > PHOTO_ZOOM_RESET_THRESHOLD) {
                    launch { gestureState.scale.animateTo(1f, spring()) }
                    launch { gestureState.offsetX.animateTo(0f, spring()) }
                    launch { gestureState.offsetY.animateTo(0f, spring()) }
                } else {
                    animatePhotoZoom(
                        gestureState = gestureState,
                        tapOffset = tapOffset
                    )
                }
            }
        },
        onTap = { onDismissRequest() }
    )
}

private fun kotlinx.coroutines.CoroutineScope.animatePhotoZoom(
    gestureState: PhotoViewerGestureState,
    tapOffset: Offset
) {
    val centerX = gestureState.viewportSize.width / 2f
    val centerY = gestureState.viewportSize.height / 2f
    val dx = tapOffset.x - centerX
    val dy = tapOffset.y - centerY
    val extraWidth = (PHOTO_DOUBLE_TAP_SCALE - 1) * gestureState.viewportSize.width
    val extraHeight = (PHOTO_DOUBLE_TAP_SCALE - 1) * gestureState.viewportSize.height
    val maxX = extraWidth / 2f
    val maxY = extraHeight / 2f
    val targetOffsetX = (-dx * (PHOTO_DOUBLE_TAP_SCALE - 1)).coerceIn(-maxX, maxX)
    val targetOffsetY = (-dy * (PHOTO_DOUBLE_TAP_SCALE - 1)).coerceIn(-maxY, maxY)

    launch { gestureState.scale.animateTo(PHOTO_DOUBLE_TAP_SCALE, spring()) }
    launch { gestureState.offsetX.animateTo(targetOffsetX, spring()) }
    launch { gestureState.offsetY.animateTo(targetOffsetY, spring()) }
}

private fun Modifier.photoTransformGesture(
    gestureState: PhotoViewerGestureState,
    coroutineScope: CoroutineScope
) = pointerInput(Unit) {
    detectTransformGestures { _, pan, zoom, _ ->
        coroutineScope.launch {
            val newScale = (gestureState.scale.value * zoom).coerceIn(1f, 5f)
            gestureState.scale.snapTo(newScale)
            val maxX = (newScale - 1) * gestureState.viewportSize.width / 2f
            val maxY = (newScale - 1) * gestureState.viewportSize.height / 2f
            gestureState.offsetX.snapTo(
                (gestureState.offsetX.value + pan.x).coerceIn(-maxX, maxX)
            )
            gestureState.offsetY.snapTo(
                (gestureState.offsetY.value + pan.y).coerceIn(-maxY, maxY)
            )
        }
    }
}
