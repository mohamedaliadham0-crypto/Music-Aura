package com.example.auramusic.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.auramusic.data.model.PlaybackMode
import com.example.auramusic.playback.PlaybackStateManager
import java.io.InputStream

object WidgetUpdateHelper {

    const val ACTION_PLAY_PAUSE = "com.example.auramusic.ACTION_PLAY_PAUSE"
    const val ACTION_PREV = "com.example.auramusic.ACTION_PREV"
    const val ACTION_NEXT = "com.example.auramusic.ACTION_NEXT"

    private fun getPendingActionIntent(context: Context, action: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            this.action = action
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    private fun getOpenAppPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        return PendingIntent.getActivity(context, 100, intent, flags)
    }

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context) ?: return

        val miniIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AuraMiniWidget::class.java))
        if (miniIds.isNotEmpty()) {
            updateMiniWidgets(context, appWidgetManager, miniIds)
        }

        val standardIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AuraStandardWidget::class.java))
        if (standardIds.isNotEmpty()) {
            updateStandardWidgets(context, appWidgetManager, standardIds)
        }

        val largeIds = appWidgetManager.getAppWidgetIds(ComponentName(context, AuraLargeWidget::class.java))
        if (largeIds.isNotEmpty()) {
            updateLargeWidgets(context, appWidgetManager, largeIds)
        }
    }

    fun updateMiniWidgets(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val song = PlaybackStateManager.currentSong.value
        val isPlaying = PlaybackStateManager.isPlaying.value

        for (id in widgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_mini)

            views.setTextViewText(R.id.widget_title, song?.title ?: "Aura Music")
            views.setTextViewText(R.id.widget_artist, song?.artist ?: "Tap to play")

            val playIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            // Setup button intents
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, getPendingActionIntent(context, ACTION_PLAY_PAUSE, 1))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingActionIntent(context, ACTION_NEXT, 2))
            views.setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))

            // Artwork
            loadArtworkToWidget(context, views, song?.albumArtUri, R.id.widget_artwork)

            manager.updateAppWidget(id, views)
        }
    }

    fun updateStandardWidgets(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val song = PlaybackStateManager.currentSong.value
        val isPlaying = PlaybackStateManager.isPlaying.value
        val positionMs = PlaybackStateManager.playbackPositionMs.value
        val durationMs = PlaybackStateManager.durationMs.value

        for (id in widgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_standard)

            views.setTextViewText(R.id.widget_title, song?.title ?: "Aura Music")
            views.setTextViewText(R.id.widget_artist, song?.artist ?: "No track playing")

            val playIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            val progress = if (durationMs > 0) ((positionMs * 1000) / durationMs).toInt().coerceIn(0, 1000) else 0
            views.setProgressBar(R.id.widget_progress, 1000, progress, false)

            views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingActionIntent(context, ACTION_PREV, 3))
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, getPendingActionIntent(context, ACTION_PLAY_PAUSE, 4))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingActionIntent(context, ACTION_NEXT, 5))
            views.setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))

            loadArtworkToWidget(context, views, song?.albumArtUri, R.id.widget_artwork)

            manager.updateAppWidget(id, views)
        }
    }

    fun updateLargeWidgets(context: Context, manager: AppWidgetManager, widgetIds: IntArray) {
        val song = PlaybackStateManager.currentSong.value
        val isPlaying = PlaybackStateManager.isPlaying.value
        val positionMs = PlaybackStateManager.playbackPositionMs.value
        val durationMs = PlaybackStateManager.durationMs.value
        val mode = PlaybackStateManager.playbackMode.value

        for (id in widgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_large)

            views.setTextViewText(R.id.widget_title, song?.title ?: "Aura Music")
            views.setTextViewText(R.id.widget_artist, song?.artist ?: "Select a track to start")
            views.setTextViewText(R.id.widget_album, song?.album ?: "Aura Atmospheric Sound")

            val modeText = when (mode) {
                PlaybackMode.NORMAL -> "Mode: Sequential"
                PlaybackMode.SHUFFLE -> "Mode: Shuffle"
                PlaybackMode.REPEAT_ONE -> "Mode: Repeat One"
            }
            views.setTextViewText(R.id.widget_shuffle_indicator, modeText)

            val playIcon = if (isPlaying) R.drawable.ic_widget_pause else R.drawable.ic_widget_play
            views.setImageViewResource(R.id.widget_btn_play_pause, playIcon)

            val progress = if (durationMs > 0) ((positionMs * 1000) / durationMs).toInt().coerceIn(0, 1000) else 0
            views.setProgressBar(R.id.widget_progress, 1000, progress, false)

            views.setTextViewText(R.id.widget_time_current, formatTime(positionMs))
            views.setTextViewText(R.id.widget_time_duration, formatTime(durationMs))

            views.setOnClickPendingIntent(R.id.widget_btn_prev, getPendingActionIntent(context, ACTION_PREV, 6))
            views.setOnClickPendingIntent(R.id.widget_btn_play_pause, getPendingActionIntent(context, ACTION_PLAY_PAUSE, 7))
            views.setOnClickPendingIntent(R.id.widget_btn_next, getPendingActionIntent(context, ACTION_NEXT, 8))
            views.setOnClickPendingIntent(R.id.widget_root, getOpenAppPendingIntent(context))

            loadArtworkToWidget(context, views, song?.albumArtUri, R.id.widget_artwork)

            manager.updateAppWidget(id, views)
        }
    }

    private fun loadArtworkToWidget(context: Context, views: RemoteViews, artUri: Uri?, viewId: Int) {
        if (artUri == null) {
            views.setImageViewResource(viewId, R.drawable.ic_widget_music)
            return
        }
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(artUri)
            if (inputStream != null) {
                val bitmap: Bitmap? = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                if (bitmap != null) {
                    views.setImageViewBitmap(viewId, bitmap)
                    return
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        views.setImageViewResource(viewId, R.drawable.ic_widget_music)
    }

    private fun formatTime(millis: Long): String {
        val totalSec = (millis / 1000).coerceAtLeast(0)
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%d:%02d".format(min, sec)
    }
}
