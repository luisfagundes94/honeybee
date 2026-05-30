package com.luisfagundes.core.common.presentation.tools

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ResourceProviderImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ResourceProvider {
    override fun getString(resId: Int): String {
        return context.getString(resId)
    }
}