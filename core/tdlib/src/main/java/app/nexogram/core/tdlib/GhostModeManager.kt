package app.nexogram.core.tdlib

class GhostModeManager {
    private var isGhostActive: Boolean = false

    fun setGhostModeEnabled(enabled: Boolean) {
        isGhostActive = enabled
    }

    fun isGhostModeEnabled(): Boolean {
        return isGhostActive
    }

    /**
     * Перехват отправки статуса "печатает" или "в сети"
     */
    fun shouldBlockOnlineStatus(): Boolean {
        return isGhostActive
    }

    /**
     * Перехват отправки отчетов о прочтении сообщений
     */
    fun shouldBlockReadReceipts(): Boolean {
        return isGhostActive
    }
}
