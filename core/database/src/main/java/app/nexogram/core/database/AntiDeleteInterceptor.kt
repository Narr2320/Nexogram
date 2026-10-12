package app.nexogram.core.database

import app.nexogram.core.database.dao.DeletedMessageDao
import app.nexogram.core.database.entity.DeletedMessageEntity

class AntiDeleteInterceptor(private val deletedMessageDao: DeletedMessageDao) {

    /**
     * Перехват запроса на удаление сообщения от сервера/собеседника
     */
    async fun onMessageDeleted(messageId: Long, chatId: Long, senderId: Long, textContent: String) {
        val entity = DeletedMessageEntity(
            messageId = messageId,
            chatId = chatId,
            senderId = senderId,
            textContent = textContent,
            timestamp = System.currentTimeMillis()
        )
        deletedMessageDao.insertDeletedMessage(entity)
    }
}
