package com.luisfagundes.honeybee.presentation.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import com.luisfagundes.albums.api.presentation.navigation.AlbumsRoute
import com.luisfagundes.config.api.presentation.navigation.ConfigRoute
import com.luisfagundes.honeybee.R
import com.luisfagundes.library.api.presentation.navigation.LibraryRoute

enum class TopLevelDestination(
    val route: NavKey,
    val icon: ImageVector,
    @StringRes val labelRes: Int,
) {
    ALBUMS(
        route = AlbumsRoute,
        icon = Icons.Default.Collections,
        labelRes = R.string.albums
    ),
    LIBRARY(
        route = LibraryRoute,
        icon = Icons.Default.PhotoLibrary,
        labelRes = R.string.library
    ),
    CONFIG(
        route = ConfigRoute,
        icon = Icons.Default.Settings,
        labelRes = R.string.config
    )
}
