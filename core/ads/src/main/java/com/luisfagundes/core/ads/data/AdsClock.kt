package com.luisfagundes.core.ads.data

import javax.inject.Inject

internal fun interface AdsClock {
    fun nowMillis(): Long
}

internal class SystemAdsClock @Inject constructor() : AdsClock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
