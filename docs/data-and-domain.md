# Data and Domain

## Use Cases Must Not Be Pass-Through

A UseCase MUST contain substantial business rules.
If it only delegates to a Repository without adding logic → do not create a UseCase.
UseCases MUST be concrete classes, NOT interfaces with a single implementation.

```kotlin
// ✅ CORRECT — UseCase with business logic
class TransferMoneyUseCase(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val limitsValidator: LimitsValidator
) {
    suspend operator fun invoke(params: TransferParams): Result<Transaction> {
        // Business rule validation
        val sourceAccount = accountRepository.getAccount(params.sourceId)
            .getOrElse { return Result.failure(it) }

        if (sourceAccount.balance < params.amount) {
            return Result.failure(InsufficientBalanceException())
        }

        // Limit validation
        if (!limitsValidator.isWithinDailyLimit(params.amount)) {
            return Result.failure(DailyLimitExceededException())
        }

        // Composite operation orchestration
        return transactionRepository.executeTransfer(params)
    }
}

// ✅ CORRECT — ViewModel accesses the Repository directly
// for a simple operation with no business logic
internal class ItemListViewModel(
    private val repository: ItemRepository // interface defined in the domain layer
) : ViewModel() {
    private val _uiState = MutableStateFlow<ItemUiState>(ItemUiState.Initial)
    val uiState: StateFlow<ItemUiState> = _uiState.asStateFlow()

    fun loadItems() {
        viewModelScope.launch {
            _uiState.value = ItemUiState.Loading
            repository.getItems()
                .onSuccess { _uiState.value = ItemUiState.Success(it) }
                .onFailure { _uiState.value = ItemUiState.Error(it.message) }
        }
    }
}

// ❌ WRONG — Anemic UseCase that only delegates without adding logic
class GetItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): Result<List<Item>> = repository.getItems()
}

// ❌ WRONG — ViewModel receives a concrete data-layer implementation
internal class ItemListViewModel(
    private val repository: ItemRepositoryImpl // FORBIDDEN: concrete implementation
) : ViewModel() { ... }

// ❌ WRONG — Interface + Impl with only one implementation
interface TransferMoneyUseCase {
    suspend operator fun invoke(params: TransferParams): Result<Transaction>
}
class TransferMoneyUseCaseImpl(...) : TransferMoneyUseCase { ... }
```

Checklist — at least one item must be YES to justify a UseCase:

- Does it orchestrate multiple repositories or data sources?
- Does it contain business rule validations?
- Does it implement complex transformation logic?
- Does it manage composite operations?
- Does it apply business policies such as limits or permissions?

If NONE of the items above apply → the ViewModel accesses the Repository directly through its domain interface.
This does NOT violate Clean Architecture because the ViewModel depends on the Repository **interface**, defined in the domain layer, rather than its implementation in the data layer.

**Rule:** A ViewModel MUST receive the Repository interface, such as `ItemRepository`, and NEVER the concrete implementation, such as `ItemRepositoryImpl`. Injecting the implementation is the responsibility of the DI module (`ScopeModule`).

Remediation: Remove the anemic UseCase and make the ViewModel call the Repository interface directly.

## Complete Result<T> Handling

Every operation that returns `Result<T>` MUST handle BOTH cases.

```kotlin
// ✅ ALWAYS
repository.getData()
    .onSuccess { _state.value = UiState.Success(it) }
    .onFailure { _state.value = UiState.Error(it.message) }

// ❌ NEVER
repository.getData().getOrThrow()
```

Remediation: Add `.onFailure` with `UiState.Error`.

## Do Not Use Data Classes for Domain and Data Entities

Entities in the domain and data layers MUST use `class`, NOT `data class`.
`data class` is allowed only in the presentation layer for UI models, `UiState`, and Args.

The vast majority of data classes in the domain and data layers do not use the generated `copy()`, `componentN()`, `toString()`, or `equals()`/`hashCode()` methods, causing unnecessary binary-size and build-time overhead.

