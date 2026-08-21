package com.luisfagundes.baselineprofile

import android.Manifest
import android.os.Build
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import java.io.IOException

private const val GRID_LOAD_TIMEOUT_MILLIS = 10_000L
private const val ONBOARDING_TIMEOUT_MILLIS = 5_000L
private const val BUTTON_CLASS_NAME = "android.widget.Button"

/**
 * Puts a freshly installed benchmark app in the same state as a user who has completed setup.
 *
 * Macrobenchmarks install the target APK with a clean data directory. Without this setup the
 * launcher opens onboarding, so there is no scrollable Library grid to measure.
 */
internal fun UiDevice.prepareLibrary(): UiObject2 {
    completeOnboardingIfNeeded()

    return wait(Until.findObject(By.scrollable(true)), GRID_LOAD_TIMEOUT_MILLIS)
        ?: error(
            "Library grid was not found. Ensure the benchmark device contains at least one " +
                "photo or video in MediaStore."
        )
}

internal fun UiDevice.grantLibraryRuntimePermissions(packageName: String) {
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        listOf(
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.POST_NOTIFICATIONS,
        )
    } else {
        listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    permissions.forEach { permission ->
        try {
            val output = executeShellCommand("pm grant $packageName $permission").trim()
            check(output.isEmpty()) {
                "Unable to grant $permission for benchmark: $output"
            }
        } catch (error: IOException) {
            throw IllegalStateException("Unable to grant $permission for benchmark", error)
        }
    }
}

private fun UiDevice.completeOnboardingIfNeeded() {
    if (wait(Until.findObject(By.scrollable(true)), ONBOARDING_TIMEOUT_MILLIS) != null) return

    wait(Until.findObject(By.clazz(BUTTON_CLASS_NAME)), ONBOARDING_TIMEOUT_MILLIS)
        ?.click()
        ?: return

    wait(Until.findObject(By.clazz(BUTTON_CLASS_NAME)), ONBOARDING_TIMEOUT_MILLIS)
        ?.click()
        ?: error("Permission screen was not shown after onboarding")
}
