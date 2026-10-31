package com.luisfagundes.library.api.domain.model

const val DELETION_LIMIT = 25
const val DELETION_WINDOW_MILLIS = 60 * 60 * 1000L

sealed interface DeletionQuota {
    data object Unlimited : DeletionQuota
    data class Available(val remaining: Int) : DeletionQuota
    data class Locked(val untilMillis: Long) : DeletionQuota
}
