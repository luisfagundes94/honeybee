package com.luisfagundes.config.impl.presentation.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.config.impl.R
import com.luisfagundes.config.impl.presentation.effect.StatisticsUiEffect
import com.luisfagundes.config.impl.presentation.event.StatisticsUiEvent
import com.luisfagundes.config.impl.presentation.state.StatisticsUiState
import com.luisfagundes.config.impl.presentation.viewmodel.StatisticsViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.common.presentation.tools.formatSize
import com.luisfagundes.core.designsystem.components.HoneybeeErrorTemplate
import com.luisfagundes.core.designsystem.components.HoneybeeLoadingTemplate
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.core.designsystem.R as DesignSystemResources
import com.luisfagundes.library.api.domain.model.Statistics

@Composable
internal fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(StatisticsUiEvent.LoadStatistics)
    }

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            StatisticsUiEffect.NavigateBack -> onNavigateBack()
        }
    }

    StatisticsScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatisticsScreen(
    uiState: StatisticsUiState,
    onEvent: (StatisticsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.statistics_title),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onEvent(StatisticsUiEvent.BackClick) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                R.string.statistics_back_button_content_description
                            ),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        when (uiState) {
            is StatisticsUiState.Loading -> {
                HoneybeeLoadingTemplate(
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is StatisticsUiState.Error -> {
                HoneybeeErrorTemplate(
                    message = stringResource(R.string.failed_to_load_statistics),
                    primaryButtonLabel = stringResource(DesignSystemResources.string.retry),
                    onPrimaryButtonClick = { onEvent(StatisticsUiEvent.LoadStatistics) },
                    secondaryButtonLabel = stringResource(DesignSystemResources.string.cancel),
                    onSecondaryButtonClick = { onEvent(StatisticsUiEvent.CancelClick) },
                    modifier = Modifier.padding(innerPadding)
                )
            }
            is StatisticsUiState.Content -> {
                val stats = uiState.statistics
                val (memoryVal, memoryUnit) = if (stats.memoryCleared == 0L) {
                    "0" to "MB"
                } else {
                    formatSize(stats.memoryCleared)
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding + PaddingValues(MaterialTheme.spacing.default))
                        .verticalScroll(rememberScrollState())
                ) {
                    StatCard(
                        title = stringResource(R.string.memory_cleared),
                        value = memoryVal,
                        unit = memoryUnit,
                        icon = Icons.Default.Eco
                    )

                    StatCard(
                        title = stringResource(R.string.media_deleted),
                        value = stats.mediaDeleted.toString(),
                        icon = Icons.Default.Delete
                    )

                    StatCard(
                        title = stringResource(R.string.photos_deleted),
                        value = stats.photosDeleted.toString(),
                        icon = Icons.Default.Image
                    )

                    StatCard(
                        title = stringResource(R.string.videos_deleted),
                        value = stats.videosDeleted.toString(),
                        icon = Icons.Default.Videocam
                    )
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    unit: String? = null
) {
    Card(
        shape = RoundedCornerShape(MaterialTheme.spacing.default),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.default),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.default))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit != null) {
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.verySmall))
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = MaterialTheme.spacing.verySmall)
                    )
                }
            }
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun StatisticsScreenPreview() {
    StatisticsScreen(
        uiState = StatisticsUiState.Content(
            statistics = Statistics(
                memoryCleared = 120 * 1024 * 1024L,
                mediaDeleted = 42,
                photosDeleted = 30,
                videosDeleted = 12
            )
        ),
        onEvent = {}
    )
}
