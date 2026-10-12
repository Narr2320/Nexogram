package app.nexogram.core.tdlib

class GhostModeManager {
    private var isGhostModeEnabled: Boolean = true
    private var blockReadReceipts: Boolean = true

    fun shouldBlockReadReceipts(): Boolean = isGhostModeEnabled && blockReadReceipts
    
    fun setGhostMode(enabled: Boolean) {
        isGhostModeEnabled = enabled
    }
}
