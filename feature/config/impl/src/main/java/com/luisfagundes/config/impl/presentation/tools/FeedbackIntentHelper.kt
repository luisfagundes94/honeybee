package com.luisfagundes.config.impl.presentation.tools

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.luisfagundes.config.impl.R
import androidx.core.net.toUri

private const val RECIPIENT_EMAIL = "lf.android.dev@gmail.com"

internal fun launchFeedbackEmailIntent(
    context: Context,
    feedbackText: String
) {
    val appVersionName = try {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.versionName ?: "Unknown"
    } catch (e: Exception) {
        "Unknown"
    }

    val deviceDetails = """
        
        
        ----------------------------------
        Device Info (Do not delete):
        App Version: $appVersionName
        Android OS: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})
        Device: ${Build.MANUFACTURER} ${Build.MODEL}
        ----------------------------------
    """.trimIndent()

    val emailBody = "$feedbackText\n$deviceDetails"

    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf(RECIPIENT_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.feedback_email_subject))
        putExtra(Intent.EXTRA_TEXT, emailBody)
    }

    val chooserTitle = context.getString(R.string.feedback_email_chooser_title)
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}
