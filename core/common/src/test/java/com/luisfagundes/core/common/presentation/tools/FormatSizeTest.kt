package com.luisfagundes.core.common.presentation.tools

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormatSizeTest {

    @Test
    fun `formatSize with zero bytes should return zero B`() {
        // Given
        val bytes = 0L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("0", "B"), result)
    }

    @Test
    fun `formatSize with negative bytes should return zero B`() {
        // Given
        val bytes = -100L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("0", "B"), result)
    }

    @Test
    fun `formatSize with small bytes should return bytes unit`() {
        // Given
        val bytes = 500L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("500", "B"), result)
    }

    @Test
    fun `formatSize with exact kilobyte should return KB unit`() {
        // Given
        val bytes = 1024L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("1", "KB"), result)
    }

    @Test
    fun `formatSize with decimal kilobyte should return formatted value and KB unit`() {
        // Given
        val bytes = 1536L // 1.5 KB

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("1.5", "KB"), result)
    }

    @Test
    fun `formatSize with exact megabyte should return MB unit`() {
        // Given
        val bytes = 1024L * 1024L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("1", "MB"), result)
    }

    @Test
    fun `formatSize with decimal megabyte should return formatted value and MB unit`() {
        // Given
        val bytes = (1024L * 1024L * 2.5).toLong() // 2.5 MB

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("2.5", "MB"), result)
    }

    @Test
    fun `formatSize with gigabyte should return GB unit`() {
        // Given
        val bytes = 1024L * 1024L * 1024L

        // When
        val result = formatSize(bytes)

        // Then
        assertEquals(Pair("1", "GB"), result)
    }
}
