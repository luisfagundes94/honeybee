package com.luisfagundes.baselineprofile

import androidx.benchmark.macro.BaselineProfileMode
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val GRID_LOAD_TIMEOUT_MILLIS = 5_000L
private const val GESTURE_MARGIN_DIVISOR = 10
private const val SCROLL_COUNT = 3
private const val SCROLL_PERCENT = 0.5f

@RunWith(AndroidJUnit4::class)
@LargeTest
class LibraryScrollBenchmarks {

    @get:Rule
    val rule = MacrobenchmarkRule()

    @Test
    fun scrollCompilationNone() = benchmark(CompilationMode.None())

    @Test
    fun scrollCompilationBaselineProfile() = benchmark(
        CompilationMode.Partial(BaselineProfileMode.Require)
    )

    private fun benchmark(compilationMode: CompilationMode) {
        val packageName = InstrumentationRegistry.getArguments().getString("targetAppId")
            ?: error("targetAppId not passed as instrumentation runner arg")

        rule.measureRepeated(
            packageName = packageName,
            metrics = listOf(FrameTimingMetric()),
            compilationMode = compilationMode,
            iterations = 5,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.scrollable(true)), GRID_LOAD_TIMEOUT_MILLIS)
            },
            measureBlock = {
                val grid = device.findObject(By.scrollable(true)) ?: error("Grid not found")
                grid.setGestureMargin(device.displayWidth / GESTURE_MARGIN_DIVISOR)
                repeat(SCROLL_COUNT) { grid.swipe(Direction.UP, SCROLL_PERCENT) }
                repeat(SCROLL_COUNT) { grid.swipe(Direction.DOWN, SCROLL_PERCENT) }
                device.waitForIdle()
            }
        )
    }
}
