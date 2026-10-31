# AGENTS.md

This file is the quick-reference guide for agents and contributors working in the Honeybee repository. Detailed guidance is grouped by topic under [`docs/`](docs/).
Keep work scoped: repository-wide searches, tests and builds are expensive.

## Architecture and Engineering Rules

- Use Kotlin.
- Preserve Clean Architecture boundaries: Presentation may depend on Domain; Data implements Domain
  contracts; Domain must not depend on Android or Data. Repository interfaces belong to Domain and
  implementations do Data.
- Use MVI for new compose flows.
- New screens use compose and MaterialTheme; do not add XML for new screens. Use MaterialTheme
  components and tokens.
- Use Coroutines and Flow. Handle failures explicitly in coroutine launches and flow collections.
- Never call `runBlocking` in production code paths and never expose synchronous facades over
  `suspend` functions. Convert the contract to `suspend`/`flow` instead.
- Use Hilt for all dependency injection. Use @Module and @InstallIn (SingletonComponent::class) for
  app-wide singletons (e.g., Network, Database). Use @Binds in an abstract class to bind interface
  implementations (cleaner than @Provides).
- Do not create mapper classes for layer conversion in new code. Convert DTOs/entities with
  extensions functions (`.toDomain()`), private to the repository or colocated with the DTO
- Create a UseCase only for business logic, validation, transformation or orchestration. A ViewModel
  may call a domain repository interface directly for a simple operation.
- In a `api`/`impl` split, `impl` may depend on `api`; `api` must never depend on `impl`.
- Represent the relevant loading, content, empty and error UI states. Keep state immutable and use
  established event/effect pattern for one-off actions.
- Add or update tests for changed behavior, including relevant failure paths and state transitions.

## Working in this repo

- Inspect 3-5 representative neighboring files before introducing a structure or naming pattern.
- Make the smallest coherent diff; do not format, rename or refactor unrelated files.
- Do not run repository-wide `./gradlew test` or `./gradlew check` by default.
- Check downstream impact before changing dependencies, public contracts, navigation routes or
  shared design-system APIs.

## Build and Validation

Adapt task names to the module type and build variant.

```bash
# Android unit test class
./gradlew :<module>:testDebugUnitTest --tests "<fully.qualified.TestClass>" --no-daemon -q

# JVM unit test class
./gradlew :<module>:test --tests "<fully.qualified.TestClass>" --no-daemon -q

# Module verification after focused tests
./gradlew :<module>:check

# App integration builds, only when necessary
./gradle :app:assembleDebug --no-daemon -q
```

## Commits

Use conventional commits. The commit message should be structured as follows:

```text
<type>[optional scope]: <description>
```

## Detailed guidance

| Topic                                                                    | Documentation                                        |
|--------------------------------------------------------------------------|------------------------------------------------------|
| Architecture, module boundaries, domain purity, and dependency injection | [`docs/architecture.md`](docs/architecture.md)       |
| UseCases, repositories, entities, DataSources, mapping, and `Result<T>`  | [`docs/data-and-domain.md`](docs/data-and-domain.md) |
| Compose design system, MVI state, ViewModels, and navigation             | [`docs/presentation.md`](docs/presentation.md)       |
| Coroutines, design principles, and prohibited anti-patterns              | [`docs/code-quality.md`](docs/code-quality.md)       |
| Unit and coroutine testing conventions                                   | [`docs/testing.md`](docs/testing.md)                 |

## Definition of Done

- The requested behavior is implemented without unrelated changes
- Relevant focused tests pass
- Architecture, DI, UI, resources, error handling and local instructions are respected
- The diff has no accidental generated files, credential or broad formatting churn.
- The handoff lists validations run and any validation not run
