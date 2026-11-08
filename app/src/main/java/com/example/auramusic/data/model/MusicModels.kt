package com.example.auramusic.data.model

import android.net.Uri

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: Uri,
    val albumArtUri: Uri? = null,
    val dataPath: String = "",
    val sizeBytes: Long = 0,
    val dateAdded: Long = 0
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    val artworkUri: Uri? = null
)

data class Artist(
    val name: String,
    val songCount: Int,
    val albumCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int,
    val createdAt: Long,
    val coverArtUri: Uri? = null
)

enum class PlaybackMode {
    NORMAL,     // Sequential playback (Song 1 -> 2 -> 3)
    SHUFFLE,    // Randomized order without duplicate repeats
    REPEAT_ONE  // Loops the currently playing song
}

enum class AuraThemeStyle(val displayName: String) {
    AURORA_VIOLET("Aurora Violet"),
    DEEP_SAPPHIRE("Deep Sapphire"),
    NEON_SUNSET("Neon Sunset"),
    CYBER_BLUSH("Cyber Blush")
}

data class SongInfo(
    val title: String,
    val artist: String,
    val album: String,
    val duration: String,
    val path: String,
    val size: String
)
