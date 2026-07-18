package com.luisfagundes.library.impl.data.mapper

import com.luisfagundes.library.impl.tools.fakeMedia
import com.luisfagundes.library.impl.tools.fakeStatistics
import com.luisfagundes.library.impl.tools.fakeStatisticsEntity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StatisticsMapperTest {

    private val mapper = StatisticsMapper()

    @Test
    fun `mapToDomain should map entity fields correctly`() {
        // Given
        val entity = fakeStatisticsEntity

        // When
        val statistics = mapper.mapToDomain(entity)

        // Then
        assertEquals(fakeStatistics, statistics)
    }

    @Test
    fun `mapToDomain should return default stats when entity is null`() {
        // When
        val statistics = mapper.mapToDomain(null)

        // Then
        assertEquals(
            fakeStatistics.copy(memoryCleared = 0L, mediaDeleted = 0, photosDeleted = 0, videosDeleted = 0),
            statistics
        )
    }

    @Test
    fun `mapToUpdatedEntity should calculate deleted media values and add them to existing entity`() {
        // Given
        val currentEntity = fakeStatisticsEntity.copy(
            memoryCleared = 100L,
            mediaDeleted = 2,
            photosDeleted = 1,
            videosDeleted = 1
        )
        val deletedMedia = listOf(
            fakeMedia.copy(id = 1L, size = 50L),
            fakeMedia.copy(id = 2L, size = 150L, isVideo = true),
            fakeMedia.copy(id = 3L, size = 200L)
        )

        // When
        val updatedEntity = mapper.mapToUpdatedEntity(currentEntity, deletedMedia)

        // Then
        assertEquals(
            fakeStatisticsEntity.copy(memoryCleared = 500L, mediaDeleted = 5, photosDeleted = 3, videosDeleted = 2),
            updatedEntity
        )
    }

    @Test
    fun `mapToUpdatedEntity should construct default entity when current entity is null`() {
        // Given
        val deletedMedia = listOf(
            fakeMedia.copy(id = 1L, size = 50L),
            fakeMedia.copy(id = 2L, size = 150L, isVideo = true)
        )

        // When
        val updatedEntity = mapper.mapToUpdatedEntity(null, deletedMedia)

        // Then
        assertEquals(
            fakeStatisticsEntity.copy(memoryCleared = 200L, mediaDeleted = 2, photosDeleted = 1, videosDeleted = 1),
            updatedEntity
        )
    }
}
