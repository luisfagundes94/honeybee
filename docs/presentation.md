# Presentation

## Design System

Do not use hardcoded dp values, colors, strings, or `@PreviewLightDark` in Compose screens.

| Forbidden | Required                                     |
|-----------|----------------------------------------------|
| `16.dp` | `MaterialTheme.spacing.default`                |
| `Color.White` | `MaterialTheme.colorScheme...`          |
| `"Text"` | `stringResource(R.string...)`                |
| `@Preview` | `@PreviewLightDark` + `@PreviewWrapper(wrapper = HoneybeeThemeWrapper::class)` |

Remediation: Replace it with the corresponding token. See the Tokens section in `compose-structure.md`.

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

## ViewModel Hierarchy

Three base classes in `core/presentation/arch/viewmodel/`:

- `ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` — use when the screen needs MVI events and one-shot side effects like navigation (effects sent via `Channel`, collected as `uiEffect`).
- `StateViewModel<State : UiState, Event : UiEvent>` — use when MVI events are needed but no side effects are required; exposes only `uiState` and handles incoming `UiEvent`s.
- `EffectViewModel<Effect : UiEffect>` — use when no MVI events are needed but one-shot side effects are required; exposes only `uiEffect` and handles incoming `UiEffect`s.`

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
