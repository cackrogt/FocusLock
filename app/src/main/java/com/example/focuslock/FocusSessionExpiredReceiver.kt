package com.example.focuslock

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi

class FocusSessionExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        FocusSessionState.end()

        // Remove overlay
        OverlayController.hide()

        context.stopService(
            Intent(context, LockService::class.java)
        )

        // Later we will stop the service & cleanup state here
    }
}