```kotlin
// ✅ CORRECT — Domain-layer entity as a class
class Transaction(
    val id: String,
    val amount: BigDecimal,
    val status: TransactionStatus
)

// ✅ CORRECT — Data-layer entity as a class
class TransactionResponse(
    val id: String,
    val amount: String,
    val status: String
)

// ✅ CORRECT — Presentation UI model as a data class
data class TransactionState(
    val id: String,
    val formattedAmount: String,
    val statusLabel: String
)

// ❌ WRONG — Domain-layer entity as a data class
data class Transaction(
    val id: String,
    val amount: BigDecimal,
    val status: TransactionStatus
)

// ❌ WRONG — Data-layer entity as a data class
data class TransactionResponse(
    val id: String,
    val amount: String,
    val status: String
)
```

| Layer | Use `data class`? |
|-------|:-----------------:|
| Domain — entities and value objects | ❌ No |
| Data — DTOs, responses, and requests | ❌ No |
| Presentation — UiState, UI models, and Args | ✅ Yes |

Remediation: Change `data class` to `class` for entities in the domain and data layers.

## A DataSource Must Not Be Pass-Through

A DataSource, such as `RemoteDataSource` or `LocalDataSource`, is **optional** in the `data` layer.
If it only delegates calls to an `ApiService`, `Dao`, or `DataStore` without adding behavior → do NOT create a DataSource. `RepositoryImpl` should access the client directly.

```kotlin
// ✅ CORRECT — Remote-only feature without a DataSource
internal class ItemsRepositoryImpl(
    private val api: ItemsApi
) : ItemsRepository {
    override suspend fun getItems(): Result<List<Item>> = runCatching {
        api.getItems().items.map { it.toDomain() }
    }
}

// ✅ CORRECT — Repository with two DataSources, remote + local,
// where each one adds behavior
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

// ❌ WRONG — Anemic proxy DataSource that only delegates without logic
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

Checklist — at least one item must be YES to justify a DataSource:

- Does it coordinate multiple sources, such as remote + local/cache?
- Does it implement a cache policy, such as staleness, invalidation, or write-through?
- Does it provide fallback or resilience, such as retry/backoff, offline-first, or a circuit breaker?
- Is it reused by multiple repositories or features?
- Does it encapsulate meaningful payload transformation or normalization?
- Is the client a concrete class that is difficult to mock, such as a proprietary SDK or singleton?

If NONE of the items above apply → `RepositoryImpl` accesses the client directly.

Remediation: Remove the anemic DataSource and inject the client directly into `RepositoryImpl`.

## Mapper Classes Are Forbidden — Use Extension Functions

Do not create mapper classes to transform models between layers.
Conversion from Response or Entity to Domain must be performed through a `.toDomain()` extension function.

```kotlin
// ✅ CORRECT — Private extension function inside the Repository
// when used exclusively there
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

// ✅ CORRECT — Extension function in the Response file
// when reused by multiple Repositories
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

// ❌ FORBIDDEN — Mapper class
internal class ItemResponseToDomainMapper  {
    override fun map(source: ItemResponse): Item { ... }
}
```

**Rules:**

- If the Response is used by **a single Repository** → use a `private fun` inside the Repository.
- If the Response is reused by **multiple Repositories** → use an `internal` extension function in the same file as the Response.
- The Repository is responsible for calling `.toDomain()` — never the DataSource.
- Domain models MUST NOT have conversion extensions because the domain layer does not know about the data layer.
- Transformation logic, including enums, formatting, and defaults, belongs inside the extension.
- Do not register it in Hilt — call it directly without injection.

**Checklist:**

- Is the extension function in the data layer? ✅
- Does it return a domain-layer model? ✅
- Is it called by the Repository rather than the DataSource? ✅

Remediation: Convert the existing Mapper class into an extension function and remove its Koin registration.
