---
name: android-domain-layer
description: Rules and guidelines for implementing the Android Domain Layer, ensuring pure Kotlin without Android framework dependencies, and preventing pass-through or anemic UseCases.
---

## Domain Layer Is Pure Kotlin

The domain layer CANNOT have Android framework dependencies. No Context, Activity, Fragment,
LiveData, StateFlow or android.* imports.

Verification: Compilation (module without Android plugin)
Remediation: Move Android deps to data or presentation layer

## UseCases Are Not Pass-Through

UseCases MUST contain substantial business rules. If it merely delegates to Repository without
adding logic → do not create a UseCase. UseCases MUST be concrete classes, NOT interfaces with a
single implementation.

```kotlin
// ✅ CORRECT — UseCase with business logic
class AddAlbumToLibraryUseCase(
    private val albumRepository: AlbumRepository,
    private val userLibraryRepository: UserLibraryRepository,
    private val storageValidator: StorageValidator
) {
    suspend operator fun invoke(albumId: String): Result<Unit> {
        // Business rule validation
        val album = albumRepository.getAlbumDetails(albumId)
            .getOrElse { return Result.failure(it) }

        if (userLibraryRepository.containsAlbum(albumId)) {
            return Result.failure(AlbumAlreadyInLibraryException())
        }

        // Storage / limit validation
        if (!storageValidator.hasEnoughSpaceForOffline(album.totalSizeBytes)) {
            return Result.failure(InsufficientStorageException())
        }

        // Composite operation orchestration
        userLibraryRepository.addAlbum(album)
            .getOrElse { return Result.failure(it) }

        return albumRepository.markAsSaved(albumId)
    }
}

// ✅ CORRECT — ViewModel accesses Repository directly (simple operation without business logic)
internal class ItemListViewModel(
    private val repository: ItemRepository // interface defined in domain layer
) : ViewModel<ItemUiState, ItemUiEvent, ItemUiEffect>(
    initialState = ItemUiState.Initial
) {
    fun loadItems() {
        viewModelScope.launch {
            setState { ItemUiState.Loading }
            repository.getItems()
                .onSuccess { setState { ItemUiState.Success(it) } }
                .onFailure { setState { ItemUiState.Error(it.message) } }
        }
    }
}

// ❌ WRONG — Anemic UseCase (just delegates without logic)
class GetItemsUseCase(private val repository: ItemRepository) {
    suspend operator fun invoke(): Result<List<Item>> = repository.getItems()
}

// ❌ WRONG — ViewModel receives concrete implementation from data layer
internal class ItemListViewModel(
    private val repository: ItemRepositoryImpl // PROHIBITED: concrete implementation
) : ViewModel() {  }

// ❌ WRONG — Interface + Impl with a single implementation
interface AddAlbumToLibraryUseCase {
    suspend operator fun invoke(albumId: String): Result<Unit>
}
class AddAlbumToLibraryUseCaseImpl() : AddAlbumToLibraryUseCase {}
```

Checklist (at least 1 = YES to justify UseCase):

- Orchestrates multiple repositories/datasources?
- Contains business rule validations?
- Implements complex transformation logic?
- Manages composite operations?
- Applies business policies (limits, permissions)?

If NONE of the above apply → ViewModel accesses Repository directly via domain interface. This does
NOT violate Clean Architecture because the ViewModel depends on the Repository **interface**
(defined in the domain layer), not the implementation (data layer).

**Rule:** ViewModel MUST receive the Repository interface (e.g., `ItemRepository`), NEVER the
concrete implementation (e.g., `ItemRepositoryImpl`). Injecting the implementation is the
responsibility of the DI module (ScopeModule).

Verification: Review Remediation: Remove anemic UseCase and make ViewModel call Repository Interface
directly
