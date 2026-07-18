package com.luisfagundes.config.impl.presentation.screen

import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BackHand
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.config.impl.R
import com.luisfagundes.config.impl.presentation.effect.ConfigUiEffect
import com.luisfagundes.config.impl.presentation.event.ConfigUiEvent
import com.luisfagundes.config.impl.presentation.state.ConfigUiState
import com.luisfagundes.config.impl.presentation.viewmodel.ConfigViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing

private const val APP_INTERNAL_SHARE_LINK =
    "https://play.google.com/apps/internaltest/4701609758531422116"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigScreen(
    onNavigateToFeedback: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ConfigViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            ConfigUiEffect.NavigateToStatistics -> onNavigateToStatistics()
            ConfigUiEffect.NavigateToFeedback -> onNavigateToFeedback()
        }
    }

    ConfigScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConfigScreen(
    uiState: ConfigUiState,
    onEvent: (ConfigUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.config),
                        modifier = Modifier.semantics { heading() }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(
                modifier = Modifier.height(MaterialTheme.spacing.default)
            )
            Text(
                text = stringResource(R.string.config_category_my_data),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.default,
                        vertical = MaterialTheme.spacing.small
                    )
                    .semantics { heading() }
            )
            Card(
                shape = RoundedCornerShape(MaterialTheme.spacing.default),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.default)
            ) {
                Column {
                    ConfigItem(
                        title = stringResource(R.string.config_item_statistics),
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        onClick = { onEvent(ConfigUiEvent.StatisticsClick) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ConfigItem(
                        title = stringResource(R.string.config_item_privacy),
                        icon = Icons.Default.BackHand,
                        onClick = { /* TODO: Navigate to privacy */ }
                    )
                }
            }
            Spacer(
                modifier = Modifier.height(MaterialTheme.spacing.default)
            )
            Text(
                text = stringResource(R.string.config_category_other),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(
                        horizontal = MaterialTheme.spacing.default,
                        vertical = MaterialTheme.spacing.small
                    )
                    .semantics { heading() }
            )
            Card(
                shape = RoundedCornerShape(MaterialTheme.spacing.default),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.spacing.default)
            ) {
                Column {
                    ConfigItem(
                        title = stringResource(R.string.config_item_premium),
                        icon = Icons.Default.Star,
                        iconContainer = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null
                            )
                        },
                        onClick = { /* TODO: Go to Premium screen */ }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ConfigItem(
                        title = stringResource(R.string.config_item_notifications),
                        icon = Icons.Default.Notifications,
                        trailingContent = {
                            Switch(
                                checked = uiState.isNotificationsEnabled,
                                onCheckedChange = {
                                    onEvent(ConfigUiEvent.NotificationsToggled(it))
                                }
                            )
                        },
                        onClick = {
                            onEvent(
                                ConfigUiEvent.NotificationsToggled(
                                    enabled = !uiState.isNotificationsEnabled
                                )
                            )
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ConfigItem(
                        title = stringResource(R.string.config_item_rate_app),
                        icon = Icons.Default.StarBorder,
                        onClick = { /* TODO: Rate app */ }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ConfigItem(
                        title = stringResource(R.string.config_item_send_feedback),
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        onClick = { onEvent(ConfigUiEvent.FeedbackClick) }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.default),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    ConfigItem(
                        title = stringResource(R.string.config_item_share_with_friends),
                        icon = Icons.Default.Share,
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, APP_INTERNAL_SHARE_LINK)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
            Spacer(
                modifier = Modifier.height(MaterialTheme.spacing.default)
            )
        }
    }
}

@Composable
private fun ConfigItem(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconContainer: (@Composable () -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    ListItem(
        modifier = modifier.let {
            if (onClick != null) it.clickable(onClick = onClick) else it
        },
        leadingContent = {
            if (iconContainer != null) {
                iconContainer()
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        trailingContent = trailingContent ?: {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        overlineContent = null,
        supportingContent = null,
        colors = ListItemDefaults.colors(
            containerColor = Color.Transparent
        ),
        content = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun ConfigScreenPreview() {
    ConfigScreen(
        uiState = ConfigUiState(),
        onEvent = {}
    )
}
