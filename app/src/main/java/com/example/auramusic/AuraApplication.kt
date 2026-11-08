package com.example.auramusic

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.auramusic.data.local.AuraDatabase
import com.example.auramusic.playback.AuraPlayerController

class AuraApplication : Application() {

    companion object {
        const val PLAYBACK_CHANNEL_ID = "aura_playback_channel"
        lateinit var instance: AuraApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannel()
        // Initialize Room DB early
        AuraDatabase.getInstance(this)
        // Initialize Player controller
        AuraPlayerController.initialize(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Aura Music Playback"
            val descriptionText = "Persistent playback notification and controls"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(PLAYBACK_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
