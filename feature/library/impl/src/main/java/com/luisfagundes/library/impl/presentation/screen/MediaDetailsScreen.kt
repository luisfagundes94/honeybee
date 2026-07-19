package com.luisfagundes.library.impl.presentation.screen

import android.content.Intent
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R as DesignSystemResources
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.components.FullscreenPhotoViewer
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import com.luisfagundes.library.impl.presentation.components.VideoPlayer
import com.luisfagundes.library.impl.presentation.effect.MediaDetailsUiEffect
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent
import com.luisfagundes.library.impl.presentation.provider.MediaDetailsUiStateProvider
import com.luisfagundes.library.impl.presentation.state.MediaDetailsUiState
import com.luisfagundes.library.impl.presentation.tools.formatPhotoDate
import com.luisfagundes.library.impl.presentation.tools.formatPhotoSize
import com.luisfagundes.library.impl.presentation.tools.getFriendlyFileType
import com.luisfagundes.library.impl.presentation.viewmodel.MediaDetailsViewModel
import kotlinx.coroutines.launch

@Composable
internal fun MediaDetailsScreen(
    initialMediaId: Long,
    albumId: String? = null,
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

    LaunchedEffect(initialMediaId, albumId) {
        viewModel.dispatchEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId, albumId))
    }

    MediaDetailsScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        initialMediaId = initialMediaId
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun MediaDetailsScreen(
    uiState: MediaDetailsUiState,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    initialMediaId: Long,
) {
    when (uiState) {
        is MediaDetailsUiState.Loading -> HoneybeeLoadingTemplate()

        is MediaDetailsUiState.Error -> HoneybeeErrorTemplate(
            description = stringResource(R.string.failed_to_load_photo_details),
            primaryButtonLabel = stringResource(DesignSystemResources.string.retry),
            onPrimaryButtonClick = { onEvent(MediaDetailsUiEvent.LoadDetails(initialMediaId)) },
            secondaryButtonLabel = stringResource(DesignSystemResources.string.cancel),
            onSecondaryButtonClick = { onEvent(MediaDetailsUiEvent.CancelClick) },
        )

        is MediaDetailsUiState.Content -> MediaDetailsContent(
            content = uiState,
            onEvent = onEvent
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MediaDetailsContent(
    content: MediaDetailsUiState.Content,
    onEvent: (MediaDetailsUiEvent) -> Unit,
) {
    val mediaList = content.mediaList
    val totalCount = mediaList.size
    val initialPage = content.initialIndex.coerceIn(0, (totalCount - 1).coerceAtLeast(0))

    val pagerState = rememberPagerState(initialPage = initialPage) { totalCount }

    val currentPage = pagerState.currentPage
    val currentMediaIndex = currentPage.coerceIn(0, (totalCount - 1).coerceAtLeast(0))
    val currentMedia = mediaList.getOrNull(currentMediaIndex)

    val percent =
        if (totalCount > 0) (content.trashCount * 100) / (totalCount + content.trashCount) else 0

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            MediaDetailsTopAppBar(
                currentMediaIndex = currentMediaIndex,
                totalCount = totalCount,
                percent = percent,
                trashCount = content.trashCount,
                onBackClick = { onEvent(MediaDetailsUiEvent.BackClick) },
                onEvent = onEvent
            )
        },
        bottomBar = {
            currentMedia?.let { media ->
                MediaDetailsBottomBar(media = media)
            }
        }
    ) { innerPadding ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { page ->
            val pageMediaIndex = page.coerceIn(0, (totalCount - 1).coerceAtLeast(0))
            mediaList.getOrNull(pageMediaIndex)?.let { media ->
                val isPageSelected = page == pagerState.currentPage

                MediaPagerItem(
                    media = media,
                    isPageSelected = isPageSelected,
                    onEvent = onEvent
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaDetailsTopAppBar(
    currentMediaIndex: Int,
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
                        currentMediaIndex + 1,
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
    media: Media,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = MaterialTheme.spacing.default)
    ) {
        val formattedDate = formatPhotoDate(media.dateAdded)
        val formattedSize = formatPhotoSize(media.size)
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
    media: Media,
    isPageSelected: Boolean,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var aspectRatio by remember(media.id) {
        mutableStateOf(
            if (media.width > 0 && media.height > 0) {
                media.width.toFloat() / media.height.toFloat()
            } else null
        )
    }
    val swipeOffset = remember(media.id) { Animatable(0f) }
    val swipeLimit = -350f
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFullscreenPhoto by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .pointerInput(media.id) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (swipeOffset.value < swipeLimit) {
                            coroutineScope.launch {
                                swipeOffset.animateTo(-1500f, tween(300))
                                onEvent(MediaDetailsUiEvent.SwipeUp(media.id))
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
                if (media.isVideo) {
                    VideoPlayer(
                        videoUri = media.uri,
                        isPageSelected = isPageSelected,
                        modifier = Modifier.fillMaxSize(),
                        onVideoSizeChanged = { ratio ->
                            aspectRatio = ratio
                        }
                    )
                } else {
                    AsyncImage(
                        model = media.uri,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        onSuccess = { state ->
                            val size = state.painter.intrinsicSize
                            if (size.width > 0 && size.height > 0) {
                                aspectRatio = size.width / size.height
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showFullscreenPhoto = true
                            }
                    )
                }
            }
        }

        MediaPagerItemActionsColumn(
            media = media,
            onInfoClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.default)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        )

        if (showBottomSheet) {
            MediaInfoBottomSheet(
                media = media,
                onDismissRequest = { showBottomSheet = false }
            )
        }

        if (showFullscreenPhoto) {
            FullscreenPhotoViewer(
                photoUri = media.uri,
                onDismissRequest = { showFullscreenPhoto = false }
            )
        }
    }
}

@Composable
private fun MediaPagerItemActionsColumn(
    media: Media,
    onInfoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val shareMediaTitle = stringResource(R.string.share_photo)

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
                    type = if (media.isVideo) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, media.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        shareMediaTitle
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MediaInfoBottomSheet(
    media: Media,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        MediaInfoBottomSheetContent(media = media)
    }
}

@Composable
private fun MediaInfoBottomSheetContent(
    media: Media,
    modifier: Modifier = Modifier
) {
    val formattedDate = formatPhotoDate(media.dateAdded)
    val formattedSize = formatPhotoSize(media.size)
    val fileType = getFriendlyFileType(media.mimeType)
    val dimensions = if (media.width > 0 && media.height > 0) {
        "${media.width} x ${media.height}"
    } else {
        "Unknown"
    }

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
            icon = Icons.Outlined.Info,
            label = "File type",
            value = fileType
        )

        InfoRow(
            icon = Icons.Outlined.AspectRatio,
            label = "Dimensions",
            value = dimensions
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

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun MediaDetailsScreenPreview(
    @PreviewParameter(MediaDetailsUiStateProvider::class) uiState: MediaDetailsUiState
) {
    MediaDetailsScreen(
        uiState = uiState,
        onEvent = {},
        initialMediaId = 1L
    )
}
