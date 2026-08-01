# Architecture

## Architecture

MVI + Clean Architecture + Clean Code

## Layer Boundaries Are Absolute

```
presentation → domain   ✅ allowed
data → domain           ✅ allowed
domain → (nothing)      ✅ required (pure Kotlin)
presentation → data     ❌ FORBIDDEN
```

Remediation: Create an interface in the domain layer and implement it in the data layer.

## The Domain Layer Is Pure Kotlin

The domain layer MUST NOT depend on the Android framework.
Do not use `Context`, `Activity`, `Fragment`, `LiveData`, or `android.*` imports.

Remediation: Move Android dependencies to the data or presentation layer.

## Feature Module Structure

Each feature module must have its own `data/`, `domain/`, `presentation/`, and `di/` sub-packages.

**Visibility convention**: All domain models, use cases, repositories, ViewModels, and DI modules are `internal`. Only navigation routes are public. Default to `internal` for new types.

When a feature needs to expose functionality to another feature, split the exposed surface into `api/` and `impl/`. Keep only the minimum contracts and types required by consumers in `api/`; keep implementations and internal details in `impl/`. Other features must depend on the feature's `api`, never its `impl`.

## Dependency Injection

Hilt throughout. Key qualifier annotations in `:core:common`:
- `@IoDispatcher`, `@DefaultDispatcher`, `@MainDispatcher`

All Hilt modules use `@InstallIn(SingletonComponent::class)`.

You can find the Hilt custom annotations at `core/common/src/main/java/com/luisfagundes/core/common/di`
