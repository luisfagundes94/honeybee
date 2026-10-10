package com.luisfagundes.config.impl.presentation.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.config.impl.R
import com.luisfagundes.core.billing.PremiumBillingCoordinator
import com.luisfagundes.core.billing.PremiumBillingUiState
import com.luisfagundes.core.billing.PremiumPlan
import com.luisfagundes.core.billing.PremiumPurchaseEvent
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.core.designsystem.components.HoneybeeLogo
import com.luisfagundes.core.designsystem.theme.HoneybeeThemeWrapper
import com.luisfagundes.core.designsystem.theme.spacing
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Composable
internal fun PremiumScreen(
    onNavigateBack: () -> Unit,
    billingCoordinator: PremiumBillingCoordinator,
    modifier: Modifier = Modifier
) {
    var selectedPlan by rememberSaveable { mutableStateOf(PremiumPlan.Monthly) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val billingState by billingCoordinator.uiState.collectAsStateWithLifecycle()
    val subscriptionStatus by billingCoordinator.status.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val purchaseSuccessMessage = stringResource(R.string.premium_purchase_success)
    val purchasePendingMessage = stringResource(R.string.premium_purchase_pending)
    val purchaseUnavailableMessage = stringResource(R.string.premium_purchase_unavailable)
    val purchaseFailedMessage = stringResource(R.string.premium_purchase_failed)
    val productsUnavailableMessage = stringResource(R.string.premium_products_unavailable)
    val priceUnavailable = stringResource(R.string.premium_price_unavailable)

    LaunchedEffect(billingCoordinator) {
        billingCoordinator.refreshOffers()
    }
    LaunchedEffect(billingCoordinator) {
        billingCoordinator.purchaseEvents.collect { event ->
            val message = when (event) {
                PremiumPurchaseEvent.Purchased -> purchaseSuccessMessage
                PremiumPurchaseEvent.Pending -> purchasePendingMessage
                PremiumPurchaseEvent.Cancelled -> null
                PremiumPurchaseEvent.Unavailable -> purchaseUnavailableMessage
                PremiumPurchaseEvent.Failed -> purchaseFailedMessage
            }
            message?.let { snackbarHostState.showSnackbar(it) }
        }
    }
    LaunchedEffect(billingState.offersUnavailable) {
        if (billingState.offersUnavailable) {
            snackbarHostState.showSnackbar(productsUnavailableMessage)
        }
    }

    PremiumLayout(
        selectedPlan = selectedPlan,
        billingState = billingState,
        isPremium = subscriptionStatus == SubscriptionStatus.Premium,
        priceUnavailable = priceUnavailable,
        onPlanSelected = { selectedPlan = it },
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        onContinue = {
            val activity = context.findActivity()
            if (activity == null) {
                coroutineScope.launch {
                    snackbarHostState.showSnackbar(purchaseUnavailableMessage)
                }
            } else {
                billingCoordinator.purchase(activity, selectedPlan)
            }
        },
        modifier = modifier
    )
}

@Composable
private fun PremiumLayout(
    selectedPlan: PremiumPlan,
    billingState: PremiumBillingUiState,
    isPremium: Boolean,
    priceUnavailable: String,
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
                    billingState = billingState,
                    isPremium = isPremium,
                    priceUnavailable = priceUnavailable,
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
    billingState: PremiumBillingUiState,
    isPremium: Boolean,
    priceUnavailable: String,
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
                price = billingState.monthlyPrice ?: priceUnavailable,
                billingPeriod = stringResource(R.string.premium_billing_period_monthly),
                selected = selectedPlan == PremiumPlan.Monthly,
                enabled = billingState.monthlyPrice != null && !isPremium,
                onClick = { onPlanSelected(PremiumPlan.Monthly) }
            )
            PremiumPlanOption(
                title = stringResource(R.string.premium_plan_lifetime),
                price = billingState.lifetimePrice ?: priceUnavailable,
                billingPeriod = stringResource(R.string.premium_billing_period_lifetime),
                selected = selectedPlan == PremiumPlan.Lifetime,
                enabled = billingState.lifetimePrice != null && !isPremium,
                onClick = { onPlanSelected(PremiumPlan.Lifetime) }
            )
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.default))
            Button(
                onClick = onContinue,
                enabled = !billingState.isLoadingOffers && !billingState.isPurchasing && !isPremium &&
                    selectedPlan.hasOffer(billingState),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MaterialTheme.spacing.primaryButtonHeight)
            ) {
                Text(
                    text = stringResource(
                        if (isPremium) R.string.premium_active else R.string.premium_purchase_action
                    ),
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
    enabled: Boolean,
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
                enabled = enabled,
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
    PremiumScreen(
        onNavigateBack = {},
        billingCoordinator = PreviewPremiumBillingCoordinator,
    )
}

private fun PremiumPlan.hasOffer(state: PremiumBillingUiState): Boolean = when (this) {
    PremiumPlan.Monthly -> state.monthlyPrice != null
    PremiumPlan.Lifetime -> state.lifetimePrice != null
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

private val PreviewPremiumBillingCoordinator = object : PremiumBillingCoordinator {
    override val status = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val uiState = MutableStateFlow(
        PremiumBillingUiState(
            isLoadingOffers = false,
            monthlyPrice = "$2.00",
            lifetimePrice = "$25.00",
        )
    )
    override val purchaseEvents = MutableSharedFlow<PremiumPurchaseEvent>()

    override suspend fun refresh() = Unit

    override fun refreshOffers() = Unit

    override fun purchase(activity: Activity, plan: PremiumPlan) = Unit
}
