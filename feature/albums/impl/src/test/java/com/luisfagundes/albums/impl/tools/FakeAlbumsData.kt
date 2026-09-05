package com.luisfagundes.albums.impl.tools

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.library.api.domain.model.Media

internal val fakeAlbum = Album.Physical(
    id = "camera_id",
    name = "Camera",
    count = 10,
    coverUri = "content://media/external/images/media/1"
)

internal val fakeAlbumMedia = Media(
    id = 1L,
    uri = "content://media/external/images/media/1",
    dateAdded = 1_000L,
    size = 0L,
    isVideo = false
)
