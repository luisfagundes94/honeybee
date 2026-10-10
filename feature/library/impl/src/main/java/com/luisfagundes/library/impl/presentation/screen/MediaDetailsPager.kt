package com.luisfagundes.library.impl.presentation.screen

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.presentation.event.MediaDetailsUiEvent

@Composable
internal fun SharedTransitionScope.MediaDetailsPager(
    mediaList: List<Media>,
    pagerState: PagerState,
    swipeState: MediaSwipeState,
    trashIconBounds: Rect?,
    onEvent: (MediaDetailsUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalPager(
        state = pagerState,
        key = { mediaList[it].id },
        userScrollEnabled = swipeState.dragOffset == 0f && !swipeState.isTrashing,
        modifier = modifier
    ) { page ->
        mediaList.getOrNull(page)?.let { media ->
            val isPageSelected = page == pagerState.currentPage
            val pageSwipeState = remember(media.id) { MediaSwipeState() }
            val activeSwipeState = if (isPageSelected) swipeState else pageSwipeState

            MediaPagerItem(
                media = media,
                isPageSelected = isPageSelected,
                swipeState = activeSwipeState,
                trashIconBounds = trashIconBounds,
                cardOverlayModifier = Modifier.renderInSharedTransitionScopeOverlay {
                    activeSwipeState.dragOffset != 0f || activeSwipeState.isTrashing
                },
                onEvent = onEvent
            )
        }
    }
}
