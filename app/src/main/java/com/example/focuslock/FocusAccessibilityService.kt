package com.example.focuslock

import android.accessibilityservice.AccessibilityService
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOWS_CHANGED
import android.view.accessibility.AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
import com.example.focuslock.objects.AllowedAppStore
import com.example.focuslock.objects.FocusSessionState
import com.example.focuslock.objects.LaunchableAppsStore
import com.example.focuslock.objects.OverlayController

class FocusAccessibilityService : AccessibilityService() {

    private fun getAllowedApps(): Set<String> {
        val userApp = AllowedAppStore.get(this)

        return setOfNotNull(
            "com.google.android.apps.messaging",
            "com.samsungsds.nexsign.client.singleid.pub",
            userApp
        )
    }

    private val toBeLocked = setOf(
        "com.android.settings",
    )

    private val toBeIgnored = setOf(
        //"com.sec.android.app.launcher",
        "com.android.systemui",
        "com.samsung.android.honeyboard"
    )

    private fun lockDevice() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE)
                as DevicePolicyManager

        val admin = ComponentName(
            this,
            FocusDeviceAdminReceiver::class.java
        )

        if (dpm.isAdminActive(admin)) {
            dpm.lockNow()
        }
    }

    private fun goToHome() {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        if (!FocusSessionState.isActive()) {
            OverlayController.hide()
            return
        }

        if (event.eventType != TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != TYPE_WINDOWS_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) return

        val className = event.className?.toString()

        val pkg = event.packageName?.toString() ?: return
        //val now = System.currentTimeMillis()
        val isAllowed = pkg in getAllowedApps()
        //val isIgnored = pkg in toBeIgnored



        val isLaunchable = LaunchableAppsStore.contains(pkg)

        if (isAllowed) {
            logEvent("Allowed", pkg, className)
            OverlayController.hide()
        } else if (pkg in toBeLocked) {
            logEvent("ToBeLocked", pkg, className)
            goToHome()
            lockDevice()
        } else if (!isLaunchable) {
            logEvent("Ignored", pkg, className)
            // maintain prev;
        } else {
            logEvent("NotInList", pkg, className)
            OverlayController.show()
        }

        //classifyEvent(pkg, className)
    }

    override fun onInterrupt() {}

    private fun logEvent(reason: String, pkg: String, className: String?) {
        Log.i(
            "ANKU_FOCUSLOCKFORE",
            "Foreground changed → pkg=$pkg  class=$className reason=$reason"
        )
    }

}

