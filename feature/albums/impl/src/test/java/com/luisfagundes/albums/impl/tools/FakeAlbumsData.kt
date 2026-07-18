package com.luisfagundes.albums.impl.tools

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia
import io.mockk.mockk

internal val fakeAlbum = Album.Physical(
    id = "camera_id",
    name = "Camera",
    count = 10,
    coverUri = mockk(),
    isVideo = false
)

internal val fakeAlbumMedia = AlbumMedia(
    id = 1L,
    uri = mockk(),
    dateAdded = 1_000L,
    isVideo = false
)
