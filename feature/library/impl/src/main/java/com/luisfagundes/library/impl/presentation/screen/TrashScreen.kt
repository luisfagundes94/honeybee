package com.luisfagundes.library.impl.presentation.screen

import android.app.Activity
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.RestoreFromTrash
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.components.HoneybeePrimaryButton
import com.luisfagundes.core.designsystem.components.HoneybeeTertiaryButton
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.api.domain.model.DELETION_LIMIT
import com.luisfagundes.library.api.domain.model.DeletionQuota
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
import com.luisfagundes.library.impl.presentation.provider.TrashUiStateProvider
import com.luisfagundes.library.impl.presentation.state.TrashUiState
import com.luisfagundes.library.impl.presentation.viewmodel.TrashViewModel
import kotlinx.coroutines.delay
import com.luisfagundes.core.designsystem.R as DesignSystemResources

private const val TIMER_TICK_MILLIS = 1_000L
private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L

@Composable
internal fun TrashScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCongratulations: (Int, Long) -> Unit,
    onNavigateToPremium: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val deletionNotAllowedMessage = stringResource(R.string.deletion_not_allowed)
    var dailyLimitRemaining by rememberSaveable { mutableStateOf<Int?>(null) }

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
            TrashUiEffect.NavigateToPremium -> onNavigateToPremium()
            is TrashUiEffect.ShowDailyLimitSheet -> dailyLimitRemaining = effect.remaining
            is TrashUiEffect.ShowDeleteConfirmation -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    val pendingIntent = MediaStore.createDeleteRequest(
                        context.contentResolver,
                        effect.request.mediaUris.map(String::toUri),
                    )
                    deleteLauncher.launch(
                        IntentSenderRequest.Builder(pendingIntent.intentSender).build()
                    )
                }
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

    dailyLimitRemaining?.let { remaining ->
        DailyLimitBottomSheet(
            remaining = remaining,
            onDismissRequest = { dailyLimitRemaining = null },
            onPremiumClick = {
                dailyLimitRemaining = null
                onNavigateToPremium()
            },
            onDeleteWithinLimitClick = {
                dailyLimitRemaining = null
                viewModel.dispatchEvent(TrashUiEvent.DeleteWithinLimit)
            }
        )
    }
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
            deletionQuota = uiState.deletionQuota,
            onEvent = onEvent,
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun TrashContent(
    mediaToBeDeleted: List<Media>,
    deletionQuota: DeletionQuota,
    onEvent: (TrashUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    var showRestoreAllDialog by rememberSaveable { mutableStateOf(false) }

    if (showRestoreAllDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreAllDialog = false },
            title = {
                Text(text = stringResource(R.string.restore_all_confirmation_title))
            },
            text = {
                Text(text = stringResource(R.string.restore_all_confirmation_description))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreAllDialog = false
                        onEvent(TrashUiEvent.ClearTrash)
                    }
                ) {
                    Text(text = stringResource(R.string.restore_all))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreAllDialog = false }) {
                    Text(text = stringResource(DesignSystemResources.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
                TrashTopAppBar(
                    canClearTrash = mediaToBeDeleted.isNotEmpty(),
                    onClearTrash = { showRestoreAllDialog = true },
                    onBackClick = onBackClick
                )
                if (deletionQuota is DeletionQuota.Locked) {
                    DeletionLockBanner(
                        untilMillis = deletionQuota.untilMillis,
                        onExpired = { onEvent(TrashUiEvent.QuotaExpired) }
                    )
                }
            }
        },
        bottomBar = {
            val deleteCount = mediaToBeDeleted.size

            if (deleteCount > 0 && deletionQuota is DeletionQuota.Locked) {
                HoneybeePrimaryButton(
                    label = stringResource(R.string.delete_premium_button),
                    onClick = { onEvent(TrashUiEvent.ConfirmDeletion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.default)
                )
            } else if (deleteCount > 0) {
                HoneybeePrimaryButton(
                    label = pluralStringResource(
                        id = R.plurals.delete_media_format,
                        count = deleteCount,
                        deleteCount
                    ),
                    icon = Icons.Default.Delete,
                    onClick = { onEvent(TrashUiEvent.ConfirmDeletion) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.default)
                )
            }
        }
    ) { innerPadding ->
        if (mediaToBeDeleted.isEmpty()) {
            TrashEmptyContent(innerPadding)
        } else {
            TrashMediaGrid(
                mediaToBeDeleted = mediaToBeDeleted,
                innerPadding = innerPadding,
                onEvent = onEvent
            )
        }
    }
}

