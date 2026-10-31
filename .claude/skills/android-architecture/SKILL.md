---
name: android-architecture
description: Expert guidance on setting up and maintaining a modern Android application architecture using Clean Architecture and Hilt. Use this when asked about project structure, module setup, or dependency injection.
---

# Android Modern Architecture & Modularization

## Instructions

When designing or refactoring an Android application, adhere to the **Clean Architecture**
principles and MVI pattern for the presentation layer.

### 1. High-Level Layers

Structure the application into three primary layers. Dependencies must strictly flow **inwards** (or
downwards) to the core logic.

* **UI Layer (Presentation)**:
    * **Responsibility**: Displaying data and handling user interactions.
    * **Components**: Activities, Fragments, Composables, ViewModels.
    * **Dependencies**: Depends on the Domain Layer (or Data Layer if simple). **Never** depends on
      the Data Layer implementation details directly.
* **Domain Layer (Business Logic):
    * **Responsibility**: Encapsulating complex business rules and reuse.
    * **Components**: Use Cases (e.g., `GetLatestNewsUseCase`), Domain Models (pure Kotlin data
      classes).
    * **Pure Kotlin**: Must NOT contain any Android framework dependencies (no `android.*` imports).
    * **Dependencies**: Depends on Repository Interfaces.
* **Data Layer**:
    * **Responsibility**: Managing application data (fetching, caching, saving).
    * **Components**: Repositories (implementations), Data Sources (Retrofit APIs, Room DAOs).
    * **Dependencies**: Depends only on external sources and libraries.

### 2. Dependency Injection with Hilt

Use **Hilt** for all dependency injection.

* **@HiltAndroidApp**: Annotate the `Application` class.
* **@AndroidEntryPoint**: Annotate Activities and Fragments.
* **@HiltViewModel**: Annotate ViewModels; use standard `constructor` injection.
* **Modules**:
    * Use `@Module` and `@InstallIn(SingletonComponent::class)` for app-wide singletons (e.g.,
      Network, Database).
    * Use `@Binds` in an abstract class to bind interface implementations (cleaner than
      `@Provides`).

### 3. Modularization Strategy

For production apps, use a multi-module strategy to improve build times and separation of concerns.

* **:app**: The main entry point, connects features.
* **:core:common**: Shared utilities.
* **:core:testing**: Shared testing utilities.
* **:core:designsystem**: Shared Composables, Theme, Resources.
* **:feature:[name]**: Standalone feature modules containing their own layers.

#### 4. Feature Module Structure

```
com.luisfagundes.{feature}/
├── di/             # Dependency Injection
├── data/           # Data layer
├── domain/         # Domain layer
├── presentation/   # Presentation layer
```

Each feature module must have its own `data/`, `domain/`, `presentation/`, and `di/` sub-packages.

**Visibility convention**: All domain models, use cases, repositories, ViewModels, and DI modules
are `internal`. Only navigation routes are public. Default to `internal` for new types.

When a feature needs to expose functionality to another feature, split the exposed surface into
`api/` and `impl/`. Keep only the minimum contracts and types required by consumers in `api/`; keep
implementations and internal details in `impl/`. Other features must depend on the feature's `api`,
never its `impl`.

### 5. Checklist for implementation

- [ ] Ensure `Domain` layer has no Android dependencies.
- [ ] Repositories should default to main-safe suspend functions (use `Dispatchers.IO` internally if
  needed).
- [ ] ViewModels should interact with the UI layer via `StateFlow`.