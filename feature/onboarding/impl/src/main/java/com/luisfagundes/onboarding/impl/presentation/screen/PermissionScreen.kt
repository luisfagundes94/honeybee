package com.luisfagundes.onboarding.impl.presentation.screen

import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.designsystem.R.drawable.honeybee_low_res
import com.luisfagundes.designsystem.components.HoneybeeButton
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.onboarding.impl.R
import com.luisfagundes.onboarding.impl.presentation.effect.PermissionUiEffect
import com.luisfagundes.onboarding.impl.presentation.event.PermissionUiEvent
import com.luisfagundes.onboarding.impl.presentation.screen.components.PermissionsSettingsDialog
import com.luisfagundes.onboarding.impl.presentation.tools.rememberPermissionsHandler
import com.luisfagundes.onboarding.impl.presentation.viewmodel.PermissionViewModel


@Composable
internal fun PermissionScreen(
    onNavigateToLibrary: () -> Unit,
    viewModel: PermissionViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val permissionsDeniedMessage = stringResource(R.string.permissions_denied_message)
    val (showSettingsDialog, setShowSettingsDialog) = remember { mutableStateOf(false) }
    val requestPermissions = rememberPermissionsHandler(
        onPermissionsGranted = {
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsGranted)
        },
        onPermissionsDenied = { shouldShowRationale ->
            viewModel.dispatchEvent(PermissionUiEvent.PermissionsDenied(shouldShowRationale))
        }
    )

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is PermissionUiEffect.NavigateToLibrary -> onNavigateToLibrary()
            is PermissionUiEffect.ShowDeniedMessage -> {
                Toast.makeText(context, permissionsDeniedMessage, Toast.LENGTH_LONG).show()
            }
            is PermissionUiEffect.ShowSettingsDialog -> {
                setShowSettingsDialog(true)
            }
        }
    }

    PermissionContent(
        onAllowAccessClick = requestPermissions,
        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.default)
    )

    if (showSettingsDialog) {
        PermissionsSettingsDialog(
            onDismiss = { setShowSettingsDialog(false) },
            onConfirm = {
                setShowSettingsDialog(false)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", context.packageName, null)
                }
                context.startActivity(intent)
            }
        )
    }
}

@Composable
private fun PermissionContent(
    onAllowAccessClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                PermissionAlertCard(
                    modifier = Modifier.fillMaxWidth()
                )
                HoneybeeButton(
                    onClick = onAllowAccessClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.allow_access)
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(innerPadding)
        ) {
            Image(
                painter = painterResource(honeybee_low_res),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.scale(0.75f)
            )
            Text(
                text = stringResource(R.string.permission_screen_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            PermissionCardItem(
                title = stringResource(R.string.photo_library_permission_title),
                description = stringResource(R.string.photo_library_permission_description),
                icon = Icons.Filled.PhotoLibrary,
                modifier = Modifier.fillMaxWidth()
            )
            PermissionCardItem(
                title = stringResource(R.string.notifications_permission_title),
                description = stringResource(R.string.notifications_permission_description),
                icon = Icons.Filled.Notifications,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun PermissionAlertCard(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.privacy_assurance),
    icon: ImageVector = Icons.Filled.PrivacyTip
) {
    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.default),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(
                modifier = Modifier.width(MaterialTheme.spacing.default)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PermissionCardItem(
    title: String,
    description: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(
            modifier = Modifier.width(MaterialTheme.spacing.default)
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun PermissionContentPreview() {
    PermissionContent(
        onAllowAccessClick = {},
        modifier = Modifier.fillMaxSize().padding(MaterialTheme.spacing.default)
    )
}