package com.example.focuslock.logging

import android.util.Log

class PackageClassifier {

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