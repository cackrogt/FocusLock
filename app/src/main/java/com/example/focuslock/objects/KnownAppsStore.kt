package com.example.focuslock.objects

object KnownAppsStore {
    private val knownApps = mutableSetOf<String>()

    fun set(apps: Set<String>) {
        knownApps.clear()
        knownApps.addAll(apps)
    }

    fun contains(pkg: String): Boolean {
        return knownApps.contains(pkg)
    }

    fun getAll(): Set<String> = knownApps
}