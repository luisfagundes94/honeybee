# AGENTS.md

This file provides guidance to Google Gemini Agent when working with code in this repository.

## Build & Development Commands

Use Android CLI Skills or gradlew commands if necessary

## Commits

Use conventional commits. The commit message should be structured as follows:
<type>[optional scope]: <description>

**DO NOT COMMIT CHANGES** before I approve it or explicitly tell you to commit.

## Architecture Overview

**Pattern**: Clean Architecture + MVI (Model-View-Intent)

- Use Design Patterns when applicable
- Follow SOLID principles
- Follow clean architecture principles
- Follow the KISS principle (Keep It Simple, Stupid)

## Coding Conventions
- Avoid repeating code
- Avoid magic numbers
- Avoid too long methods, break them into smaller methods

### Feature Module Structure

Each feature module must have its own `data/`, `domain/`, `presentation/`, and `di/` sub-packages.

**Visibility convention**: All domain models, use cases, repositories, ViewModels, and DI modules are `internal`. Only navigation routes are public. Default to `internal` for new types.

### ViewModel Hierarchy

Three base classes in `core/presentation/arch/viewmodel/`:

- `ViewModel<State : UiState, Event : UiEvent, Effect : UiEffect>` — use when the screen needs MVI events and one-shot side effects like navigation (effects sent via `Channel`, collected as `uiEffect`).
- `StateViewModel<State : UiState, Event : UiEvent>` — use when MVI events are needed but no side effects are required; exposes only `uiState` and handles incoming `UiEvent`s.
- `EffectViewModel<Effect : UiEffect>` — use when no MVI events are needed but one-shot side effects are required; exposes only `uiEffect` and handles incoming `UiEffect`s.`

Data flow:
1. **User Action / Events**: The Compose screen dispatches immutable events implementing `UiEvent` by calling `viewModel.dispatchEvent(event)`. All public business methods on the ViewModel are kept `private`, exposing only `dispatchEvent`.
2. **State Updates**: The ViewModel handles incoming events within `dispatchEvent(event)`. It updates the immutable `uiState` via `setState { }` or `setStateOf<T> { }` (to only update state when matching a specific subtype in a sealed hierarchy).
3. **Side Effects**: One-shot side-effects (e.g. navigation, toasts) are dispatched from the ViewModel using `sendEffect { }` and collected in Compose screens using `CollectUiEffects(viewModel.uiEffect)`.

**State updates**: Use `setState { }` for unconditional updates. Use `setStateOf<SpecificState> { }` to only update when the current state matches a specific subtype in a sealed hierarchy (e.g., only mutate if currently in `Success` state).

### Navigation

Uses **androidx.navigation3** (experimental Nav3 library, not the stable `androidx.navigation`/NavController).

- Routes are `@Serializable data object` or `@Serializable data class` implementing `NavKey`
- New feature routes are registered by adding `entry<RouteType> { }` blocks inside the feature's `EntryProviderScope<NavKey>.featureSection()` extension, then wiring the section call in `AppNavDisplay.kt`
- `TopLevelDestinations` enum maps top-level routes to icons/labels for the bottom bar

### Dependency Injection

Hilt throughout. Key qualifier annotations in `:core:common`:
- `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher`

All Hilt modules use `@InstallIn(SingletonComponent::class)`.

You can find the Hilt custom annotations at core/common/src/main/java/com/luisfagundes/core/common/di

### Data Layer 

- **Room** with TypeConverters if necessary
- Itinerary items (Flight, Accommodation, Restaurant, Activity) use a polymorphic DAO factory pattern (`ItineraryDaoFactory`)
- **Retrofit** for Unsplash image API; auth injected via OkHttp interceptor
- Unsplash API key: `secrets-gradle-plugin` reads `UNSPLASH_ACCESS_KEY` from `secrets.properties` (not committed) and exposes it via `BuildConfig.UNSPLASH_ACCESS_KEY`
- Repositories return `Result<T>`; ViewModels fold on success/failure

### Presentation Layer

- Extension or helper functions related to UI should be put in a presentation/tools folder. If it's used in more than one module, put it in :core:common:presentation:tools
- Don't use hardcoded strings for texts and content descriptions. Use strings.xml
- Don't use hardcoded colors. Use MaterialTheme.colorScheme. 
- Don't use hardcoded dimensions. Use MaterialTheme.spacing. If MaterialTheme.spacing.* does not have a specific value, you can use `dp` values.

### Testing

JUnit 5 + MockK + Turbine (Flow assertions)

- Use `MainDispatcherRule` from `:core:testing` for coroutine tests.
- Use Given, When and Then comments
- Don't repeat fake data in tests. In this case, create a package tools and put the fake data there for reusability.
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
        
        @Before
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

### Design System Conventions

- Use `MaterialTheme.spacing.*` (`verySmall=4dp`, `small=8dp`, `default=16dp`, `large=32dp`, `veryLarge=42dp`, `extraLarge=52dp`) instead of hardcoded `dp` values
- Use Material3 always
- Use `MaterialTheme.colorScheme.*` instead of hardcoded colors
- Use `MaterialTheme.typography.*` instead of hardcoded font sizes
- Use `MaterialTheme.shapes.*` instead of hardcoded corner radius
- When importing a resource from design system module in another module, use the import com.luisfagundes.core.designsystem R as DesignSystemResources
