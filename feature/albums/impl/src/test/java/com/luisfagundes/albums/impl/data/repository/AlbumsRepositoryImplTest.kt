package com.luisfagundes.albums.impl.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.albums.impl.data.datasource.LocalAlbumsDataSource
import com.luisfagundes.albums.impl.data.model.AlbumMediaDto
import io.mockk.coEvery
import io.mockk.coVerify
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

@OptIn(ExperimentalCoroutinesApi::class)
class AlbumsRepositoryImplTest {

    @RegisterExtension
    val dispatcherRule = MainDispatcherRule(UnconfinedTestDispatcher())

    private val dataSource: LocalAlbumsDataSource = mockk()
    private val context: Context = mockk()
    private val sharedPrefs: SharedPreferences = mockk()

    private lateinit var repository: AlbumsRepositoryImpl

    @BeforeEach
    fun setUp() {
        every { context.getSharedPreferences("library_prefs", Context.MODE_PRIVATE) } returns sharedPrefs
        repository = AlbumsRepositoryImpl(
            dataSource = dataSource,
            context = context,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `getAlbums should return mapped and grouped albums excluding trashed or deleted media`() = runTest {
        // Given
        val mockUri: Uri = mockk()
        val media1 = AlbumMediaDto(id = 1L, uri = mockUri, dateAdded = 1000L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = true)
        val media2 = AlbumMediaDto(id = 2L, uri = mockUri, dateAdded = 2000L, isVideo = true, bucketDisplayName = "WhatsApp", bucketId = "wa", isFavorite = false)
        val media3 = AlbumMediaDto(id = 3L, uri = mockUri, dateAdded = 3000L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = false)
        val media4 = AlbumMediaDto(id = 4L, uri = mockUri, dateAdded = 4000L, isVideo = false, bucketDisplayName = "Trash", bucketId = "trash", isFavorite = false) // will be filtered

        coEvery { dataSource.fetchMediaList() } returns Result.success(listOf(media1, media2, media3, media4))
        
        // Preferences mocking
        every { sharedPrefs.getStringSet("trashed_photo_ids", emptySet()) } returns setOf("4")
        every { sharedPrefs.getStringSet("deleted_photo_ids", emptySet()) } returns emptySet()

        // When
        val result = repository.getAlbums()

        // Then
        assertTrue(result.isSuccess)
        val albums = result.getOrNull()!!

        // Expected albums: "Camera" (cam, 2 items), "Favorites" (favorites, 1 item), "Videos" (videos, 1 item), "WhatsApp" (wa, 1 item)
        // Sorted alphabetically: Camera -> Favorites -> Videos -> WhatsApp
        assertEquals(4, albums.size)

        assertEquals("cam", albums[0].id)
        assertEquals("Camera", albums[0].name)
        assertEquals(2, albums[0].count)

        assertEquals("favorites", albums[1].id)
        assertEquals("Favorites", albums[1].name)
        assertEquals(1, albums[1].count)

        assertEquals("videos", albums[2].id)
        assertEquals("Videos", albums[2].name)
        assertEquals(1, albums[2].count)

        assertEquals("wa", albums[3].id)
        assertEquals("WhatsApp", albums[3].name)
        assertEquals(1, albums[3].count)
    }

    @Test
    fun `getAlbumMedia should return specific album media items for physical and virtual albums`() = runTest {
        // Given
        val mockUri: Uri = mockk()
        val media1 = AlbumMediaDto(id = 1L, uri = mockUri, dateAdded = 1000L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = true)
        val media2 = AlbumMediaDto(id = 2L, uri = mockUri, dateAdded = 2000L, isVideo = true, bucketDisplayName = "WhatsApp", bucketId = "wa", isFavorite = false)
        val media3 = AlbumMediaDto(id = 3L, uri = mockUri, dateAdded = 3000L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = false)

        coEvery { dataSource.fetchMediaList() } returns Result.success(listOf(media1, media2, media3))
        every { sharedPrefs.getStringSet("trashed_photo_ids", emptySet()) } returns emptySet()
        every { sharedPrefs.getStringSet("deleted_photo_ids", emptySet()) } returns emptySet()

        // Test 1: Fetch "Camera" physical album
        val cameraMediaResult = repository.getAlbumMedia("cam")
        assertTrue(cameraMediaResult.isSuccess)
        val cameraMedia = cameraMediaResult.getOrNull()!!
        assertEquals(2, cameraMedia.size)
        assertEquals(1L, cameraMedia[0].id)
        assertEquals(3L, cameraMedia[1].id)

        // Test 2: Fetch "Favorites" virtual album
        val favoritesMediaResult = repository.getAlbumMedia("favorites")
        assertTrue(favoritesMediaResult.isSuccess)
        val favoritesMedia = favoritesMediaResult.getOrNull()!!
        assertEquals(1, favoritesMedia.size)
        assertEquals(1L, favoritesMedia[0].id)

        // Test 3: Fetch "Videos" virtual album
        val videosMediaResult = repository.getAlbumMedia("videos")
        assertTrue(videosMediaResult.isSuccess)
        val videosMedia = videosMediaResult.getOrNull()!!
        assertEquals(1, videosMedia.size)
        assertEquals(2L, videosMedia[0].id)
    }
}
