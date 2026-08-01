package com.luisfagundes.albums.impl.tools

import com.luisfagundes.albums.impl.domain.model.Album
import com.luisfagundes.albums.impl.domain.model.AlbumMedia

internal val fakeAlbum = Album.Physical(
    id = "camera_id",
    name = "Camera",
    count = 10,
    coverUri = "content://media/external/images/media/1",
    isVideo = false
)

internal val fakeAlbumMedia = AlbumMedia(
    id = 1L,
    uri = "content://media/external/images/media/1",
    dateAdded = 1_000L,
    isVideo = false
)
