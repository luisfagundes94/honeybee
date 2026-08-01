# Code Quality

## Architecture and Design Principles

Use common design patterns when applicable.
Follow best development practices such as SOLID and KISS.

## Coroutines for New Code

New code MUST use Coroutines + Flow.

| Context | Technology |
|---------|------------|
| New code | Coroutines + Flow ✅ |

Remediation: Use suspend functions + Flow.

## Anti-Patterns That Agents Amplify

Patterns that must be corrected IMMEDIATELY because agents replicate them:

| Anti-Pattern | Why It Amplifies | Fix |
|-------------|------------------|-----|
| Direct database query without a Repository | The agent copies the direct access | Always use a Repository |
| Magic numbers or strings | The agent does not know they are temporary | Extract them into a constant or configuration |
| Generic `catch(e: Exception)` | The agent will never become more specific | Catch specific exception types |
| Unhandled `runCatching` | The agent replicates it without `onFailure` or `getOrThrow`, causing silent failures | Always handle it with `onSuccess`/`onFailure` or `getOrElse`/`getOrThrow` |
| Wildcard imports such as `import com.picpay.*` | The agent copies the wildcard | Use explicit imports |
| Functions with 10+ parameters | The agent replicates the signature | Extract the parameters into a data class |
| `companion object` used only for constants | The agent generates unnecessary overhead | Remove the `companion object` and declare top-level constants |

Remediation: Refactor the pattern before asking an agent to work on the module.
