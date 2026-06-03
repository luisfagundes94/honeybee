package com.luisfagundes.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.luisfagundes.designsystem.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneybeeTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = title,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = WindowInsets(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneybeeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    HoneybeeTopAppBar(
        title = { Text(text = title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoneybeeTopAppBar(
    title: String,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0,
    actionIcon: ImageVector = Icons.Default.DeleteOutline,
    badgedIcon: ImageVector = Icons.Default.Delete,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
) {
    HoneybeeTopAppBar(
        title = title,
        modifier = modifier,
        colors = colors,
        actions = {
            BadgedBox(
                badge = {
                    if (badgeCount > 0) {
                        Badge(
                            modifier = Modifier.offset(x = (-12).dp, y = 12.dp)
                        ) {
                            val displayCount = if (badgeCount > 99) "99+" else badgeCount.toString()
                            Text(text = displayCount)
                        }
                    }
                },
                modifier = Modifier.padding(end = MaterialTheme.spacing.default)
            ) {
                IconButton(onClick = onActionClick) {
                    Icon(
                        imageVector = if (badgeCount > 0) badgedIcon else actionIcon,
                        contentDescription = null
                    )
                }
            }
        }
    )
}