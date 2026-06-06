package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.luisfagundes.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.library.impl.domain.model.Photo
import com.luisfagundes.library.impl.presentation.effect.TrashUiEffect
import com.luisfagundes.library.impl.presentation.event.TrashUiEvent
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

    TrashContent(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        onBackClick = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TrashContent(
    uiState: TrashUiState,
    onEvent: (TrashUiEvent) -> Unit,
    onBackClick: () -> Unit
) {
    when (uiState) {
        is TrashUiState.Loading -> HoneybeeLoadingTemplate(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )

        is TrashUiState.Error -> HoneybeeErrorTemplate(
            message = uiState.message,
            onRetry = { onEvent(TrashUiEvent.LoadTrash) }
        )

        is TrashUiState.Content -> Trash(
            photosToBeDeleted = uiState.photosToBeDeleted,
            onEvent = onEvent,
            onBackClick = onBackClick
        )
    }
}

@Composable
private fun Trash(
    photosToBeDeleted: List<Photo>,
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
                deleteCount = photosToBeDeleted.size,
                onConfirmDeletion = { onEvent(TrashUiEvent.ConfirmDeletion) }
            )
        }
    ) { innerPadding ->
        if (photosToBeDeleted.isEmpty()) {
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
                modifier = Modifier.padding(innerPadding),
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.default),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall)
            ) {
                items(photosToBeDeleted) { photo ->
                    TrashPhotoItem(
                        photo = photo,
                        onItemClick = { onEvent(TrashUiEvent.RestorePhoto(photo.id)) }
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
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.default)
        ) {
            Button(
                onClick = onConfirmDeletion,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
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
                        id = R.plurals.delete_photos_format,
                        count = deleteCount,
                        deleteCount
                    ),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun TrashPhotoItem(
    photo: Photo,
    onItemClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(108.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onItemClick)
    ) {
        AsyncImage(
            model = photo.uri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
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
