package com.example.auramusic.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.example.MainActivity
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.data.local.PlayHistoryEntity
import com.example.auramusic.data.model.PlaybackMode
import com.example.auramusic.data.model.Song
import com.example.auramusic.widget.WidgetUpdateHelper
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@UnstableApi
class AuraPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    lateinit var player: ExoPlayer

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressTickerJob: Job? = null
    private val database by lazy { AuraDatabase.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        // Configure audio attributes for music playback with automatic audio becoming noisy handling
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true) // true = handle audio focus & becoming noisy
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Open app pending intent on notification click
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val sessionActivity = PendingIntent.getActivity(this, 0, openAppIntent, flags)

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivity)
            .setCallback(AuraSessionCallback())
            .build()

        setupPlayerListener()
    }

    private fun setupPlayerListener() {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                PlaybackStateManager.updatePlaying(applicationContext, isPlaying)
                if (isPlaying) {
                    startProgressTicker()
                } else {
                    stopProgressTicker()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    PlaybackStateManager.updatePosition(player.currentPosition, player.duration)
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                val currentQueue = PlaybackStateManager.queue.value
                val currentIndex = player.currentMediaItemIndex

                if (currentIndex in currentQueue.indices) {
                    val song = currentQueue[currentIndex]
                    PlaybackStateManager.updateCurrentSong(applicationContext, song)
                    PlaybackStateManager.updateQueue(currentQueue, currentIndex)

                    // Record play history in local Room database
                    serviceScope.launch(Dispatchers.IO) {
                        try {
                            database.musicDao().recordPlayed(PlayHistoryEntity(songId = song.id))
                        } catch (_: Exception) {}
                    }
                }
            }
        })
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressTickerJob = serviceScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    val pos = player.currentPosition
                    val dur = player.duration
                    PlaybackStateManager.updatePosition(pos, dur)
                }
                delay(500)
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    fun applyPlaybackMode(mode: PlaybackMode) {
        when (mode) {
            PlaybackMode.NORMAL -> {
                player.repeatMode = Player.REPEAT_MODE_OFF
                player.shuffleModeEnabled = false
            }
            PlaybackMode.SHUFFLE -> {
                player.repeatMode = Player.REPEAT_MODE_ALL
                player.shuffleModeEnabled = true
            }
            PlaybackMode.REPEAT_ONE -> {
                player.repeatMode = Player.REPEAT_MODE_ONE
                player.shuffleModeEnabled = false
            }
        }
        PlaybackStateManager.updateMode(applicationContext, mode)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            WidgetUpdateHelper.ACTION_PLAY_PAUSE -> {
                if (player.isPlaying) {
                    player.pause()
                } else {
                    if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                        player.prepare()
                    }
                    player.play()
                }
            }
            WidgetUpdateHelper.ACTION_PREV -> {
                if (player.currentPosition > 3000) {
                    player.seekTo(0)
                } else {
                    player.seekToPreviousMediaItem()
                }
            }
            WidgetUpdateHelper.ACTION_NEXT -> {
                player.seekToNextMediaItem()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Keep playing if user is actively listening when swiping away app
        if (!player.playWhenReady || player.mediaItemCount == 0 || player.playbackState == Player.STATE_ENDED) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        stopProgressTicker()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class AuraSessionCallback : MediaSession.Callback {
        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }
}
