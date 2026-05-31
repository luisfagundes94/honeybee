package com.luisfagundes.library.api.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LibraryRoute : NavKey

@Serializable
data class MediaDetailsRoute(val initialPhotoId: Long) : NavKey

@Serializable
data object TrashRoute : NavKey

@Serializable
data class CongratulationsRoute(val deletedCount: Int, val deletedSize: Long) : NavKey