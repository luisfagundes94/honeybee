package com.luisfagundes.albums.impl.domain.repository

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia

internal interface AlbumsRepository {
    suspend fun getAlbums(): Result<List<Album>>
    suspend fun getAlbumMedia(albumId: String): Result<List<AlbumMedia>>
}
