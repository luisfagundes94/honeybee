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
    onPermissionsGranted: () -> Unit,
    onPermissionsDenied: (shouldShowRationale: Boolean) -> Unit
): () -> Unit {
    val context = LocalContext.current

    val (requiredPermissions, allPermissions) = remember {
        val required = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        val optional = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        required.toTypedArray() to (required + optional).toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allRequiredGranted = requiredPermissions.all { results[it] == true }
        if (allRequiredGranted) {
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
        val allRequiredGranted = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PERMISSION_GRANTED
        }
        if (allRequiredGranted) {
            onPermissionsGranted()
        } else {
            permissionLauncher.launch(allPermissions)
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