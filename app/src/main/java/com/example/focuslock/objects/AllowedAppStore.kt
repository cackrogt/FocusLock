package com.example.focuslock.objects

import android.content.Context

object AllowedAppStore {
    private const val PREFS = "allowed_app_prefs"
    private const val KEY_ALLOWED_APP = "allowed_app"

    fun set(context: Context, pkg: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ALLOWED_APP, pkg)
            .apply()
    }

    fun get(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_ALLOWED_APP, null)
    }
}