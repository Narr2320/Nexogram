package app.nexogram.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.nexogram.core.database.entity.DeletedMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeletedMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedMessage(message: DeletedMessageEntity)

    @Query("SELECT * FROM deleted_messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    fun getDeletedMessagesForChat(chatId: Long): Flow<List<DeletedMessageEntity>>

    @Query("DELETE FROM deleted_messages WHERE chatId = :chatId")
    suspend fun clearChatHistory(chatId: Long)
}
