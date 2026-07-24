package com.luisfagundes.premium.impl.presentation.screen

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luisfagundes.core.common.presentation.arch.compose.CollectUiEffects
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.core.designsystem.components.HoneybeePrimaryButton
import com.luisfagundes.core.designsystem.theme.spacing
import com.luisfagundes.premium.impl.R
import com.luisfagundes.premium.impl.data.PlayBillingSubscriptionProvider
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import com.luisfagundes.premium.impl.presentation.effect.PremiumUiEffect
import com.luisfagundes.premium.impl.presentation.event.PremiumUiEvent
import com.luisfagundes.premium.impl.presentation.state.PremiumUiState
import com.luisfagundes.premium.impl.presentation.viewmodel.PremiumViewModel

@Composable
internal fun PremiumScreen(
    onNavigateBack: () -> Unit,
    subscriptionProvider: PlayBillingSubscriptionProvider,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activity = LocalActivity.current

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            PremiumUiEffect.NavigateBack -> onNavigateBack()
            is PremiumUiEffect.LaunchPurchase -> activity?.let {
                subscriptionProvider.launchPurchase(it, effect.offerToken)
            }
            PremiumUiEffect.OpenSubscriptionManagement -> activity?.let {
                subscriptionProvider.openSubscriptionManagement(it)
            }
        }
    }

    LaunchedEffect(Unit) { viewModel.dispatchEvent(PremiumUiEvent.Load) }
    PremiumScreen(uiState = uiState, onEvent = viewModel::dispatchEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PremiumScreen(
    uiState: PremiumUiState,
    onEvent: (PremiumUiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.premium_title),
                        modifier = Modifier.semantics { heading() },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(PremiumUiEvent.BackClick) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.premium_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(MaterialTheme.spacing.default),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.default),
        ) {
            Text(
                text = stringResource(R.string.premium_description),
                style = MaterialTheme.typography.bodyLarge,
            )
            when (uiState.subscriptionStatus) {
                SubscriptionStatus.Premium -> PremiumActiveContent(onEvent)
                SubscriptionStatus.Loading -> Text(stringResource(R.string.premium_loading))
                SubscriptionStatus.Free -> PremiumOffersContent(uiState, onEvent)
            }
        }
    }
}

@Composable
private fun PremiumActiveContent(onEvent: (PremiumUiEvent) -> Unit) {
    Text(
        text = stringResource(R.string.premium_active),
        style = MaterialTheme.typography.headlineSmall,
    )
    HoneybeePrimaryButton(
        label = stringResource(R.string.premium_manage),
        onClick = { onEvent(PremiumUiEvent.ManageSubscriptionClick) }
    )
}

@Composable
private fun PremiumOffersContent(
    uiState: PremiumUiState,
    onEvent: (PremiumUiEvent) -> Unit,
) {
    if (uiState.isPurchasePending) {
        Text(
            text =
                stringResource(R.string.premium_pending)
        )
        HoneybeePrimaryButton(
            label = stringResource(R.string.premium_restore),
            onClick = { onEvent(PremiumUiEvent.RestoreClick) }
        )
        return
    }

    if (uiState.offers.isEmpty()) {
        Text(
            text = stringResource(R.string.premium_no_offers)
        )
        HoneybeePrimaryButton(
            label = stringResource(R.string.premium_restore),
            onClick = { onEvent(PremiumUiEvent.RestoreClick) }
        )
        return
    }

    uiState.offers.forEach { offer ->
        OfferCard(
            offer = offer,
            selected = offer.id == uiState.selectedOfferId,
            onClick = { onEvent(PremiumUiEvent.OfferSelected(offer.id)) },
        )
    }
    HoneybeePrimaryButton(
        label = stringResource(R.string.premium_purchase),
        onClick = { onEvent(PremiumUiEvent.PurchaseClick) },
        modifier = Modifier.fillMaxWidth(),
    )
    HoneybeePrimaryButton(
        label = stringResource(R.string.premium_restore),
        onClick = { onEvent(PremiumUiEvent.RestoreClick) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun OfferCard(
    offer: SubscriptionOffer,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainer
            },
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.default),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            Text(
                text = stringResource(
                    if (offer.plan == SubscriptionPlan.ANNUAL) R.string.premium_annual else R.string.premium_monthly,
                ),
                modifier = Modifier.weight(1f),
            )
            Text(text = offer.formattedPrice)
        }
    }
}
