package com.example.auramusic.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.auramusic.data.model.PlaybackMode
import com.example.auramusic.data.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AuraPlayerController {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    var mediaController: MediaController? = null
        private set

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    fun initialize(context: Context) {
        if (mediaController != null || controllerFuture != null) return

        val sessionToken = SessionToken(
            context,
            ComponentName(context, AuraPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                _isConnected.value = true
                syncInitialState(context)
            } catch (_: Exception) {
                _isConnected.value = false
            }
        }, MoreExecutors.directExecutor())
    }

    private fun syncInitialState(context: Context) {
        val controller = mediaController ?: return
        PlaybackStateManager.updatePlaying(context, controller.isPlaying)
        if (controller.isPlaying) {
            PlaybackStateManager.updatePosition(controller.currentPosition, controller.duration)
        }
    }

    fun playQueue(context: Context, songs: List<Song>, startIndex: Int = 0) {
        val controller = mediaController ?: return
        if (songs.isEmpty()) return

        val mediaItems = songs.map { song ->
            val metadata = MediaMetadata.Builder()
                .setTitle(song.title)
                .setArtist(song.artist)
                .setAlbumTitle(song.album)
                .setArtworkUri(song.albumArtUri)
                .build()

            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(song.contentUri)
                .setMediaMetadata(metadata)
                .build()
        }

        val clampedIndex = startIndex.coerceIn(0, songs.size - 1)
        PlaybackStateManager.updateQueue(songs, clampedIndex)
        PlaybackStateManager.updateCurrentSong(context, songs[clampedIndex])

        controller.setMediaItems(mediaItems, clampedIndex, 0L)
        controller.prepare()
        controller.play()
    }

    fun playSong(context: Context, song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.let { if (it >= 0) it else 0 }
        val targetQueue = if (queue.isNotEmpty()) queue else listOf(song)
        playQueue(context, targetQueue, index)
    }

    fun togglePlayPause(context: Context) {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            if (controller.playbackState == Player.STATE_IDLE || controller.playbackState == Player.STATE_ENDED) {
                controller.prepare()
            }
            controller.play()
        }
    }

    fun skipNext() {
        val controller = mediaController ?: return
        if (controller.hasNextMediaItem()) {
            controller.seekToNextMediaItem()
        } else {
            controller.seekToDefaultPosition(0)
        }
    }

    fun skipPrevious() {
        val controller = mediaController ?: return
        if (controller.currentPosition > 3000) {
            controller.seekTo(0)
        } else if (controller.hasPreviousMediaItem()) {
            controller.seekToPreviousMediaItem()
        } else {
            controller.seekTo(0)
        }
    }

    fun seekTo(positionMs: Long) {
        val controller = mediaController ?: return
        controller.seekTo(positionMs)
        PlaybackStateManager.updatePosition(positionMs, controller.duration)
    }

    fun cyclePlaybackMode(context: Context) {
        val nextMode = when (PlaybackStateManager.playbackMode.value) {
            PlaybackMode.NORMAL -> PlaybackMode.SHUFFLE
            PlaybackMode.SHUFFLE -> PlaybackMode.REPEAT_ONE
            PlaybackMode.REPEAT_ONE -> PlaybackMode.NORMAL
        }
        setPlaybackMode(context, nextMode)
    }

    fun setPlaybackMode(context: Context, mode: PlaybackMode) {
        val controller = mediaController ?: return
        when (mode) {
            PlaybackMode.NORMAL -> {
                controller.repeatMode = Player.REPEAT_MODE_OFF
                controller.shuffleModeEnabled = false
            }
            PlaybackMode.SHUFFLE -> {
                controller.repeatMode = Player.REPEAT_MODE_ALL
                controller.shuffleModeEnabled = true
            }
            PlaybackMode.REPEAT_ONE -> {
                controller.repeatMode = Player.REPEAT_MODE_ONE
                controller.shuffleModeEnabled = false
            }
        }
        PlaybackStateManager.updateMode(context, mode)
    }

    fun release() {
        controllerFuture?.let { MediaController.releaseFuture(it) }
        mediaController = null
        controllerFuture = null
        _isConnected.value = false
    }
}
