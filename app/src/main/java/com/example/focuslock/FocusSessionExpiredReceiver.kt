package com.example.focuslock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.focuslock.lock_overlay.LockService
import com.example.focuslock.objects.FocusSessionState
import com.example.focuslock.objects.OverlayController

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
