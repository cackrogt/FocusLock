package com.example.focuslock

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission

object FocusSessionManager {

    private const val SESSION_DURATION_MS = 1 * 60 * 1000L // 5 minutes

    @RequiresApi(Build.VERSION_CODES.S)
    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    fun startSession(context: Context) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, FocusSessionExpiredReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAt = System.currentTimeMillis() + SESSION_DURATION_MS

        if(alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
        }
    }
}
