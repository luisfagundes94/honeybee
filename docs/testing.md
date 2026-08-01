# Testing

JUnit 5 + MockK + Turbine (Flow assertions)

- Use `MainDispatcherRule` from `:core:testing` for coroutine tests.
- Use Given, When and Then comments where the corresponding phase exists. Omit `// Given` when the test has no setup data.
- Place `// When` immediately above the action under test, such as `viewModel.dispatchEvent(...)`, rather than above a surrounding Turbine `test` block.
- Use `// When & Then` when the action and verification naturally happen in the same statement or block.
- Don't repeat fake data in tests. In this case, create a package `tools` and put reusable fake-data `val`s there.
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

        @BeforeEach
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
