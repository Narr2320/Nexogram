package app.nexogram.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_messages")
data class DeletedMessageEntity(
    @PrimaryKey val messageId: Long,
    val chatId: Long,
    val senderId: Long,
    val textContent: String,
    val timestamp: Long,
    val deletedAt: Long = System.currentTimeMillis()
)
