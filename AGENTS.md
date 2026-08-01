# AGENTS.md

This file is the quick-reference guide for agents and contributors working in the Honeybee repository. Detailed guidance is grouped by topic under [`docs/`](docs/).

## Quick reference

- Use MVI + Clean Architecture + Clean Code.
- Keep layer dependencies one-way: presentation → domain, data → domain, and domain → nothing. Presentation must never depend on data implementations.
- Keep the domain layer pure Kotlin; it must not import Android framework types.
- ViewModels depend on domain repository interfaces, never data-layer implementations.
- Create a UseCase only when it contains meaningful business rules or orchestration.
- Handle both success and failure for every `Result<T>` operation.
- New code uses Coroutines + Flow.
- `UiState`, `UiEvent`, and `UiEffect` use sealed interfaces or sealed classes.
- Use design-system tokens instead of hardcoded Compose dimensions, colors, strings, or previews.
- Keep domain and data entities as `class`; presentation UI models, states, and args may use `data class`.
- Use extension functions for data-to-domain mapping; do not create mapper classes.
- DataSources must add meaningful behavior; pass-through wrappers are not allowed.
- Default new types to `internal`; only navigation routes are public unless an explicit `api`/`impl` split is needed.
- Use Hilt for dependency injection and the project dispatcher qualifiers.

## Detailed guidance

| Topic | Documentation |
| --- | --- |
| Architecture, module boundaries, domain purity, and dependency injection | [`docs/architecture.md`](docs/architecture.md) |
| UseCases, repositories, entities, DataSources, mapping, and `Result<T>` | [`docs/data-and-domain.md`](docs/data-and-domain.md) |
| Compose design system, MVI state, ViewModels, and navigation | [`docs/presentation.md`](docs/presentation.md) |
| Coroutines, design principles, and prohibited anti-patterns | [`docs/code-quality.md`](docs/code-quality.md) |
| Unit and coroutine testing conventions | [`docs/testing.md`](docs/testing.md) |
| Commit message conventions | [`docs/contributing.md`](docs/contributing.md) |

## Before changing code

Read the topic document that applies to the change before editing code. When a change crosses layers or concerns, read every relevant document. The topic documents contain the complete rules, examples, and remediation guidance that were previously kept in this file.
