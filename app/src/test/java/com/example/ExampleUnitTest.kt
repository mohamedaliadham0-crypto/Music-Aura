package com.example

import com.example.auramusic.data.model.AuraThemeStyle
import com.example.auramusic.data.model.PlaybackMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testPlaybackModeEnumValues() {
        val modes = PlaybackMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(PlaybackMode.NORMAL))
        assertTrue(modes.contains(PlaybackMode.SHUFFLE))
        assertTrue(modes.contains(PlaybackMode.REPEAT_ONE))
    }

    @Test
    fun testThemeStyles() {
        val themes = AuraThemeStyle.values()
        assertEquals(4, themes.size)
        assertEquals("Aurora Violet", AuraThemeStyle.AURORA_VIOLET.displayName)
        assertEquals("Deep Sapphire", AuraThemeStyle.DEEP_SAPPHIRE.displayName)
        assertEquals("Neon Sunset", AuraThemeStyle.NEON_SUNSET.displayName)
        assertEquals("Cyber Blush", AuraThemeStyle.CYBER_BLUSH.displayName)
    }

    @Test
    fun testDurationFormatterLogic() {
        fun formatMs(ms: Long): String {
            val totalSeconds = (ms / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }

        assertEquals("3:34", formatMs(214000L))
        assertEquals("0:00", formatMs(0L))
        assertEquals("1:05", formatMs(65000L))
        assertEquals("10:00", formatMs(600000L))
    }
}
