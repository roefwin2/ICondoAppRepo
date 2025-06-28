package com.idsolution.icondoapp.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.testkmpapp.di.initKoin
import com.example.voip.voip.core.di.coreModule
import com.example.voip.voip.data.di.voipDataModule
import com.example.voip.voip.presenter.di.voipViewModelModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.dsl.module
import timber.log.Timber

class CondoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        Timber.plant(Timber.DebugTree())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "condo_channel_id",
                "Channel name",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        initKoin {
            androidLogger()
            androidContext(this@CondoApplication)
        }
    }
}