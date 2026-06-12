package com.luisfagundes.config.impl.presentation.screen

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.config.impl.R
import com.luisfagundes.config.impl.presentation.effect.FeedbackUiEffect
import com.luisfagundes.config.impl.presentation.event.FeedbackUiEvent
import com.luisfagundes.config.impl.presentation.state.FeedbackUiState
import com.luisfagundes.config.impl.presentation.viewmodel.FeedbackViewModel
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.config.impl.presentation.tools.launchFeedbackEmailIntent
import com.luisfagundes.designsystem.components.HoneybeeButton
import com.luisfagundes.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.designsystem.theme.spacing

@Composable
internal fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            FeedbackUiEffect.NavigateBack -> onNavigateBack()
            is FeedbackUiEffect.ShowToast -> {
                Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
            is FeedbackUiEffect.OpenEmailClient -> {
                launchFeedbackEmailIntent(
                    context = context,
                    feedbackText = effect.feedbackText
                )
            }
        }
    }

    FeedbackScreen(
        uiState = uiState,
        onEvent = viewModel::dispatchEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedbackScreen(
    uiState: FeedbackUiState,
    onEvent: (FeedbackUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.feedback)) },
                navigationIcon = {
                    IconButton(onClick = { onEvent(FeedbackUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(
                                id = R.string.feedback_back_button_content_description
                            ),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            HoneybeeButton(
                onClick = { onEvent(FeedbackUiEvent.SubmitFeedback) },
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(MaterialTheme.spacing.default),
                enabled = uiState.isSubmitButtonEnabled
            ) {
                Text(
                    text = stringResource(R.string.feedback_submit),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = MaterialTheme.spacing.verySmall)
                )
            }
        }
    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.default)
        ) {
            Text(
                text = stringResource(R.string.feedback_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextField(
                value = uiState.feedbackText,
                onValueChange = { onEvent(FeedbackUiEvent.UpdateFeedbackText(it)) },
                placeholder = {
                    Text(
                        text = stringResource(R.string.feedback_placeholder),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = false,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun FeedbackScreenPreview() {
    FeedbackScreen(
        uiState = FeedbackUiState(feedbackText = "This is a feedback"),
        onEvent = {}
    )
}
