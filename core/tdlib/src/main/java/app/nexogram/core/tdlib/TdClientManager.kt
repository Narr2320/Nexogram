package app.nexogram.core.tdlib

import android.content.Context

class TdClientManager(private val context: Context) {

    val ghostModeManager = GhostModeManager()

    fun initialize() {
        // Инициализация TDLib клиента
    }

    /**
     * Отправка статуса прочтения (блокируется, если включен Ghost Mode)
     */
    fun markAsRead(chatId: Long, messageId: Long) {
        if (ghostModeManager.shouldBlockReadReceipts()) {
            return
        }
        // Логика отправки отчета о прочтении в TDLib
    }
}
