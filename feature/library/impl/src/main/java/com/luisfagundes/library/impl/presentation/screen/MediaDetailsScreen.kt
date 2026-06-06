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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.presentation.viewmodel.MediaDetailsViewModel
import com.luisfagundes.library.impl.presentation.tools.formatPhotoDate
import com.luisfagundes.library.impl.presentation.tools.formatPhotoSize
import kotlinx.coroutines.launch
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper

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

@OptIn(ExperimentalFoundationApi::class)
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

    // Calculate current visible photo based on swiping direction logic:
    // Swiping right-to-left shows next photo, left-to-right shows previous photo.
    val currentPage = pagerState.currentPage
    val currentPhotoIndex = currentPage.coerceIn(0, (totalCount - 1).coerceAtLeast(0))
    val currentPhoto = photos.getOrNull(currentPhotoIndex)

    val percent =
        if (totalCount > 0) (content.trashCount * 100) / (totalCount + content.trashCount) else 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediaDetailsTopAppBar(
                currentPhotoIndex = currentPhotoIndex,
                totalCount = totalCount,
                percent = percent,
                trashCount = content.trashCount,
                onBackClick = onBackClick,
                onEvent = onEvent
            )
        },
        bottomBar = {
            currentPhoto?.let { photo ->
                MediaDetailsBottomBar(photo = photo)
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
            photos.getOrNull(pagePhotoIndex)?.let { photo ->
                val isFavorite = content.favoritePhotoIds.contains(photo.id)
                MediaPagerItem(
                    photo = photo,
                    isFavorite = isFavorite,
                    onEvent = onEvent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaDetailsTopAppBar(
    currentPhotoIndex: Int,
    totalCount: Int,
    percent: Int,
    trashCount: Int,
    onBackClick: () -> Unit,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(R.string.all_photos),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        R.string.media_details_progress_format,
                        currentPhotoIndex + 1,
                        totalCount,
                        percent
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
            }
        },
        actions = {
            TrashBadgedBox(
                itemsInTrash = trashCount,
                onClick = { onEvent(MediaDetailsUiEvent.TrashClick) },
                contentDescription = stringResource(R.string.items_in_trash),
                modifier = Modifier.padding(end = MaterialTheme.spacing.default)
            )
        }
    )
}

@Composable
private fun MediaDetailsBottomBar(
    photo: Photo,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.large)
    ) {
        val formattedDate = formatPhotoDate(photo.dateAdded)
        val formattedSize = formatPhotoSize(photo.size)
        Text(
            text = stringResource(
                R.string.media_details_info_format,
                formattedDate,
                formattedSize
            ),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaPagerItem(
    photo: Photo,
    isFavorite: Boolean,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var aspectRatio by remember(photo.id) { mutableStateOf<Float?>(null) }
    val swipeOffset = remember(photo.id) { Animatable(0f) }
    val swipeLimit = -350f
    var showBottomSheet by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
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
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default)
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = (if (aspectRatio != null) {
                    Modifier.aspectRatio(aspectRatio!!)
                } else {
                    Modifier.fillMaxSize()
                })
                    .graphicsLayer {
                        translationY = swipeOffset.value
                        alpha = (1f + (swipeOffset.value / 1200f)).coerceIn(0.2f, 1f)
                        shape = RoundedCornerShape(24.dp)
                        clip = true
                    }
            ) {
                AsyncImage(
                    model = photo.uri,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    onSuccess = { state ->
                        val size = state.painter.intrinsicSize
                        if (size.width > 0 && size.height > 0) {
                            aspectRatio = size.width / size.height
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        MediaPagerItemActionsColumn(
            photo = photo,
            isFavorite = isFavorite,
            onEvent = onEvent,
            onInfoClick = { showBottomSheet = true },
            modifier = Modifier.align(Alignment.BottomEnd)
        )

        if (showBottomSheet) {
            PhotoInfoBottomSheet(
                photo = photo,
                onDismissRequest = { showBottomSheet = false }
            )
        }
    }
}

@Composable
private fun MediaPagerItemActionsColumn(
    photo: Photo,
    isFavorite: Boolean,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(MaterialTheme.spacing.default)
            .background(Color.Black.copy(alpha = 0.3f), CircleShape)
            .padding(MaterialTheme.spacing.verySmall)
    ) {
        val sharePhotoTitle = stringResource(R.string.share_photo)

        IconButton(
            onClick = onInfoClick
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = stringResource(R.string.info),
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
                        sharePhotoTitle
                    )
                )
            }
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = stringResource(R.string.share),
                tint = Color.White
            )
        }
        IconButton(
            onClick = { onEvent(MediaDetailsUiEvent.ToggleFavorite(photo.id)) }
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = stringResource(R.string.favorite),
                tint = if (isFavorite) Color.Red else Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhotoInfoBottomSheet(
    photo: Photo,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        PhotoInfoBottomSheetContent(photo = photo)
    }
}

@Composable
private fun PhotoInfoBottomSheetContent(
    photo: Photo,
    modifier: Modifier = Modifier
) {
    val formattedDate = formatPhotoDate(photo.dateAdded)
    val formattedSize = formatPhotoSize(photo.size)
    val photoUriString = photo.uri.toString()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.default)
            .padding(bottom = MaterialTheme.spacing.large)
    ) {
        Text(
            text = stringResource(R.string.info),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.default)
        )

        InfoRow(
            icon = Icons.Outlined.Info,
            label = "ID",
            value = photo.id.toString()
        )

        InfoRow(
            icon = Icons.Outlined.CalendarToday,
            label = "Date added",
            value = formattedDate
        )

        InfoRow(
            icon = Icons.Outlined.Storage,
            label = "File size",
            value = formattedSize
        )

        InfoRow(
            icon = Icons.Outlined.Link,
            label = "URI",
            value = photoUriString
        )
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.spacing.small)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(end = MaterialTheme.spacing.default)
                .size(24.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun MediaPagerPreview() {
    MediaPager(
        content = MediaDetailsUiState.Content(
            photos = listOf(
                Photo(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                Photo(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L),
                Photo(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L)
            ),
            initialIndex = 0,
            trashCount = 2,
            favoritePhotoIds = setOf()
        ),
        onEvent = {},
        onBackClick = {}
    )
}
