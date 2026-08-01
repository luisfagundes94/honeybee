# Presentation

## Design System

Do not use hardcoded dp values, colors, strings, or `@PreviewLightDark` in Compose screens.

| Forbidden | Required |
|-----------|----------|
| `16.dp` | `MaterialTheme.spacing.default` |
| `Color.White` | `MaterialTheme.colorScheme...` |
| `"Text"` | `stringResource(R.string...)` |
| `@Preview` | `@PreviewLightDark` + `@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)` |

Remediation: Replace it with the corresponding token. See the Tokens section in `compose-structure.md`.

## State Modeling

Use sealed interfaces or sealed classes when a UI model has finite, mutually exclusive
variants such as loading, content, and error. Use a data class when the state is one
evolving immutable snapshot whose fields are valid together. Keep the snapshot
immutable and update it through `setState { }` or `setStateOf<T> { }`.

```kotlin
// Correct for finite, mutually exclusive variants
sealed class FeatureUiState : UiState {
    data object Loading : FeatureUiState()
    data class Success(val data: Data) : FeatureUiState()
    data class Error(val message: String) : FeatureUiState()
}

// Also correct for one evolving snapshot
data class FeatureUiState(
    val isLoading: Boolean = false,
    val data: Data? = null,
    val error: String? = null,
) : UiState
```

Do not use an open mutable class or combine nullable fields that represent mutually
exclusive variants. When those variants exist, model them with the sealed pattern above.

## ViewModel Hierarchy

Three base classes in `core/presentation/arch/viewmodel/`:

- `ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` - use when the screen needs MVI events and one-shot side effects like navigation (effects sent via `Channel`, collected as `uiEffect`).
- `StateViewModel<State : UiState, Event : UiEvent>` - use when MVI events are needed but no side effects are required; exposes only `uiState` and handles incoming `UiEvent`s.
- `EffectViewModel<Effect : UiEffect>` - use when no MVI events are needed but one-shot side effects are required; exposes only `uiEffect` and handles incoming `UiEffect`s.

Data flow:
1. **User Action / Events**: The Compose screen dispatches immutable events implementing `UiEvent` by calling `viewModel.dispatchEvent(event)`. All public business methods on the ViewModel are kept `private`, exposing only `dispatchEvent`.
2. **State Updates**: The ViewModel handles incoming events within `dispatchEvent(event)`. It updates the immutable `uiState` via `setState { }` or `setStateOf<T> { }` (to only update state when matching a specific subtype in a sealed hierarchy).
3. **Side Effects**: One-shot side-effects (e.g. navigation, toasts) are dispatched from the ViewModel using `sendEffect { }` and collected in Compose screens using `CollectUiEffects(viewModel.uiEffect)`.

**State updates**: Use `setState { }` for unconditional updates. Use `setStateOf<SpecificState> { }` to only mutate if the current state matches a specific subtype in a sealed hierarchy (e.g., only mutate if currently in `Success` state).

## Navigation

Uses **androidx.navigation3** (experimental Nav3 library, not the stable `androidx.navigation`/NavController).

- Routes are `@Serializable data object` or `@Serializable data class` implementing `NavKey`
- New feature routes are registered by adding `entry<RouteType> { }` blocks inside the feature's `EntryProviderScope<NavKey>.featureSection()` extension, then wiring the section call in `AppNavDisplay.kt`
- `TopLevelDestinations` enum maps top-level routes to icons/labels for the bottom bar
