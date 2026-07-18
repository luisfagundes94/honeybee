package com.luisfagundes.library.impl.presentation.tools

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.Fetcher
import coil.imageLoader
import coil.request.Options

internal data class MediaStoreThumbnailRequest(
    val uri: Uri,
    val widthPx: Int,
    val heightPx: Int
)

@RequiresApi(Build.VERSION_CODES.Q)
private class MediaStoreThumbnailFetcher(
    private val request: MediaStoreThumbnailRequest,
    private val options: Options
) : Fetcher {

    override suspend fun fetch(): DrawableResult {
        val bitmap = options.context.contentResolver.loadThumbnail(
            request.uri,
            Size(request.widthPx, request.heightPx),
            null
        )
        return DrawableResult(
            drawable = BitmapDrawable(options.context.resources, bitmap),
            isSampled = true,
            dataSource = DataSource.DISK
        )
    }

    class Factory : Fetcher.Factory<MediaStoreThumbnailRequest> {
        override fun create(
            data: MediaStoreThumbnailRequest,
            options: Options,
            imageLoader: ImageLoader
        ): Fetcher? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStoreThumbnailFetcher(data, options)
            } else {
                null
            }
        }
    }
}

@Composable
internal fun rememberMediaStoreThumbnailImageLoader(): ImageLoader {
    val context = LocalContext.current
    val imageLoader = context.imageLoader
    return remember(context, imageLoader) {
        imageLoader.newBuilder()
            .components {
                add(MediaStoreThumbnailFetcher.Factory())
            }
            .build()
    }
}
