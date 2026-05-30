package com.luisfagundes.library.impl.data.repository

import com.luisfagundes.core.common.di.DefaultDispatcher
import com.luisfagundes.impl.data.datasource.LibraryDataSource
import com.luisfagundes.impl.data.mapper.PhotoMapper
import com.luisfagundes.impl.domain.model.PhotoSection
import com.luisfagundes.impl.domain.repository.LibraryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

internal class LibraryRepositoryImpl @Inject constructor(
    private val dataSource: LibraryDataSource,
    private val photoMapper: PhotoMapper,
    @param:DefaultDispatcher private val dispatcher: CoroutineDispatcher
) : LibraryRepository {
    override suspend fun getPhotosByMonth(): Result<List<PhotoSection>> = withContext(dispatcher) {
        dataSource.fetchPhotoList().map { photos ->
            val groupedMap = photos.groupBy { photo ->
                val instant = Instant.ofEpochMilli(photo.dateAdded)
                YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            }
            groupedMap.map { (month, photosByMonth) ->
                PhotoSection(
                    yearMonth = month,
                    photos = photosByMonth.map { photoMapper.mapToDomain(it) }
                )
            }
        }
    }
}
