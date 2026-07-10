package com.luisfagundes.library.impl.data.mapper

import android.net.Uri
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.api.domain.model.Media
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StatisticsMapperTest {

    private val mapper = StatisticsMapper()

    @Test
    fun `mapToDomain should map entity fields correctly`() {
        // Given
        val entity = StatisticsEntity(
            id = 1,
            memoryCleared = 1024L,
            mediaDeleted = 5,
            photosDeleted = 3,
            videosDeleted = 2
        )

        // When
        val domain = mapper.mapToDomain(entity)

        // Then
        assertEquals(1024L, domain.memoryCleared)
        assertEquals(5, domain.mediaDeleted)
        assertEquals(3, domain.photosDeleted)
        assertEquals(2, domain.videosDeleted)
    }

    @Test
    fun `mapToDomain should return default stats when entity is null`() {
        // When
        val domain = mapper.mapToDomain(null)

        // Then
        assertEquals(0L, domain.memoryCleared)
        assertEquals(0, domain.mediaDeleted)
        assertEquals(0, domain.photosDeleted)
        assertEquals(0, domain.videosDeleted)
    }

    @Test
    fun `mapToUpdatedEntity should calculate deleted media values and add them to existing entity`() {
        // Given
        val currentEntity = StatisticsEntity(
            id = 1,
            memoryCleared = 100L,
            mediaDeleted = 2,
            photosDeleted = 1,
            videosDeleted = 1
        )
        val mockUri: Uri = mockk()
        val deletedMedia = listOf(
            Media(id = 1L, uri = mockUri, dateAdded = 0L, size = 50L, isVideo = false),
            Media(id = 2L, uri = mockUri, dateAdded = 0L, size = 150L, isVideo = true),
            Media(id = 3L, uri = mockUri, dateAdded = 0L, size = 200L, isVideo = false)
        )

        // When
        val updated = mapper.mapToUpdatedEntity(currentEntity, deletedMedia)

        // Then
        assertEquals(1, updated.id)
        assertEquals(500L, updated.memoryCleared) // 100 + 50 + 150 + 200
        assertEquals(5, updated.mediaDeleted) // 2 + 3
        assertEquals(3, updated.photosDeleted) // 1 + 2
        assertEquals(2, updated.videosDeleted) // 1 + 1
    }

    @Test
    fun `mapToUpdatedEntity should construct default entity when current entity is null`() {
        // Given
        val mockUri: Uri = mockk()
        val deletedMedia = listOf(
            Media(id = 1L, uri = mockUri, dateAdded = 0L, size = 50L, isVideo = false),
            Media(id = 2L, uri = mockUri, dateAdded = 0L, size = 150L, isVideo = true)
        )

        // When
        val updated = mapper.mapToUpdatedEntity(null, deletedMedia)

        // Then
        assertEquals(1, updated.id)
        assertEquals(200L, updated.memoryCleared)
        assertEquals(2, updated.mediaDeleted)
        assertEquals(1, updated.photosDeleted)
        assertEquals(1, updated.videosDeleted)
    }
}
