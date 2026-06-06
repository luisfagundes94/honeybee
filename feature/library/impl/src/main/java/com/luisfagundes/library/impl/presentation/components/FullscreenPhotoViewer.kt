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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import kotlinx.coroutines.launch

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
        val coroutineScope = rememberCoroutineScope()
        BoxWithConstraints(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            val scale = remember { Animatable(1f) }
            val offsetX = remember { Animatable(0f) }
            val offsetY = remember { Animatable(0f) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapOffset ->
                                coroutineScope.launch {
                                    if (scale.value > 1.05f) {
                                        launch { scale.animateTo(1f, spring()) }
                                        launch { offsetX.animateTo(0f, spring()) }
                                        launch { offsetY.animateTo(0f, spring()) }
                                    } else {
                                        val targetScale = 3f
                                        val centerX = constraints.maxWidth / 2f
                                        val centerY = constraints.maxHeight / 2f
                                        val dx = tapOffset.x - centerX
                                        val dy = tapOffset.y - centerY

                                        val extraWidth = (targetScale - 1) * constraints.maxWidth
                                        val extraHeight = (targetScale - 1) * constraints.maxHeight
                                        val maxX = extraWidth / 2f
                                        val maxY = extraHeight / 2f

                                        val targetOffsetX = (-dx * (targetScale - 1)).coerceIn(-maxX, maxX)
                                        val targetOffsetY = (-dy * (targetScale - 1)).coerceIn(-maxY, maxY)

                                        launch { scale.animateTo(targetScale, spring()) }
                                        launch { offsetX.animateTo(targetOffsetX, spring()) }
                                        launch { offsetY.animateTo(targetOffsetY, spring()) }
                                    }
                                }
                            },
                            onTap = {
                                onDismissRequest()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            coroutineScope.launch {
                                val newScale = (scale.value * zoom).coerceIn(1f, 5f)
                                scale.snapTo(newScale)

                                val extraWidth = (newScale - 1) * constraints.maxWidth
                                val extraHeight = (newScale - 1) * constraints.maxHeight
                                val maxX = extraWidth / 2f
                                val maxY = extraHeight / 2f

                                val newOffsetX = (offsetX.value + pan.x).coerceIn(-maxX, maxX)
                                val newOffsetY = (offsetY.value + pan.y).coerceIn(-maxY, maxY)

                                offsetX.snapTo(newOffsetX)
                                offsetY.snapTo(newOffsetY)
                            }
                        }
                    }
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

                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .padding(MaterialTheme.spacing.default)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = Color.White
                    )
                }
            }
        }
    }
}
