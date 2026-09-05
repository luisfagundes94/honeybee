package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.Media
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AlbumTransformationsTest {

    @Test
    fun `toAlbums groups physical albums, adds virtual albums, and sorts by name`() {
        // Given
        val cameraMedia = Media(
            id = 1L,
            uri = "content://camera/1",
            dateAdded = 1_000L,
            size = 100L,
            isVideo = false,
            bucketId = "camera",
            bucketDisplayName = "Camera",
            isFavorite = true
        )
        val media = listOf(
            cameraMedia,
            cameraMedia.copy(
                id = 2L,
                uri = "content://camera/2",
                dateAdded = 2_000L,
                size = 200L,
                isVideo = true,
                isFavorite = false,
            ),
            cameraMedia.copy(
                id = 3L,
                uri = "content://downloads/3",
                dateAdded = 3_000L,
                size = 300L,
                bucketId = "downloads",
                bucketDisplayName = "Downloads",
                isFavorite = false,
            )
        )

        // When
        val albums = media.toAlbums()

        // Then
        assertEquals(listOf("camera", "downloads", "favorites", "videos"), albums.map { it.id })
        assertEquals(2, albums[0].count)
        assertEquals("content://camera/1", albums[0].coverUri)
        assertEquals(1, albums[1].count)
        assertEquals(1, albums[2].count)
        assertEquals(1, albums[3].count)
    }

}
