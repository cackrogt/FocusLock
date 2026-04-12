package com.example.focuslock.objects

object LaunchableAppsStore {
    private val apps = mutableSetOf<String>()

    fun set(appList: Set<String>) {
        apps.clear()
        apps.addAll(appList)
    }

    fun contains(pkg: String): Boolean {
        return apps.contains(pkg)
    }

    fun getAll(): Set<String> = apps
}