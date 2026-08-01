package com.luisfagundes.premium.impl.presentation.viewmodel

import app.cash.turbine.test
import com.luisfagundes.core.common.provider.SubscriptionStatus
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.premium.impl.domain.model.PremiumSubscriptionState
import com.luisfagundes.premium.impl.domain.model.SubscriptionOffer
import com.luisfagundes.premium.impl.domain.model.SubscriptionPlan
import com.luisfagundes.premium.impl.domain.repository.PremiumSubscriptionRepository
import com.luisfagundes.premium.impl.presentation.effect.PremiumUiEffect
import com.luisfagundes.premium.impl.presentation.event.PremiumUiEvent
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class PremiumViewModelTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val state = MutableStateFlow(
        PremiumSubscriptionState(subscriptionStatus = SubscriptionStatus.Free),
    )
    private val repository: PremiumSubscriptionRepository = mockk()

    private lateinit var viewModel: PremiumViewModel

    @BeforeEach
    fun setUp() {
        every { repository.state } returns state
        viewModel = PremiumViewModel(repository)
    }

    @Test
    fun `offer updates should select annual plan and purchase should use domain offer token`() = runTest {
        // Given
        val monthly = SubscriptionOffer("monthly", SubscriptionPlan.MONTHLY, "$5")
        val annual = SubscriptionOffer("yearly", SubscriptionPlan.YEARLY, "$40")

        // When
        state.value = state.value.copy(offers = listOf(monthly, annual))

        // Then
        assertEquals(annual.id, viewModel.uiState.value.selectedOfferId)

        viewModel.uiEffect.test {
            // When
            viewModel.dispatchEvent(PremiumUiEvent.PurchaseClick)

            // Then
            assertEquals(PremiumUiEffect.LaunchPurchase(annual.id), awaitItem())
        }
    }

    @Test
    fun `load event refreshes through the premium domain repository`() = runTest {
        // Given
        coEvery { repository.refresh() } returns Unit

        // When
        viewModel.dispatchEvent(PremiumUiEvent.Load)

        // Then
        coVerify(exactly = 1) { repository.refresh() }
    }
}
