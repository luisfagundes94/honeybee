package com.luisfagundes.library.impl.data.repository

import android.content.Context
import android.net.Uri
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.library.impl.data.datasource.LibraryDataSource
import com.luisfagundes.library.impl.data.datasource.LibraryPreferences
import com.luisfagundes.library.impl.data.mapper.PhotoMapper
import com.luisfagundes.library.impl.data.model.PhotoDto
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.time.Instant
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val dataSource: LibraryDataSource = mockk()
    private val photoMapper = PhotoMapper()
    private val preferences: LibraryPreferences = mockk()
    private val context: Context = mockk()

    private lateinit var repository: LibraryRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = LibraryRepositoryImpl(
            dataSource = dataSource,
            photoMapper = photoMapper,
            preferences = preferences,
            context = context,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `getPhotosByMonth should return sorted photo sections and sorted photos`() = runTest {
        // Given
        val mockUri1: Uri = mockk()
        val mockUri2: Uri = mockk()
        val mockUri3: Uri = mockk()

        val may1Time = Instant.parse("2026-05-01T10:00:00Z").epochSecond
        val may15Time = Instant.parse("2026-05-15T10:00:00Z").epochSecond
        val june10Time = Instant.parse("2026-06-10T10:00:00Z").epochSecond

        val photo1 = PhotoDto(id = 1L, uri = mockUri1, dateAdded = may15Time, size = 100L)
        val photo2 = PhotoDto(id = 2L, uri = mockUri2, dateAdded = june10Time, size = 200L)
        val photo3 = PhotoDto(id = 3L, uri = mockUri3, dateAdded = may1Time, size = 300L)

        coEvery { dataSource.fetchPhotoList() } returns Result.success(listOf(photo1, photo2, photo3))
        every { preferences.getTrashedPhotoIds() } returns emptySet()
        every { preferences.getDeletedPhotoIds() } returns emptySet()

        // When
        val result = repository.getPhotosByMonth()

        // Then
        assertTrue(result.isSuccess)
        val sections = result.getOrNull()!!
        assertEquals(2, sections.size)

        // The most recent month (June 2026) must be first
        assertEquals(YearMonth.of(2026, 6), sections[0].yearMonth)
        assertEquals(1, sections[0].photos.size)
        assertEquals(2L, sections[0].photos[0].id)

        // The older month (May 2026) must be second
        assertEquals(YearMonth.of(2026, 5), sections[1].yearMonth)
        assertEquals(2, sections[1].photos.size)

        // Within May 2026, the most recent photo (May 15th) must be first
        assertEquals(1L, sections[1].photos[0].id)
        assertEquals(3L, sections[1].photos[1].id)
    }
}
