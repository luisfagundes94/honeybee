package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.luisfagundes.library.impl.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R as DesignSystemResources
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.provider.TrashUiStateProvider
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import com.luisfagundes.library.impl.presentation.viewmodel.TrashViewModel

@Composable
internal fun TrashScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCongratulations: (Int, Long) -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val deletionNotAllowedMessage = stringResource(R.string.deletion_not_allowed)

    val deleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.dispatchEvent(TrashUiEvent.ApproveDeletion)
        } else {
            Toast.makeText(context, deletionNotAllowedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            TrashUiEffect.NavigateBack -> onNavigateBack()
            is TrashUiEffect.ShowDeleteConfirmation -> {
                val request = IntentSenderRequest.Builder(effect.intentSender).build()
                deleteLauncher.launch(request)
            }
            is TrashUiEffect.NavigateToCongratulations -> {
                onNavigateToCongratulations(effect.deletedCount, effect.deletedSize)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(TrashUiEvent.LoadTrash)
    }

    TrashScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TrashScreen(
    uiState: TrashUiState,
    onEvent: (TrashUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    when (uiState) {
        is TrashUiState.Loading -> HoneybeeLoadingTemplate()

        is TrashUiState.Error -> HoneybeeErrorTemplate(
            title = stringResource(R.string.error_loading_trash_title),
            description = stringResource(R.string.error_loading_trash_description),
            primaryButtonLabel = stringResource(DesignSystemResources.string.retry),
            onPrimaryButtonClick = { onEvent(TrashUiEvent.LoadTrash) }
        )

        is TrashUiState.Content -> TrashContent(
            mediaToBeDeleted = uiState.mediaToBeDeleted,
            onEvent = onEvent,
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun TrashContent(
    mediaToBeDeleted: List<Media>,
    onEvent: (TrashUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TrashTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            TrashBottomBar(
                deleteCount = mediaToBeDeleted.size,
                onConfirmDeletion = { onEvent(TrashUiEvent.ConfirmDeletion) }
            )
        }
    ) { innerPadding ->
        if (mediaToBeDeleted.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Text(
                    text = stringResource(R.string.trash_is_empty),
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding(),
                    start = MaterialTheme.spacing.default,
                    end = MaterialTheme.spacing.default
                ),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall)
            ) {
                items(mediaToBeDeleted) { media ->
                    TrashMediaItem(
                        media = media,
                        onItemClick = { onEvent(TrashUiEvent.RestoreMedia(media.id)) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrashTopBar(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.trash),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close)
                )
            }
        }
    )
}

@Composable
private fun TrashBottomBar(
    deleteCount: Int,
    onConfirmDeletion: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (deleteCount > 0) {
        Button(
            onClick = onConfirmDeletion,
            shape = RoundedCornerShape(24.dp),
            modifier = modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(MaterialTheme.spacing.default)
                .height(56.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = pluralStringResource(
                    id = R.plurals.delete_media_format,
                    count = deleteCount,
                    deleteCount
                ),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun TrashMediaItem(
    media: Media,
    onItemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(108.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(MaterialTheme.spacing.small))
            .clickable(onClick = onItemClick)
    ) {
        AsyncImage(
            model = media.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        if (media.isVideo) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(MaterialTheme.spacing.verySmall)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f), CircleShape)
                    .padding(MaterialTheme.spacing.verySmall)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(
                        DesignSystemResources.string.video_content_description
                    ),
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(10.dp)
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(MaterialTheme.spacing.verySmall)
                .size(24.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun TrashScreenPreview(
    @PreviewParameter(TrashUiStateProvider::class) uiState: TrashUiState
) {
    TrashScreen(
        uiState = uiState,
        onEvent = {},
        onBackClick = {}
    )
}
