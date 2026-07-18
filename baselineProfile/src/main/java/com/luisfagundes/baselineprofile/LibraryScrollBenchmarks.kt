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
            iterations = 10,
            setupBlock = {
                pressHome()
                startActivityAndWait()
                device.wait(Until.hasObject(By.scrollable(true)), GridLoadTimeoutMillis)
            },
            measureBlock = {
                val grid = device.findObject(By.scrollable(true))
                    ?: error("Library grid was not found")
                grid.setGestureMargin(device.displayWidth / GestureMarginDivisor)
                repeat(ScrollCount) {
                    grid.scroll(Direction.DOWN, ScrollPercent)
                }
                repeat(ScrollCount) {
                    grid.scroll(Direction.UP, ScrollPercent)
                }
                device.waitForIdle()
            }
        )
    }

    private companion object {
        const val GridLoadTimeoutMillis = 5_000L
        const val GestureMarginDivisor = 10
        const val ScrollCount = 3
        const val ScrollPercent = 1f
    }
}
