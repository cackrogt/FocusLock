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

    private var currentStableApp: String? = null
    private var lastChangeTime = 0L
    private val STABILITY_DELAY_MS = 100L

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
        val now = System.currentTimeMillis()
        val isAllowed = pkg in getAllowedApps()
        val isIgnored = pkg in toBeIgnored

        logEvent(pkg, className)

        val isKnown = KnownAppsStore.contains(pkg)
        if (!isKnown) {
            Log.i("ANKU_FOCUS_DEBUG", "IGNORED UNKNOWN: $pkg")
        } else {
            Log.i("ANKU_FOCUS_DEBUG", "KNOWN: $pkg")
        }

        if(!isKnown) {
            Log.i("ANKU_FOCUS_IGNORE", "Ignored: $pkg")
            return;
        }

        val isLaunchable = LaunchableAppsStore.contains(pkg)

        if (isAllowed) {
            OverlayController.hide()
        } else if (pkg in toBeLocked) {
            goToHome()
            lockDevice()
        } else if (!isLaunchable) {
            // maintain prev;
        } else {
            OverlayController.show()
        }

        classifyEvent(pkg, className)
    }

    override fun onInterrupt() {}

    private fun logEvent(pkg: String, className: String?) {
        Log.i(
            "ANKU_FOCUSLOCKFORE",
            "Foreground changed → pkg=$pkg  class=$className"
        )
    }

    private fun classifyEvent(pkg: String, className: String?) {
        when {
            isLauncher(pkg) ->
                Log.i("ANKU_FOCUSLOCK", "→ HOME detected")

            isRecents(pkg, className) ->
                Log.i("ANKU_FOCUSLOCK", "→ RECENTS detected")

            isSettings(pkg) ->
                Log.i("ANKU_FOCUSLOCK", "→ SETTINGS detected")

            isSystemUI(pkg) ->
                Log.i("ANKU_FOCUSLOCK", "→ SYSTEM UI (shade / lock / recents)")

            else ->
                Log.i("ANKU_FOCUSLOCK", "→ APP = $pkg")
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

