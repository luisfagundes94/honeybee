package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.library.impl.R

private const val TRASH_ONBOARDING_ANIMATION_SCALE = 1.25f

@Composable
internal fun SwipeUpTrashOnboardingOverlay(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(R.string.swipe_up_trash_onboarding_description)
    val tapHint = stringResource(R.string.swipe_up_trash_onboarding_tap_hint)
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.swipe_up_trash_onboarding),
        cacheKey = "swipe_up_trash_onboarding"
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClickLabel = tapHint,
                onClick = onDismiss
            )
            .semantics(mergeDescendants = true) {
                contentDescription = description
            }
    ) {
        SwipeUpTrashOnboardingContent(
            composition = composition,
            progress = progress,
            description = description,
            tapHint = tapHint
        )
    }
}

@Composable
private fun SwipeUpTrashOnboardingContent(
    composition: LottieComposition?,
    progress: Float,
    description: String,
    tapHint: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(MaterialTheme.spacing.large)
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.scale(TRASH_ONBOARDING_ANIMATION_SCALE)
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = tapHint,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}

@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@PreviewLightDark
@Composable
private fun SwipeUpTrashOnboardingOverlayPreview() {
    SwipeUpTrashOnboardingOverlay(
        onDismiss = {}
    )
}
