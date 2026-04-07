object FocusSessionState {

    private const val DURATION_MS = 10 * 60 * 1000L // 5 minutes
    private var startTime: Long = 0L

    fun start() {
        startTime = System.currentTimeMillis()
    }

    fun isActive(): Boolean {
        if (startTime == 0L) return false
        return System.currentTimeMillis() - startTime < DURATION_MS
    }

    fun end() {
        startTime = 0L
    }
}
