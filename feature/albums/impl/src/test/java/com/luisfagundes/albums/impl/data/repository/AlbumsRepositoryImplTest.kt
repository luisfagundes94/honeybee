package com.luisfagundes.albums.impl.data.repository

import android.net.Uri
import com.luisfagundes.core.testing.MainDispatcherRule
import com.luisfagundes.albums.impl.data.mapper.AlbumMapper
import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import com.luisfagundes.library.api.domain.model.Media
import com.luisfagundes.library.api.domain.repository.LibraryRepository
import io.mockk.coEvery
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

    private val libraryRepository: LibraryRepository = mockk()
    private val albumMapper = AlbumMapper()

    private lateinit var repository: AlbumsRepositoryImpl

    @BeforeEach
    fun setUp() {
        repository = AlbumsRepositoryImpl(
            libraryRepository = libraryRepository,
            albumMapper = albumMapper,
            dispatcher = dispatcherRule.testDispatcher
        )
    }

    @Test
    fun `getAlbums should return mapped and grouped albums`() = runTest {
        // Given
        val mockUri: Uri = mockk()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 100L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = true)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 2000L, size = 200L, isVideo = true, bucketDisplayName = "WhatsApp", bucketId = "wa", isFavorite = false)
        val media3 = Media(id = 3L, uri = mockUri, dateAdded = 3000L, size = 300L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = false)

        coEvery { libraryRepository.getActiveMedia() } returns Result.success(listOf(media1, media2, media3))

        // When
        val result = repository.getAlbums()

        // Then
        assertTrue(result.isSuccess)
        val albums = result.getOrNull()!!

        // Expected albums: "Camera" (cam, 2 items), "Favorites" (favorites, 1 item), "Videos" (videos, 1 item), "WhatsApp" (wa, 1 item)
        // Sorted alphabetically by sort name: Camera -> Favorites -> Videos -> WhatsApp
        assertEquals(4, albums.size)

        assertTrue(albums[0] is Album.Physical)
        val cameraAlbum = albums[0] as Album.Physical
        assertEquals("cam", cameraAlbum.id)
        assertEquals("Camera", cameraAlbum.name)
        assertEquals(2, cameraAlbum.count)

        assertTrue(albums[1] is Album.Virtual.Favorites)
        val favoritesAlbum = albums[1] as Album.Virtual.Favorites
        assertEquals(Album.Virtual.Favorites.ID, favoritesAlbum.id)
        assertEquals(1, favoritesAlbum.count)

        assertTrue(albums[2] is Album.Virtual.Videos)
        val videosAlbum = albums[2] as Album.Virtual.Videos
        assertEquals(Album.Virtual.Videos.ID, videosAlbum.id)
        assertEquals(1, videosAlbum.count)

        assertTrue(albums[3] is Album.Physical)
        val waAlbum = albums[3] as Album.Physical
        assertEquals("wa", waAlbum.id)
        assertEquals("WhatsApp", waAlbum.name)
        assertEquals(1, waAlbum.count)
    }

    @Test
    fun `getAlbumMedia should return specific album media items for physical and virtual albums`() = runTest {
        // Given
        val mockUri: Uri = mockk()
        val media1 = Media(id = 1L, uri = mockUri, dateAdded = 1000L, size = 100L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = true)
        val media2 = Media(id = 2L, uri = mockUri, dateAdded = 2000L, size = 200L, isVideo = true, bucketDisplayName = "WhatsApp", bucketId = "wa", isFavorite = false)
        val media3 = Media(id = 3L, uri = mockUri, dateAdded = 3000L, size = 300L, isVideo = false, bucketDisplayName = "Camera", bucketId = "cam", isFavorite = false)

        coEvery { libraryRepository.getActiveMedia() } returns Result.success(listOf(media1, media2, media3))

        // Test 1: Fetch "Camera" physical album
        val cameraMediaResult = repository.getAlbumMedia("cam")
        assertTrue(cameraMediaResult.isSuccess)
        val cameraMedia = cameraMediaResult.getOrNull()!!
        assertEquals(2, cameraMedia.size)
        assertEquals(1L, cameraMedia[0].id)
        assertEquals(3L, cameraMedia[1].id)

        // Test 2: Fetch "Favorites" virtual album
        val favoritesMediaResult = repository.getAlbumMedia(Album.Virtual.Favorites.ID)
        assertTrue(favoritesMediaResult.isSuccess)
        val favoritesMedia = favoritesMediaResult.getOrNull()!!
        assertEquals(1, favoritesMedia.size)
        assertEquals(1L, favoritesMedia[0].id)

        // Test 3: Fetch "Videos" virtual album
        val videosMediaResult = repository.getAlbumMedia(Album.Virtual.Videos.ID)
        assertTrue(videosMediaResult.isSuccess)
        val videosMedia = videosMediaResult.getOrNull()!!
        assertEquals(1, videosMedia.size)
        assertEquals(2L, videosMedia[0].id)
    }
}
