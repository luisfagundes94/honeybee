---
name: android-ui
description: Best practices for building UI with Jetpack Compose, focusing on state hoisting, detailed performance optimizations, and theming. Use this when writing or refactoring Composable functions.
---

# Jetpack Compose Best Practices

## Instructions

Follow these guidelines to create performant, reusable, and testable Composables.

### 1. Screen Pattern

```kotlin
@Composable
internal fun FeatureScreen(
    onNavigateToMediaDetail: (Long) -> Unit,
    onNavigateToTrash: () -> Unit,
    viewModel: FeatureViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.dispatchEvent(FeatureUiEvent.LoadMedia)
    }

    CollectUiEffects(viewModel.uiEffect) { effect ->
        when (effect) {
            is FeatureUiEffect.NavigateToMediaDetail -> onNavigateToMediaDetail(effect.mediaId)
            is FeatureUiEffect.NavigateToTrash -> onNavigateToTrash()
        }
    }

    FeatureScreen(
        uiState = uiState,
        sendEvent = viewModel::dispatchEvent
    )
}

@Composable
private fun FeatureScreen(
    uiState: FeatureUiState,
    sendEvent: (FeatureUiEvent) -> Unit
) {
    when (uiState) {
        is FeatureUiState.Loading -> FeatureLoadingContent(
            modifier = Modifier
        )

        is FeatureUiState.Error -> FeatureErrorContent(
            sendEvent = sendEvent,
            modifier = Modifier
        )

        is FeatureUiState.Content -> FeatureContent(
            sendEvent = sendEvent,
            modifier = Modifier
        )
    }
}
```

### 2. State Hoisting (Unidirectional Data Flow)

Make Composables **stateless** whenever possible by moving state to the caller.

* **Pattern**: Function signature should usually look like:
  ```kotlin
  @Composable
  fun MyComponent(
      value: String,              // State flows down
      onValueChange: (String) -> Unit, // Events flow up
      modifier: Modifier = Modifier // Standard modifier parameter
  )
  ```
* **Benefit**: Decouples the UI from simple state storage, making it easier to preview and test.
* **ViewModel Integration**: The screen-level Composable retrieves state from the ViewModel
  (`viewModel.uiState.collectAsStateWithLifecycle()`) and passes it down.

### 3. Modifiers

* **Default Parameter**: Always provide a `modifier: Modifier = Modifier` as the first optional
  parameter.
* **Application**: Apply this `modifier` to the *root* layout element of your Composable.
* **Ordering matters**: `padding().clickable()` is different from `clickable().padding()`. Generally
  apply layout-affecting modifiers (like padding) *after* click listeners if you want the padding to
  be clickable.

### 4. Performance Optimization

* **`remember`**: Use `remember { ... }` to cache expensive calculations across recompositions.
* **`derivedStateOf`**: Use `derivedStateOf { ... }` when a state changes frequently (like scroll
  position) but the UI only needs to react to a threshold or summary (e.g., show "Jump to Top"
  button). This prevents unnecessary recompositions.
  ```kotlin
  val showButton by remember {
      derivedStateOf { listState.firstVisibleItemIndex > 0 }
  }
  ```
* **Lambda Stability**: Prefer method references (e.g., `viewModel::dispatchEvent`) or remembered
  lambdas to prevent unstable types from triggering recomposition of children.

### 5. Theming and Resources

* Use `MaterialTheme.colorScheme` and `MaterialTheme.typography` instead of hardcoded colors or text
  styles.
* Organize simple UI components into specific files (e.g., `DesignSystem.kt` or `Components.kt`) if
  they are shared across features.

### 6. Previews

* Create a private preview function for every public Composable.
* Use `@PreviewLightDark` for light and dark variants.
* Pass dummy data (static) to the stateless Composable for the preview.