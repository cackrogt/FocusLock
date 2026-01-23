package com.example.focuslock

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class LockService : Service() {
    private lateinit var overlay: LockOverlay
    private val allowedApps = setOf(
        "com.google.android.apps.messaging",       // Gmail
        "com.samsungsds.nexsign.client.singleid.pub" // Calendar
    )

    override fun onCreate() {
        super.onCreate()
        startForegroundServiceInternal()
        FocusSessionState.start()

        OverlayController.init(applicationContext, allowedApps)
        OverlayController.show()
        //startMonitoring()
    }

    private fun startForegroundServiceInternal() {
        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "lock_channel")
            .setContentTitle("Focus Lock Active")
            .setContentText("Phone is locked except allowed apps")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()

        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "lock_channel",
                "Focus Lock Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent): IBinder? = null
}
