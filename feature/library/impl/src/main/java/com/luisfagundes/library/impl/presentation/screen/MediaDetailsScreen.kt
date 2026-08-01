package com.luisfagundes.library.impl.presentation.screen

import android.content.Intent
import androidx.core.net.toUri
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Card
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R as DesignSystemResources
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.components.FullscreenPhotoViewer
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
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
            title = stringResource(R.string.error_loading_media_details_title),
            description = stringResource(R.string.error_loading_media_details_description),
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
    var showBottomSheet by remember { mutableStateOf(false) }
    var showFullscreenPhoto by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .mediaSwipeGesture(
                mediaId = media.id,
                swipeOffset = swipeOffset,
                coroutineScope = coroutineScope,
                onSwipeUp = { onEvent(MediaDetailsUiEvent.SwipeUp(media.id)) }
            )
    ) {
        MediaPagerCard(
            media = media,
            isPageSelected = isPageSelected,
            aspectRatio = aspectRatio,
            swipeOffset = swipeOffset,
            onAspectRatioChanged = { aspectRatio = it },
            onPhotoClick = { showFullscreenPhoto = true }
        )

        MediaPagerItemActionsColumn(
            media = media,
            onInfoClick = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(MaterialTheme.spacing.default)
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f), CircleShape)
        )

        if (showBottomSheet) {
            MediaInfoBottomSheet(
                media = media,
                onDismissRequest = { showBottomSheet = false }
            )
        }

        if (showFullscreenPhoto) {
            FullscreenPhotoViewer(
                        photoUri = media.uri.toUri(),
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
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        IconButton(
            onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = if (media.isVideo) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, media.uri.toUri())
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
                tint = MaterialTheme.colorScheme.onSurface
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
        stringResource(R.string.media_dimensions_format, media.width, media.height)
    } else {
        stringResource(R.string.media_unknown)
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
            label = stringResource(R.string.media_info_date_added),
            value = formattedDate
        )

        InfoRow(
            icon = Icons.Outlined.Storage,
            label = stringResource(R.string.media_info_file_size),
            value = formattedSize
        )

        InfoRow(
            icon = Icons.Outlined.Info,
            label = stringResource(R.string.media_info_file_type),
            value = fileType
        )

        InfoRow(
            icon = Icons.Outlined.AspectRatio,
            label = stringResource(R.string.media_info_dimensions),
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
                .size(MaterialTheme.spacing.iconLarge)
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
