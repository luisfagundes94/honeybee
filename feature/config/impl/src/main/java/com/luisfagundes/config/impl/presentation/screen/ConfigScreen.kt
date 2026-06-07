package com.luisfagundes.config.impl.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.luisfagundes.designsystem.theme.spacing
import com.luisfagundes.config.impl.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ConfigScreen(
    modifier: Modifier = Modifier
) {
    var isDarkModeEnabled by remember { mutableStateOf(true) }
    var isStorageOptimizationEnabled by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.config)) },
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
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.default,
                    vertical = MaterialTheme.spacing.small
                )
            )

            ConfigToggleItem(
                title = "Dark Mode",
                subtitle = "Use dark color scheme throughout the application",
                icon = Icons.Default.Palette,
                checked = isDarkModeEnabled,
                onCheckedChange = { isDarkModeEnabled = it }
            )

            ConfigToggleItem(
                title = "Smart Cleanup",
                subtitle = "Auto-group similar photos for easier sorting",
                icon = Icons.Default.Storage,
                checked = isStorageOptimizationEnabled,
                onCheckedChange = { isStorageOptimizationEnabled = it }
            )

            Text(
                text = "About",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.spacing.default,
                    vertical = MaterialTheme.spacing.small
                )
            )

            ListItem(
                headlineContent = { Text("Version") },
                supportingContent = { Text("1.0.0 (Release)") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Database Status") },
                supportingContent = { Text("Healthy (SQLite Room)") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null
                    )
                }
            )
        }
    }
}

@Composable
private fun ConfigToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    ListItem(
        headlineContent = { Text(text = title) },
        supportingContent = { Text(text = subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        },
        modifier = modifier.clickable { onCheckedChange(!checked) }
    )
}
