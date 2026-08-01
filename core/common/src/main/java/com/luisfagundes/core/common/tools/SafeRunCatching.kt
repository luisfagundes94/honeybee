package com.luisfagundes.core.common.tools

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> safeRunCatching(block: () -> T): Result<T> {
    return runCatching(block).onFailure { throwable ->
        if (throwable is CancellationException) {
            throw throwable
        }
    }
}
