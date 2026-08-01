package com.luisfagundes.library.impl.presentation.screen

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieClipSpec
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.luisfagundes.core.common.presentation.tools.formatSize
import com.luisfagundes.core.designsystem.components.HoneybeePrimaryButton
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.impl.R
import com.luisfagundes.library.impl.R.raw.congratulations_animation

@Composable
internal fun CongratulationsScreen(
    deletedCount: Int,
    deletedSize: Long,
    onDoneClick: (Activity) -> Unit
) {
    val activity = LocalActivity.current
    val animationResource = LottieCompositionSpec.RawRes(congratulations_animation)
    val composition by rememberLottieComposition(
        spec = animationResource,
        cacheKey = "congratulations_anim"
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        speed = 1.2f,
        clipSpec = LottieClipSpec.Progress(0.1f, 1.0f),
        iterations = 1
    )
    val (sizeValue, sizeUnit) = formatSize(deletedSize)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = {
            HoneybeePrimaryButton(
                label = stringResource(R.string.done),
                onClick = { activity?.let(onDoneClick) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.default)
            )
        }
    ) { innerPadding ->
        CongratulationsContent(
            composition = composition,
            progress = progress,
            sizeValue = sizeValue,
            sizeUnit = sizeUnit,
            deletedCount = deletedCount,
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
private fun CongratulationsContent(
    composition: com.airbnb.lottie.LottieComposition?,
    progress: Float,
    sizeValue: String,
    sizeUnit: String,
    deletedCount: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = MaterialTheme.spacing.default),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(MaterialTheme.spacing.illustrationSize)
        )
        SavedSize(sizeValue = sizeValue, sizeUnit = sizeUnit)
        Text(
            text = stringResource(R.string.saved),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        DeletedMediaCard(deletedCount = deletedCount)
    }
}

@Composable
private fun SavedSize(sizeValue: String, sizeUnit: String) {
    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = sizeValue,
            fontSize = 64.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
        Text(
            text = sizeUnit,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = MaterialTheme.spacing.content)
        )
    }
}

@Composable
private fun DeletedMediaCard(deletedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(MaterialTheme.spacing.default)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.default),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(R.string.media_deleted),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = deletedCount.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun CongratulationsScreenPreview() {
    CongratulationsScreen(
        deletedCount = 2,
        deletedSize = 7340032L, // 7 MB
        onDoneClick = {}
    )
}