@Composable
private fun DeletionLockBanner(
    untilMillis: Long,
    onExpired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnExpired by rememberUpdatedState(onExpired)
    val remainingMillis by produceState(
        initialValue = (untilMillis - System.currentTimeMillis()).coerceAtLeast(0L),
        key1 = untilMillis
    ) {
        while (true) {
            val remaining = (untilMillis - System.currentTimeMillis()).coerceAtLeast(0L)
            value = remaining
            if (remaining == 0L) {
                currentOnExpired()
                break
            }
            delay(TIMER_TICK_MILLIS)
        }
    }
    val totalSeconds = remainingMillis / MILLIS_PER_SECOND
    val hours = (totalSeconds / SECONDS_PER_HOUR).toInt()
    val minutes = (totalSeconds % SECONDS_PER_HOUR / SECONDS_PER_MINUTE).toInt()
    val seconds = (totalSeconds % SECONDS_PER_MINUTE).toInt()
    val timerDescription = stringResource(
        R.string.deletion_lock_timer_content_description,
        hours,
        minutes,
        seconds
    )

    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(MaterialTheme.spacing.default),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.default)
            .padding(bottom = MaterialTheme.spacing.small)
            .semantics(mergeDescendants = true) { contentDescription = timerDescription }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.content),
            modifier = Modifier.padding(MaterialTheme.spacing.default)
        ) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.deletion_lock_banner_title),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.deletion_lock_banner_description),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = stringResource(R.string.deletion_lock_timer_format, hours, minutes, seconds),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DailyLimitBottomSheet(
    remaining: Int,
    onDismissRequest: () -> Unit,
    onPremiumClick: () -> Unit,
    onDeleteWithinLimitClick: () -> Unit
) {
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden
    )
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.default)
                .padding(bottom = MaterialTheme.spacing.large)
        ) {
            Text(
                text = stringResource(R.string.daily_limit_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            Text(
                text = stringResource(R.string.daily_limit_sheet_description, DELETION_LIMIT),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.default))
            HoneybeePrimaryButton(
                label = stringResource(R.string.daily_limit_sheet_premium_button),
                onClick = onPremiumClick,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            HoneybeeTertiaryButton(
                label = pluralStringResource(
                    id = R.plurals.delete_within_limit_format,
                    count = remaining,
                    remaining
                ),
                onClick = onDeleteWithinLimitClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TrashTopAppBar(
    canClearTrash: Boolean,
    onClearTrash: () -> Unit,
    onBackClick: () -> Unit
) {
    CenterAlignedTopAppBar(
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
        },
        actions = {
            TextButton(
                onClick = onClearTrash,
                enabled = canClearTrash
            ) {
                Icon(
                    imageVector = Icons.Default.RestoreFromTrash,
                    contentDescription = null
                )
                Text(
                    text = stringResource(R.string.restore_all)
                )
            }
        }
    )
}

@Composable
private fun TrashEmptyContent(innerPadding: PaddingValues) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) {
        Text(
            text = stringResource(R.string.trash_is_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun TrashMediaGrid(
    mediaToBeDeleted: List<Media>,
    innerPadding: PaddingValues,
    onEvent: (TrashUiEvent) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = MaterialTheme.spacing.mediaTileMin),
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

@Composable
private fun TrashMediaItem(
    media: Media,
    onItemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(MaterialTheme.spacing.mediaTileWidth)
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
                    modifier = Modifier.size(MaterialTheme.spacing.iconTiny)
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(MaterialTheme.spacing.verySmall)
                .size(MaterialTheme.spacing.iconLarge)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.RestoreFromTrash,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall)
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
