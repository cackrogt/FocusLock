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

class FocusAccessibilityService : AccessibilityService() {

    private fun getAllowedApps(): Set<String> {
        val userApp = AllowedAppStore.get(this)

        return setOfNotNull(
            "com.google.android.apps.messaging",
            "com.samsungsds.nexsign.client.singleid.pub",
            userApp
        )
    }

    private val allowedApps = setOf(
        "com.google.android.apps.messaging",
        "com.samsungsds.nexsign.client.singleid.pub"
    )

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

//        if (pkg == "com.android.systemui") {
//            OverlayController.show()
//            return
//        }

        if (pkg in getAllowedApps() || pkg in toBeIgnored) {
            OverlayController.hide()
        } else if (pkg in toBeLocked) {
            goToHome()
            lockDevice()
        } else{
            OverlayController.show()
        }

        logEvent(pkg, className)
        classifyEvent(pkg, className)
    }

    override fun onInterrupt() {}

    private fun logEvent(pkg: String, className: String?) {
        Log.d(
            "FOCUSLOCK",
            "Foreground changed → pkg=$pkg  class=$className"
        )
    }

    private fun classifyEvent(pkg: String, className: String?) {
        when {
            isLauncher(pkg) ->
                Log.d("FOCUSLOCK", "→ HOME detected")

            isRecents(pkg, className) ->
                Log.d("FOCUSLOCK", "→ RECENTS detected")

            isSettings(pkg) ->
                Log.d("FOCUSLOCK", "→ SETTINGS detected")

            isSystemUI(pkg) ->
                Log.d("FOCUSLOCK", "→ SYSTEM UI (shade / lock / recents)")

            else ->
                Log.d("FOCUSLOCK", "→ APP = $pkg")
        }
    }

    private fun isLauncher(pkg: String): Boolean {
        return pkg.contains("launcher")
    }

    private fun isRecents(pkg: String, className: String?): Boolean {
        return pkg == "com.android.systemui" &&
                className?.contains("Recents") == true
    }

    private fun isSettings(pkg: String): Boolean {
        return pkg == "com.android.settings"
    }

    private fun isSystemUI(pkg: String): Boolean {
        return pkg == "com.android.systemui"
    }

}

