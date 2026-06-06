package com.luisfagundes.onboarding.impl.presentation.tools

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

@Composable
internal fun rememberPermissionsHandler(
    isPremium: Boolean,
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (shouldShowRationale: Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val permissions = remember(isPremium) {
        buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                if (isPremium) add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    val requiredPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val requiredGranted = requiredPermissions.all { results[it] == true }
        if (requiredGranted) {
            onPermissionsGranted()
        } else {
            val deniedRequiredPermissions = requiredPermissions.filter { results[it] != true }
            val activity = context.findActivity()
            val shouldShowRationale = activity?.let { act ->
                deniedRequiredPermissions.any { permission ->
                    ActivityCompat.shouldShowRequestPermissionRationale(act, permission)
                }
            } ?: false
            onPermissionsDenied(shouldShowRationale)
        }
    }

    return {
        val requiredGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED
        }
        if (requiredGranted) {
            onPermissionsGranted()
        } else {
            permissionLauncher.launch(permissions)
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}