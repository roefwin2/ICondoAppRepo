package com.idsolution.icondoapp.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.idsolution.icondoapp.di.initKoin
import com.idsolution.icondoapp.feature.voip.VoipServiceFactory
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import timber.log.Timber

class CondoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize logging
        Timber.plant(Timber.DebugTree())

        // Create notification channels
        createNotificationChannels()

        // Initialize VoIP service factory with application context
        VoipServiceFactory.init(this)

        // Initialize Koin DI
        initKoin {
            androidLogger()
            androidContext(this@CondoApplication)
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Main notification channel
            val mainChannel = NotificationChannel(
                "condo_channel_id",
                "ICondo Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "General notifications"
            }
            notificationManager.createNotificationChannel(mainChannel)

            // VoIP channel
            val voipChannel = NotificationChannel(
                "icondo_voip_channel",
                "ICondo VoIP Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps the app ready for incoming video calls"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(voipChannel)
        }
    }
}
