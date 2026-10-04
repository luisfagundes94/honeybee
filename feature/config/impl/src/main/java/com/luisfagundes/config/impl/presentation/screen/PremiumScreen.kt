package com.luisfagundes.config.impl.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import com.luisfagundes.config.impl.R
import com.luisfagundes.core.designsystem.components.HoneybeeLogo
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import kotlinx.coroutines.launch

private enum class PremiumPlan {
    Monthly,
    Lifetime
}

@Composable
internal fun PremiumScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by rememberSaveable { mutableStateOf(PremiumPlan.Monthly) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val purchaseUnavailableMessage = stringResource(R.string.premium_purchase_unavailable)

    PremiumLayout(
        selectedPlan = selectedPlan,
        onPlanSelected = { selectedPlan = it },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        onContinue = {
            coroutineScope.launch {
                snackbarHostState.showSnackbar(purchaseUnavailableMessage)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PremiumLayout(
    selectedPlan: PremiumPlan,
    onPlanSelected: (PremiumPlan) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f),
            topBar = { PremiumTopBar(onNavigateBack = onNavigateBack) },
            bottomBar = {
                PremiumPurchaseOptions(
                    selectedPlan = selectedPlan,
                    onPlanSelected = onPlanSelected,
                    onContinue = onContinue
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            PremiumContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
            )
        }
    }
}

@Composable
private fun PremiumTopBar(onNavigateBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(
                        id = R.string.premium_close_button_content_description
                    )
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0f)
        )
    )
}

@Composable
private fun PremiumContent(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        PremiumHeader(
            modifier = Modifier.padding(MaterialTheme.spacing.default)
        )
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.large),
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.default)
        ) {
            PremiumBenefit(
                title = stringResource(R.string.premium_benefit_delete_title),
                description = stringResource(R.string.premium_benefit_delete_description),
                icon = Icons.Default.Delete
            )
            PremiumBenefit(
                title = stringResource(R.string.premium_benefit_no_ads_title),
                description = stringResource(R.string.premium_benefit_no_ads_description),
                icon = Icons.Default.Check
            )
            PremiumBenefit(
                title = stringResource(R.string.premium_benefit_videos_title),
                description = stringResource(R.string.premium_benefit_videos_description),
                icon = Icons.Default.Videocam
            )
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun PremiumHeader(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        modifier = modifier
    ) {
        HoneybeeLogo(
            modifier = Modifier
        )
        Text(
            text = stringResource(R.string.premium_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.semantics { heading() }
        )
    }
}

@Composable
private fun PremiumBenefit(
    title: String,
    description: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(MaterialTheme.spacing.iconLarge)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(MaterialTheme.spacing.iconSmall)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.verySmall)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumPurchaseOptions(
    selectedPlan: PremiumPlan,
    onPlanSelected: (PremiumPlan) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(MaterialTheme.spacing.default)
                .selectableGroup()
        ) {
            PremiumPlanOption(
                title = stringResource(R.string.premium_plan_monthly),
                price = stringResource(R.string.premium_price_monthly),
                billingPeriod = stringResource(R.string.premium_billing_period_monthly),
                selected = selectedPlan == PremiumPlan.Monthly,
                onClick = { onPlanSelected(PremiumPlan.Monthly) }
            )
            PremiumPlanOption(
                title = stringResource(R.string.premium_plan_lifetime),
                price = stringResource(R.string.premium_price_lifetime),
                billingPeriod = stringResource(R.string.premium_billing_period_lifetime),
                selected = selectedPlan == PremiumPlan.Lifetime,
                onClick = { onPlanSelected(PremiumPlan.Lifetime) }
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.default))
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.spacing.primaryButtonHeight)
            ) {
                Text(
                    text = stringResource(R.string.premium_purchase_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PremiumPlanOption(
    title: String,
    price: String,
    billingPeriod: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onClick
            ),
        shape = RoundedCornerShape(MaterialTheme.spacing.default),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            }
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.default)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = price,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                    Text(
                        text = billingPeriod,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)
@Composable
private fun PremiumScreenPreview() {
    PremiumScreen(onNavigateBack = {})
}
