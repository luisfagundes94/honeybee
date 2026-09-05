package com.luisfagundes.premium.impl.domain.model

internal data class CustomerInfoSnapshot(
    val appUserId: String,
    val originalAppUserId: String,
    val activeEntitlements: Set<String>,
    val managementUrl: String?,
)
