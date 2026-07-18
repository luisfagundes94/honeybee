package com.luisfagundes.library.impl.presentation.screen

import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaSection
import com.luisfagundes.library.impl.presentation.components.TrashBadgedBox
import com.luisfagundes.library.impl.presentation.effect.LibraryUiEffect
import com.luisfagundes.library.impl.presentation.event.LibraryUiEvent
import com.luisfagundes.library.impl.presentation.state.LibraryUiState
import com.luisfagundes.library.impl.presentation.tools.MediaStoreThumbnailRequest
import com.luisfagundes.library.impl.presentation.tools.getFormattedMonthName
import com.luisfagundes.library.impl.presentation.tools.rememberMediaStoreThumbnailImageLoader
import com.luisfagundes.library.impl.presentation.viewmodel.LibraryViewModel
import java.time.YearMonth

private val MinimumMediaTileSize = 100.dp
private const val SquareAspectRatio = 1f
private const val VideoIndicatorContainerAlpha = 0.5f

@Composable
internal fun LibraryScreen(
    onNavigateToMediaDetail: (Long) -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is LibraryUiEffect.NavigateToMediaDetail -> onNavigateToMediaDetail(effect.mediaId)
            LibraryUiEffect.NavigateToTrash -> onNavigateToTrash()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(LibraryUiEvent.LoadMedia)
    }

    LibraryScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun LibraryScreen(
    uiState: LibraryUiState,
    onEvent: (LibraryUiEvent) -> Unit
) {
    when (uiState) {
        is LibraryUiState.Loading -> HoneybeeLoadingTemplate(
            modifier = Modifier.fillMaxSize()
        )

        is LibraryUiState.Error -> HoneybeeErrorTemplate(
            message = uiState.message,
            onRetry = { onEvent(LibraryUiEvent.LoadMedia) }
        )

        is LibraryUiState.Content -> LibraryContent(
            mediaSectionList = uiState.mediaSectionList,
            itemsInTrash = uiState.itemsInTrash,
            onEvent = onEvent,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryContent(
    mediaSectionList: List<MediaSection>,
    itemsInTrash: Int,
    onEvent: (LibraryUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val mediaThumbnailImageLoader = rememberMediaStoreThumbnailImageLoader()
    val gridSpacing = MaterialTheme.spacing.verySmall
    val gridHorizontalPadding = MaterialTheme.spacing.default
    val density = LocalDensity.current
    var mediaTileSizePx by remember { mutableIntStateOf(0) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    Text(
                        text = stringResource(R.string.library),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    TrashBadgedBox(
                        itemsInTrash = itemsInTrash,
                        onClick = { onEvent(LibraryUiEvent.TrashClick) },
                        contentDescription = stringResource(R.string.items_in_trash),
                        modifier = Modifier.padding(end = MaterialTheme.spacing.default)
                    )
                }
            )
        }
    ) { innerPadding ->
        if (mediaSectionList.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = stringResource(R.string.library_is_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = MinimumMediaTileSize),
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { gridSize ->
                        mediaTileSizePx = calculateMediaTileSizePx(
                            gridWidthPx = gridSize.width,
                            horizontalPaddingPx = with(density) {
                                gridHorizontalPadding.roundToPx()
                            },
                            spacingPx = with(density) { gridSpacing.roundToPx() },
                            minimumTileSizePx = with(density) {
                                MinimumMediaTileSize.roundToPx()
                            }
                        )
                    }
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = gridHorizontalPadding,
                    end = gridHorizontalPadding
                ),
                horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                verticalArrangement = Arrangement.spacedBy(gridSpacing)
            ) {
                mediaSectionList.forEach { mediaSection ->
                    item(
                        key = "header_${mediaSection.yearMonth}",
                        span = { GridItemSpan(maxLineSpan) },
                        contentType = "header"
                    ) {
                        val month = mediaSection.yearMonth.getFormattedMonthName()
                        val year = mediaSection.yearMonth.year

                        Text(
                            text = stringResource(R.string.media_section_header, month, year),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .padding(vertical = MaterialTheme.spacing.small)
                                .semantics { heading() }
                        )
                    }
                    items(
                        items = mediaSection.mediaList,
                        key = { media -> media.id },
                        contentType = { "media" }
                    ) { media ->
                        MediaGridItem(
                            media = media,
                            thumbnailSizePx = mediaTileSizePx,
                            imageLoader = mediaThumbnailImageLoader,
                            onClick = { onEvent(LibraryUiEvent.MediaClick(media.id)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaGridItem(
    media: Media,
    thumbnailSizePx: Int,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val mediaContentDescription = stringResource(
        if (media.isVideo) R.string.open_video else R.string.open_photo
    )
    val thumbnailRequest = remember(context, media.uri, thumbnailSizePx) {
        thumbnailSizePx.takeIf { it > 0 }?.let { sizePx ->
            ImageRequest.Builder(context)
                .data(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        MediaStoreThumbnailRequest(
                            uri = media.uri,
                            widthPx = sizePx,
                            heightPx = sizePx
                        )
                    } else {
                        media.uri
                    }
                )
                .size(sizePx, sizePx)
                .build()
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(SquareAspectRatio)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .semantics {
                contentDescription = mediaContentDescription
            }
    ) {
        SubcomposeAsyncImage(
            model = thumbnailRequest,
            imageLoader = imageLoader,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        ) {
            when (painter.state) {
                is AsyncImagePainter.State.Empty,
                is AsyncImagePainter.State.Loading -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                is AsyncImagePainter.State.Error -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                else -> SubcomposeAsyncImageContent()
            }
        }

        if (media.isVideo) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(MaterialTheme.spacing.verySmall)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = VideoIndicatorContainerAlpha
                        ),
                        shape = CircleShape
                    )
                    .padding(MaterialTheme.spacing.verySmall)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(MaterialTheme.spacing.default)
                )
            }
        }
    }
}

private fun calculateMediaTileSizePx(
    gridWidthPx: Int,
    horizontalPaddingPx: Int,
    spacingPx: Int,
    minimumTileSizePx: Int
): Int {
    val availableWidthPx = (gridWidthPx - horizontalPaddingPx * 2).coerceAtLeast(0)
    val columnCount = ((availableWidthPx + spacingPx) / (minimumTileSizePx + spacingPx))
        .coerceAtLeast(1)
    val totalSpacingPx = spacingPx * (columnCount - 1)
    return ((availableWidthPx - totalSpacingPx) / columnCount).coerceAtLeast(1)
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun LibraryContentContentPreview() {
    LibraryContent(
        mediaSectionList = listOf(
            MediaSection(
                yearMonth = YearMonth.of(2026, 6),
                mediaList = listOf(
                    Media(id = 1L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                    Media(id = 2L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = true),
                    Media(id = 3L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                )
            ),
            MediaSection(
                yearMonth = YearMonth.of(2026, 5),
                mediaList = listOf(
                    Media(id = 4L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = true),
                    Media(id = 5L, uri = Uri.EMPTY, dateAdded = 0L, size = 0L, isVideo = false),
                )
            )
        ),
        itemsInTrash = 3,
        onEvent = {},
        modifier = Modifier.fillMaxWidth()
    )
}

