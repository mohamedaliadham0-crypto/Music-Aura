package com.example.auramusic.playback

import android.content.Context
import com.example.auramusic.data.model.PlaybackMode
import com.example.auramusic.data.model.Song
import com.example.auramusic.widget.WidgetUpdateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object PlaybackStateManager {

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackMode = MutableStateFlow(PlaybackMode.NORMAL)
    val playbackMode: StateFlow<PlaybackMode> = _playbackMode.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _currentIndex = MutableStateFlow(0)
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    fun updateCurrentSong(context: Context?, song: Song?) {
        _currentSong.value = song
        _durationMs.value = song?.durationMs ?: 0L
        context?.let { WidgetUpdateHelper.updateAllWidgets(it) }
    }

    fun updatePlaying(context: Context?, playing: Boolean) {
        _isPlaying.value = playing
        context?.let { WidgetUpdateHelper.updateAllWidgets(it) }
    }

    fun updatePosition(positionMs: Long, durationMs: Long) {
        _playbackPositionMs.value = positionMs
        if (durationMs > 0) {
            _durationMs.value = durationMs
        }
    }

    fun updateMode(context: Context?, mode: PlaybackMode) {
        _playbackMode.value = mode
        context?.let { WidgetUpdateHelper.updateAllWidgets(it) }
    }

    fun updateQueue(queue: List<Song>, index: Int) {
        _queue.value = queue
        _currentIndex.value = index
    }
}
