package com.example.focuslock.objects

object FocusSessionState {

    private var startTime: Long = 0L
    private var duration: Long = 0L

    fun start(durationMs: Long) {
        startTime = System.currentTimeMillis()
        duration = durationMs
    }

    fun isActive(): Boolean {
        if (startTime == 0L) return false
        return System.currentTimeMillis() - startTime < duration
    }

    fun end() {
        startTime = 0L
        duration = 0L
    }

    fun getRemainingMs(): Long {
        if (startTime == 0L) return 0L
        val elapsed = System.currentTimeMillis() - startTime
        return (duration - elapsed).coerceAtLeast(0)
    }
}