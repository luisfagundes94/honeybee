package com.luisfagundes.albums.impl.domain.usecase

import com.luisfagundes.library.api.domain.model.Media
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class AlbumTransformationsTest {

    @Test
    fun `toAlbums groups physical albums, adds virtual albums, and sorts by name`() {
        // Given
        val media = listOf(
            Media(
                id = 1L,
                uri = "content://camera/1",
                dateAdded = 1_000L,
                size = 100L,
                isVideo = false,
                bucketId = "camera",
                bucketDisplayName = "Camera",
                isFavorite = true
            ),
            Media(
                id = 2L,
                uri = "content://camera/2",
                dateAdded = 2_000L,
                size = 200L,
                isVideo = true,
                bucketId = "camera",
                bucketDisplayName = "Camera"
            ),
            Media(
                id = 3L,
                uri = "content://downloads/3",
                dateAdded = 3_000L,
                size = 300L,
                isVideo = false,
                bucketId = "downloads",
                bucketDisplayName = "Downloads"
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
        assertTrue(albums[3].isVideo)
    }

    @Test
    fun `toAlbumMedia preserves media fields needed by album details`() {
        // Given
        val media = Media(
            id = 7L,
            uri = "content://media/7",
            dateAdded = 7_000L,
            size = 700L,
            durationMillis = 12_000L,
            isVideo = true
        )

        // When
        val albumMedia = media.toAlbumMedia()

        // Then
        assertEquals(media.id, albumMedia.id)
        assertEquals(media.uri, albumMedia.uri)
        assertEquals(media.dateAdded, albumMedia.dateAdded)
        assertEquals(media.durationMillis, albumMedia.durationMillis)
        assertEquals(media.isVideo, albumMedia.isVideo)
    }
}
