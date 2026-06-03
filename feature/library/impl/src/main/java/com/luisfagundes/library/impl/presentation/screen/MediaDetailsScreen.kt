package com.luisfagundes.library.impl.presentation.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
import com.luisfagundes.library.impl.presentation.viewmodel.MediaDetailsViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

@Composable
internal fun MediaDetailsScreen(
    initialPhotoId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: MediaDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            MediaDetailsUiEffect.NavigateBack -> onNavigateBack()
            MediaDetailsUiEffect.NavigateToTrash -> onNavigateToTrash()
        }
    }

    LaunchedEffect(initialPhotoId) {
        viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialPhotoId))
    }

    MediaDetailsContent(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaDetailsContent(
    uiState: MediaDetailsUiState,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    when (uiState) {
        is MediaDetailsUiState.Loading -> HoneybeeLoadingTemplate(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        is MediaDetailsUiState.Error -> HoneybeeErrorTemplate(
            message = uiState.message,
            onRetry = { /* Managed by key launch */ }
        )

        is MediaDetailsUiState.Content -> MediaPager(
            content = uiState,
            onEvent = onEvent,
            onBackClick = onBackClick
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun MediaPager(
    content: MediaDetailsUiState.Content,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val photos = content.photos
    val totalCount = photos.size
    val initialPage = content.initialIndex.coerceIn(0, (totalCount - 1).coerceAtLeast(0))

    val pagerState = rememberPagerState(initialPage = initialPage) { totalCount }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Calculate current visible photo based on swiping direction logic:
    // Swiping right-to-left shows next photo, left-to-right shows previous photo.
    val currentPage = pagerState.currentPage
    val currentPhotoIndex = currentPage.coerceIn(0, (totalCount - 1).coerceAtLeast(0))
    val currentPhoto = photos[currentPhotoIndex]

    val percent =
        if (totalCount > 0) (content.trashCount * 100) / (totalCount + content.trashCount) else 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "All Photos",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentPhotoIndex + 1}/$totalCount • $percent%",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    TrashBadgedBox(
                        itemsInTrash = content.trashCount,
                        onClick = { onEvent(MediaDetailsUiEvent.TrashClick) },
                        contentDescription = stringResource(R.string.items_in_trash),
                        modifier = Modifier.padding(end = MaterialTheme.spacing.default)
                    )
                }
            )
        },
        bottomBar = {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.large)
            ) {
                val formattedDate = formatPhotoDate(currentPhoto.dateAdded)
                val formattedSize = formatPhotoSize(currentPhoto.size)
                Text(
                    text = "$formattedDate • $formattedSize",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val pagePhotoIndex = page.coerceIn(0, (totalCount - 1).coerceAtLeast(0))
            val photo = photos[pagePhotoIndex]

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                val swipeOffset = remember(photo.id) { Animatable(0f) }
                val swipeLimit = -350f

                Box(
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default)
                ) {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                translationY = swipeOffset.value
                                alpha = (1f + (swipeOffset.value / 1200f)).coerceIn(0.2f, 1f)
                                shape = RoundedCornerShape(24.dp)
                                clip = true
                            }
                            .pointerInput(photo.id) {
                                detectVerticalDragGestures(
                                    onDragEnd = {
                                        if (swipeOffset.value < swipeLimit) {
                                            coroutineScope.launch {
                                                swipeOffset.animateTo(-1500f, tween(300))
                                                onEvent(MediaDetailsUiEvent.SwipeUp(photo.id))
                                            }
                                        } else {
                                            coroutineScope.launch {
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
                    ) {
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(MaterialTheme.spacing.default)
                            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            .padding(MaterialTheme.spacing.verySmall)
                    ) {
                        val isFavorite = content.favoritePhotoIds.contains(photo.id)

                        IconButton(
                            onClick = {
                                Toast.makeText(
                                    context,
                                    "File size: ${formatPhotoSize(photo.size)}\nDate added: ${
                                        formatPhotoDate(
                                            photo.dateAdded
                                        )
                                    }",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Info",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "image/*"
                                    putExtra(Intent.EXTRA_STREAM, photo.uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Share photo"
                                    )
                                )
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = { onEvent(MediaDetailsUiEvent.ToggleFavorite(photo.id)) }
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color.Red else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}


private fun formatPhotoDate(epochSeconds: Long): String {
    return runCatching {
        val instant = Instant.ofEpochSecond(epochSeconds)
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy • h:mm a", Locale.ENGLISH)
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    }.getOrDefault("")
}

private fun formatPhotoSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()
    return runCatching {
        String.format(
            Locale.US,
            "%.1f %s",
            bytes / 1024.0.pow(digitGroups.toDouble()),
            units[digitGroups]
        )
    }.getOrDefault("$bytes B")
}
