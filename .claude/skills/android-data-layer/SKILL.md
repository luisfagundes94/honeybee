---
name: android-data-layer
description: Rules and guidelines for implementing the Android Data Layer, including when to use DataSources vs Repository direct access, mandatory Result<T> handling, and using extension functions instead of Mapper classes.
---

## DataSources Cannot Be Pass-Through

DataSource (`RemoteDataSource`, `LocalDataSource`) is **optional** in the `data` layer. If it merely
delegates calls to `ApiService`, `Dao`, or `DataStore` without adding behavior → do NOT create a
DataSource. `RepositoryImpl` accesses the client directly.

```kotlin
// ✅ CORRECT — Remote-only feature, without DataSource
internal class ItemsRepositoryImpl(
    private val api: ItemsApi
) : ItemsRepository {
    override suspend fun getItems(): Result<List<Item>> = runCatching {
        api.getItems().items.map { it.toDomain() }
    }
}

// ✅ CORRECT — Repository with two DataSources (remote + local), each aggregating behavior
internal class ItemsRemoteDataSource(
    private val api: ItemsApi
) {
    suspend fun getItems(): List<ItemResponse> = api.getItems().items
}

internal class ItemsLocalDataSource(
    private val dao: ItemsDao
) {
    suspend fun getAll(): List<ItemEntity> = dao.getAll()
    suspend fun save(items: List<ItemEntity>) = dao.insertAll(items)
    suspend fun isStale(): Boolean = dao.lastUpdate()?.let {
        System.currentTimeMillis() - it > CACHE_TTL
    } ?: true
}

internal class ItemsRepositoryImpl(
    private val remote: ItemsRemoteDataSource,
    private val local: ItemsLocalDataSource
) : ItemsRepository {
    override suspend fun getItems(): Result<List<Item>> = runCatching {
        if (local.isStale()) {
            val remoteItems = remote.getItems()
            local.save(remoteItems.map { it.toEntity() })
            remoteItems.map { it.toDomain() }
        } else {
            local.getAll().map { it.toDomain() }
        }
    }
}

// ❌ WRONG — Proxy/anemic DataSource (only delegates without logic)
internal class ItemsRemoteDataSource(
    private val api: ItemsApi
) {
    suspend fun getItems(): ItemsResponse = api.getItems()
}

internal class ItemsRepositoryImpl(
    private val remote: ItemsRemoteDataSource
) : ItemsRepository {
    override suspend fun getItems(): Result<List<Item>> = runCatching {
        remote.getItems().items.map { it.toDomain() }
    }
}
```

Checklist (at least 1 = YES to justify DataSource):

- Coordinates multiple sources (remote + local/cache)?
- Implements cache policy (stale time, invalidation, write-through)?
- Performs fallback/resiliency (retry/backoff, offline-first, circuit breaker)?
- Is reused by multiple repositories/features?
- Encapsulates relevant payload transformation/normalization?
- Client is a concrete class difficult to mock (proprietary SDK, singleton)?

If NONE of the above apply → RepositoryImpl accesses the client directly.

Verification: Review Remediation: Remove anemic DataSource and inject the client directly into
RepositoryImpl

## Result<T> With Full Handling

Every operation returning Result<T> MUST handle BOTH cases.

```kotlin
// ✅ ALWAYS
repository.getData()
    .onSuccess { _state.value = UiState.Success(it) }
    .onFailure { _state.value = UiState.Error(it.message) }

// ❌ NEVER
repository.getData().getOrThrow()
```

Verification: Review + compilation Remediation: Add .onFailure with UiState.Error

## Mapper Classes Are Prohibited — Use Extension Functions

Do not create `Mapper<S, T>` classes for transformation between layer models. Conversion from
Response/Entity to Domain is done via `.toDomain()` extension function.

```kotlin
// ✅ CORRECT — Private extension function in Repository (exclusive use)
internal class ItemsRepositoryImpl(
    private val api: ItemsApi
) : ItemsRepository {

    override suspend fun getItems(): Result<List<Item>> = runCatching {
        api.getItems().items.map { it.toDomain() }
    }

    private fun ItemResponse.toDomain() = Item(
        id = id,
        name = name,
        status = ItemStatus.from(statusCode)
    )
}

// ✅ CORRECT — Extension function in Response file (when reused by multiple Repositories)
// File: data/model/ItemResponse.kt
@Serializable
data class ItemResponse(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("status_code") val statusCode: Int,
)

internal fun ItemResponse.toDomain() = Item(
    id = id,
    name = name,
    status = ItemStatus.from(statusCode)
)

// ❌ PROHIBITED — Mapper Class
internal class ItemResponseToDomainMapper : Mapper<ItemResponse, Item> {
    override fun map(source: ItemResponse): Item {}
}
```
