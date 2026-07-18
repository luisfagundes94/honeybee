package com.luisfagundes.library.impl.data.repository

import android.content.Context
import android.net.Uri
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.impl.data.database.dao.StatisticsDao
import com.luisfagundes.library.impl.data.database.entity.StatisticsEntity
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.mapper.MediaMapper
import com.luisfagundes.library.impl.data.mapper.StatisticsMapper
import com.luisfagundes.library.impl.data.model.MediaDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val dataSource: LibraryDataSource = mockk()
    private val mediaMapper = MediaMapper()
    private val preferences: LibraryPreferences = mockk()
    private val context: Context = mockk()
    private val statisticsDao: StatisticsDao = mockk(relaxed = true)
    private val statisticsMapper = StatisticsMapper()

    private lateinit var repository: LibraryRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = LibraryRepositoryImpl(
            dataSource = dataSource,
            mediaMapper = mediaMapper,
            preferences = preferences,
            statisticsDao = statisticsDao,
            statisticsMapper = statisticsMapper,
            context = context,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `getActiveMedia should return active media`() = runTest {
        // Given
        val mockUri1: Uri = mockk()
        val mockUri2: Uri = mockk()
        val mockUri3: Uri = mockk()

        val may1Time = Instant.parse("2026-05-01T10:00:00Z").epochSecond
        val may15Time = Instant.parse("2026-05-15T10:00:00Z").epochSecond
        val june10Time = Instant.parse("2026-06-10T10:00:00Z").epochSecond

        val media1 = MediaDto(id = 1L, uri = mockUri1, dateAdded = may15Time, size = 100L, isVideo = false)
        val media2 = MediaDto(id = 2L, uri = mockUri2, dateAdded = june10Time, size = 200L, isVideo = true)
        val media3 = MediaDto(id = 3L, uri = mockUri3, dateAdded = may1Time, size = 300L, isVideo = false)

        coEvery { dataSource.fetchMediaList() } returns Result.success(listOf(media1, media2, media3))
        every { preferences.getTrashedPhotoIds() } returns emptySet()
        every { preferences.getDeletedPhotoIds() } returns emptySet()

        // When
        val result = repository.getActiveMedia()

        // Then
        assertTrue(result.isSuccess)
        val mediaList = result.getOrDefault(emptyList())
        assertEquals(3, mediaList.size)
    }

    @Test
    fun `permanentlyDelete should update statistics and call content resolver`() = runTest {
        // Given
        val mediaId = 1L
        val mockUri: Uri = mockk()
        val mediaDto = MediaDto(id = mediaId, uri = mockUri, dateAdded = 0L, size = 100L, isVideo = false)
        val media = Media(id = mediaId, uri = mockUri, dateAdded = 0L, size = 100L, isVideo = false)

        coEvery { dataSource.fetchMediaList() } returns Result.success(listOf(mediaDto))
        every { preferences.getTrashedPhotoIds() } returns setOf(mediaId)
        every { preferences.getDeletedPhotoIds() } returns emptySet()
        every { preferences.setTrashedPhotoIds(any()) } returns Unit
        every { preferences.setDeletedPhotoIds(any()) } returns Unit

        val mockContentResolver: android.content.ContentResolver = mockk(relaxed = true)
        every { context.contentResolver } returns mockContentResolver
        every { statisticsDao.getStatistics() } returns null
        every { statisticsDao.insertOrUpdate(any()) } returns 1L

        // When
        repository.permanentlyDelete(listOf(media))

        // Then
        verify {
            statisticsDao.insertOrUpdate(
                StatisticsEntity(
                    id = 1,
                    memoryCleared = 100L,
                    mediaDeleted = 1,
                    photosDeleted = 1,
                    videosDeleted = 0
                )
            )
        }
    }

    @Test
    fun `getStatistics should return statistics successfully`() = runTest {
        // Given
        val expectedEntity = StatisticsEntity(
            id = 1,
            memoryCleared = 200L,
            mediaDeleted = 2,
            photosDeleted = 1,
            videosDeleted = 1
        )
        coEvery { statisticsDao.getStatistics() } returns expectedEntity

        // When
        val result = repository.getStatistics()

        // Then
        assertTrue(result.isSuccess)
        val stats = result.getOrNull()!!
        assertEquals(200L, stats.memoryCleared)
        assertEquals(2, stats.mediaDeleted)
        assertEquals(1, stats.photosDeleted)
        assertEquals(1, stats.videosDeleted)
    }
}
