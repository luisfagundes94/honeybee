# AGENTS.md

This file provides guidance to AI agents when working with code in this repository.

---

## Architecture

MVI + Clean Architecture + Clean Code

Use common design patterns when applicable
Follow best development practices (SOLID, KISS, etc)

---

## Layer Boundaries Are Absolute

```
presentation → domain   ✅ allowed
data → domain           ✅ allowed
domain → (nothing)      ✅ required (pure Kotlin)
presentation → data     ❌ FORBIDDEN
```

Remediation: Create an interface in the domain layer and implement it in the data layer.

---

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

---

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

---

## Design System

Do not use hardcoded dp values, colors, strings, or `@PreviewLightDark` in Compose screens.

| Forbidden | Required                                     |
|-----------|----------------------------------------------|
| `16.dp` | `MaterialTheme.spacing.default`                |
| `Color.White` | `MaterialTheme.colorScheme...`          |
| `"Text"` | `stringResource(R.string...)`                |
| `@Preview` | `@PreviewLightDark` + `@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)` |

Remediation: Replace it with the corresponding token. See the Tokens section in `compose-structure.md`.

---

## Sealed Classes for States

`UiState`, `UiEvent`, and `UiEffect` MUST be sealed interfaces or sealed classes.
Open data classes for states allow inconsistent state combinations.

```kotlin
// ✅ CORRECT
sealed class FeatureUiState : UiState {
    data object Loading : FeatureUiState()
    data class Success(val data: Data) : FeatureUiState()
    data class Error(val message: String) : FeatureUiState()
}

// ❌ WRONG
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: String? = null
) : UiState
```

Remediation: Convert it to a sealed class following the Cookbook MVI pattern.

---

## Coroutines for New Code

New code MUST use Coroutines + Flow.

| Context | Technology |
|---------|------------|
| New code | Coroutines + Flow ✅ |

Remediation: Use suspend functions + Flow.

---

## The Domain Layer Is Pure Kotlin

The domain layer MUST NOT depend on the Android framework.
Do not use `Context`, `Activity`, `Fragment`, `LiveData`, or `android.*` imports.

Remediation: Move Android dependencies to the data or presentation layer.

---

## Anti-Patterns That Agents Amplify

Patterns that must be corrected IMMEDIATELY because agents replicate them:

| Anti-Pattern | Why It Amplifies | Fix |
|-------------|-------------------|-----|
| Direct database query without a Repository | The agent copies the direct access | Always use a Repository |
| Magic numbers or strings | The agent does not know they are temporary | Extract them into a constant or configuration |
| Generic `catch(e: Exception)` | The agent will never become more specific | Catch specific exception types |
| Unhandled `runCatching` | The agent replicates it without `onFailure` or `getOrThrow`, causing silent failures | Always handle it with `onSuccess`/`onFailure` or `getOrElse`/`getOrThrow` |
| Wildcard imports such as `import com.picpay.*` | The agent copies the wildcard | Use explicit imports |
| Functions with 10+ parameters | The agent replicates the signature | Extract the parameters into a data class |
| `companion object` used only for constants | The agent generates unnecessary overhead | Remove the `companion object` and declare top-level constants |

Remediation: Refactor the pattern before asking an agent to work on the module.

---

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

---

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

---

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

---

## Feature Module Structure

Each feature module must have its own `data/`, `domain/`, `presentation/`, and `di/` sub-packages.

**Visibility convention**: All domain models, use cases, repositories, ViewModels, and DI modules are `internal`. Only navigation routes are public. Default to `internal` for new types.

---

## ViewModel Hierarchy

Three base classes in `core/presentation/arch/viewmodel/`:

- `ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` — use when the screen needs MVI events and one-shot side effects like navigation (effects sent via `Channel`, collected as `uiEffect`).
- `StateViewModel<State : UiState, Event : UiEvent>` — use when MVI events are needed but no side effects are required; exposes only `uiState` and handles incoming `UiEvent`s.
- `EffectViewModel<Effect : UiEffect>` — use when no MVI events are needed but one-shot side effects are required; exposes only `uiEffect` and handles incoming `UiEffect`s.`

Data flow:
1. **User Action / Events**: The Compose screen dispatches immutable events implementing `UiEvent` by calling `viewModel.dispatchEvent(event)`. All public business methods on the ViewModel are kept `private`, exposing only `dispatchEvent`.
2. **State Updates**: The ViewModel handles incoming events within `dispatchEvent(event)`. It updates the immutable `uiState` via `setState { }` or `setStateOf<T> { }` (to only update state when matching a specific subtype in a sealed hierarchy).
3. **Side Effects**: One-shot side-effects (e.g. navigation, toasts) are dispatched from the ViewModel using `sendEffect { }` and collected in Compose screens using `CollectUiEffects(viewModel.uiEffect)`.

**State updates**: Use `setState { }` for unconditional updates. Use `setStateOf<SpecificState> { }` to only update when the current state matches a specific subtype in a sealed hierarchy (e.g., only mutate if currently in `Success` state).

---

## Navigation

Uses **androidx.navigation3** (experimental Nav3 library, not the stable `androidx.navigation`/NavController).

- Routes are `@Serializable data object` or `@Serializable data class` implementing `NavKey`
- New feature routes are registered by adding `entry<RouteType> { }` blocks inside the feature's `EntryProviderScope<NavKey>.featureSection()` extension, then wiring the section call in `AppNavDisplay.kt`
- `TopLevelDestinations` enum maps top-level routes to icons/labels for the bottom bar

---

## Dependency Injection

Hilt throughout. Key qualifier annotations in `:core:common`:
- `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher`

All Hilt modules use `@InstallIn(SingletonComponent::class)`.

You can find the Hilt custom annotations at core/common/src/main/java/com/luisfagundes/core/common/di

---

### Testing

JUnit 5 + MockK + Turbine (Flow assertions)

- Use `MainDispatcherRule` from `:core:testing` for coroutine tests.
- Use Given, When and Then comments where the corresponding phase exists. Omit `// Given` when the test has no setup data.
- Place `// When` immediately above the action under test, such as `viewModel.dispatchEvent(...)`, rather than above a surrounding Turbine `test` block.
- Use `// When & Then` when the action and verification naturally happen in the same statement or block.
- Don't repeat fake data in tests. In this case, create a package `tools` and put reusable fake-data `val`s there. Use `copy(...)` in the test when only some fields need to change; don't use factory functions for fake data.
- Don't create intermediate variables when asserting state or effects that only asserts one thing:

  ❌ *Don't do this:*
    ```kotlin 
        val state = awaitItem() as UiState.Content
        assertEquals(state.items, items)
    ```

  ✅ *Do this:*
    ```kotlin
        assertEquals(UiState.Content(items), awaitItem())
    ```

- Example testing with turbine:
  ```kotlin
    internal class MyViewModelTest { 
        @RegisterExtension
        val dispatcher = MainDispatcherRule(UnconfinedTestDispatcher())
    
        private lateinit var viewModel: MyViewModel
        
        @BeforeEach
        fun setUp() {
            viewModel = MyViewModel()
        }
    
        @Test
        fun `test event dispatching`() = runTest { 
            // Given
            val data = listOf(1, 2, 3)
      
            viewModel.uiState.test {
                // When
                viewModel.dispatchEvent(UiEvent.SomeEvent)
      
                // Then
                assertEquals(UiState.Content(data), awaitItem())
            } 
        }
    }
    ```

---

## Commits

Use conventional commits. The commit message should be structured as follows:
<type>[optional scope]: <description>
