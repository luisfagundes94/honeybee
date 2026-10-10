package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.runtime.MonotonicFrameClock
import androidx.compose.ui.geometry.Offset
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.tools.fakeMediaCardBounds
import com.luisfagundes.library.impl.tools.fakeTrashIconBounds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@OptIn(ExperimentalCoroutinesApi::class)
internal class MediaSwipeStateTest {
    @RegisterExtension
    val dispatcherRule = MainDispatcherRule()

    private val state = MediaSwipeState().apply { cardBounds = fakeMediaCardBounds }

    @Test
    fun `media starts shrinking with the first upward movement and keeps shrinking during drag`() {
        // Given
        val initialScale = state.dragScale(fakeTrashIconBounds)

        // When
        state.dragBy(-1f)
        val firstDragScale = state.dragScale(fakeTrashIconBounds)
        state.dragBy(-100f)
        val furtherDragScale = state.dragScale(fakeTrashIconBounds)

        // Then
        assertEquals(1f, initialScale)
        assertTrue(firstDragScale < initialScale)
        assertTrue(furtherDragScale < firstDragScale)
        assertFalse(state.hasReachedTrashThreshold(fakeTrashIconBounds))
    }

    @Test
    fun `threshold follows the distance to the trash icon on different screen sizes`() {
        // Given
        val upwardDistance = fakeMediaCardBounds.center.y - fakeTrashIconBounds.center.y

        // When
        state.dragBy(-upwardDistance * 0.50f)

        // Then
        assertTrue(state.hasReachedTrashThreshold(fakeTrashIconBounds))
        state.cardBounds = fakeMediaCardBounds.translate(Offset(0f, upwardDistance))
        assertFalse(state.hasReachedTrashThreshold(fakeTrashIconBounds))
    }

    @Test
    fun `swipes below the threshold return to their starting position`() = runTest {
        // Given
        val scope = animationScope()
        state.dragBy(-100f)
        assertFalse(state.hasReachedTrashThreshold(fakeTrashIconBounds))

        // When
        state.returnToStart(scope)
        advanceUntilIdle()

        // Then
        assertEquals(0f, state.dragOffset)
        assertEquals(1f, state.dragScale(fakeTrashIconBounds))
        assertFalse(state.isTrashing)
    }

    @Test
    fun `repeated trash requests dispatch once after the animation finishes`() = runTest {
        // Given
        val scope = animationScope()
        var trashRequests = 0
        state.dragBy(-400f)

        // When
        repeat(2) {
            state.animateToTrash(fakeTrashIconBounds, scope) { trashRequests++ }
        }
        advanceTimeBy(100)
        runCurrent()

        // Then
        assertTrue(state.isTrashing)
        assertEquals(0, trashRequests)
        assertTrue(state.trashProgress.value > 0f && state.trashProgress.value < 1f)
        advanceUntilIdle()
        assertEquals(1f, state.trashProgress.value)
        assertEquals(1, trashRequests)
    }

    @Test
    fun `new drag and cancellation cannot interrupt a committed trash animation`() = runTest {
        // Given
        val scope = animationScope()
        var trashRequests = 0
        state.dragBy(-400f)
        val handoffScale = state.dragScale(fakeTrashIconBounds)
        state.animateToTrash(fakeTrashIconBounds, scope) { trashRequests++ }

        // When
        state.startDrag()
        state.dragBy(100f)
        state.returnToStart(scope)
        advanceUntilIdle()

        // Then
        assertEquals(-400f, state.dragOffset)
        assertEquals(handoffScale, state.dragScale(fakeTrashIconBounds))
        assertEquals(1f, state.trashProgress.value)
        assertEquals(1, trashRequests)
    }

    private fun TestScope.animationScope(): CoroutineScope {
        val frameClock = object : MonotonicFrameClock {
            override suspend fun <R> withFrameNanos(onFrame: (Long) -> R): R {
                delay(16)
                return onFrame(testScheduler.currentTime * 1_000_000)
            }
        }
        return CoroutineScope(coroutineContext + frameClock)
    }
}
