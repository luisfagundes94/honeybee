package com.luisfagundes.library.impl.presentation.components

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import android.view.LayoutInflater
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.luisfagundes.library.impl.R

@android.annotation.SuppressLint("InflateParams")
@OptIn(UnstableApi::class)
@Composable
internal fun VideoPlayer(
    videoUri: android.net.Uri,
    isPageSelected: Boolean,
    modifier: Modifier = Modifier,
    onVideoSizeChanged: ((aspectRatio: Float) -> Unit)? = null
) {
    val context = LocalContext.current

    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
            
            addListener(object : Player.Listener {
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    if (videoSize.width > 0 && videoSize.height > 0) {
                        val isRotated = videoSize.unappliedRotationDegrees == 90 || videoSize.unappliedRotationDegrees == 270
                        val width = if (isRotated) videoSize.height else videoSize.width
                        val height = if (isRotated) videoSize.width else videoSize.height
                        onVideoSizeChanged?.invoke(width.toFloat() / height.toFloat())
                    }
                }
            })
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPageSelected) {
        if (isPageSelected) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    AndroidView(
        factory = { ctx ->
            (LayoutInflater.from(ctx).inflate(R.layout.view_video_player, null) as PlayerView).apply {
                useController = true
                controllerAutoShow = false
                hideController()
            }
        },
        update = { playerView ->
            playerView.player = exoPlayer
        },
        modifier = modifier.fillMaxSize()
    )
}
