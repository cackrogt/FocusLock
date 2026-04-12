package com.example.focuslock

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