---
name: android-presentation
description: Best practices for implementing Android ViewModels using StateFlow for UI state, Events for user interactions, and SharedFlow for one-off events.
---

### ViewModel

```kotlin
class FeatureViewModel(
    private val useCase: FeatureUseCase
) : ViewModel<FeatureUiState, FeatureUiEvent, FeatureUiEffect>(
    initialState = FeatureUiState.Initial
) {
    fun dispatchEvent(event: FeatureUiEvent) {
        when (event) {
            is FeatureUiEvent.ButtonClicked -> loadData()
            is FeatureUiEvent.TextChanged -> updateText(event.text)
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            setState { FeatureUiState.Loading }
            useCase().fold(
                onSuccess = { setState { FeatureUiState.Success(it) } },
                onFailure = { sendEffect { FeatureUiAction.ShowError(it.message) } }
            )
        }
    }
}

// UiState with Sealed Interface or Sealed Class 
sealed interface FeatureUiState {
    data object Initial : FeatureUiState()
    data object Loading : FeatureUiState()
    data class Success(val data: FeatureData) : FeatureUiState()
    data class Error(val message: String) : FeatureUiState()
}

// UiEvent - user actions
sealed class FeatureUiEvent {
    data object ButtonClicked : FeatureUiEvent()
    data class TextChanged(val text: String) : FeatureUiEvent()
}

// UiEffect - side-effects (one-shot)
sealed class FeatureUiEffet {
    data class ShowError(val message: String?) : FeatureUiEffect()
    data class Navigate(val route: String) : FeatureUiEffect()
    data object GoBack : FeatureUiAction()
}
```

## Critical Rules

- ⛔ NO `init` block for data loading — use `OnStart` event via `LaunchedEffect`
- ⛔ NO anemic UseCases — ViewModel calls Repository directly if no business logic
- ✅ Base class: `core/presentation/arch/viewmodel/` (NOT AndroidX)
- ✅ `setState { }` for state changes
- ✅ `sendEffect { }` for one-shot events

## State Modeling

Use sealed interfaces or sealed classes to represent states. Keep the state immutable and update it
through `setState { }` or `setStateOf<T> { }`.

## ViewModel Hierarchy

Three base classes in `core/presentation/arch/viewmodel/`:

- `ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` - use when the screen needs MVI events and one-shot side effects like navigation (effects sent via `Channel`, collected as `uiEffect`).
- `StateViewModel<State : UiState, Event : UiEvent>` - use when MVI events are needed but no side effects are required; exposes only `uiState` and handles incoming `UiEvent`s.
- `EffectViewModel<Effect : UiEffect>` - use when no MVI events are needed but one-shot side effects are required; exposes only `uiEffect` and handles incoming `UiEffect`s.

Data flow:
1. **User Action / Events**: The Compose screen dispatches immutable events implementing `UiEvent` by calling `viewModel.dispatchEvent(event)`. All public business methods on the ViewModel are kept `private`, exposing only `dispatchEvent`.
2. **State Updates**: The ViewModel handles incoming events within `dispatchEvent(event)`. It updates the immutable `uiState` via `setState { }` or `setStateOf<T> { }` (to only update state when matching a specific subtype in a sealed hierarchy).
3. **Side Effects**: One-shot side-effects (e.g. navigation, toasts) are dispatched from the ViewModel using `sendEffect { }` and collected in Compose screens using `CollectUiEffects(viewModel.uiEffect)`.

## Navigation

Use **androidx.navigation3** (https://developer.android.com/guide/navigation/navigation-3).

- Routes are `@Serializable data object` or `@Serializable data class` implementing `NavKey`
- New feature routes are registered by adding `entry<RouteType> { }` blocks inside the feature's `EntryProviderScope<NavKey>.featureSection()` extension, then wiring the section call in `AppNavDisplay.kt`
- `TopLevelDestinations` enum maps top-level routes to icons/labels for the bottom bar
