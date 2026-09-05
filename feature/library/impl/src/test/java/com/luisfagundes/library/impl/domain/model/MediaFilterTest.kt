package com.luisfagundes.library.impl.domain.model

import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.model.MediaFilter
import com.luisfagundes.library.api.domain.model.filterBy
import com.luisfagundes.library.api.domain.model.toMediaFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MediaFilterTest {

    private val media = listOf(
        Media(
            id = 1L,
            uri = "content://media/1",
            dateAdded = 1L,
            size = 1L,
            isVideo = false,
            bucketId = "camera",
            isFavorite = true
        ),
        Media(
            id = 2L,
            uri = "content://media/2",
            dateAdded = 2L,
            size = 2L,
            isVideo = true,
            bucketId = "camera"
        ),
        Media(
            id = 3L,
            uri = "content://media/3",
            dateAdded = 3L,
            size = 3L,
            isVideo = false,
            bucketId = "downloads"
        )
    )

    @Test
    fun `filter by id scopes media to the requested collection`() {
        assertEquals(listOf(1L), media.filterBy("favorites".toMediaFilter()).map { it.id })
        assertEquals(listOf(2L), media.filterBy("videos".toMediaFilter()).map { it.id })
        assertEquals(listOf(1L, 2L), media.filterBy("camera".toMediaFilter()).map { it.id })
        assertEquals(listOf(1L, 2L, 3L), media.filterBy(null.toMediaFilter()).map { it.id })
    }

    @Test
    fun `all filter returns the original list`() {
        assertEquals(media, media.filterBy(MediaFilter.All))
    }
}
