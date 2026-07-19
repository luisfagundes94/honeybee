package com.luisfagundes.library.impl.presentation.tools

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FormatVideoDurationTest {

    @Test
    fun `duration under one hour should be formatted as minutes and seconds`() {
        // When & Then
        assertEquals("1:05", 65_000L.formatVideoDuration())
    }

    @Test
    fun `duration over one hour should include hours`() {
        // When & Then
        assertEquals("1:02:05", 3_725_000L.formatVideoDuration())
    }

    @Test
    fun `negative duration should be formatted as zero`() {
        // When & Then
        assertEquals("0:00", (-1L).formatVideoDuration())
    }
}
