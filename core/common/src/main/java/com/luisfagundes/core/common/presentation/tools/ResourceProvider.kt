package com.luisfagundes.core.common.presentation.tools

interface ResourceProvider {
    fun getString(resId: Int): String
}