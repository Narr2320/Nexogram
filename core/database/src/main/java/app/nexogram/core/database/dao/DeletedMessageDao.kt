package app.nexogram.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.nexogram.core.database.entity.DeletedMessageEntity

@Dao
interface DeletedMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletedMessage(entity: DeletedMessageEntity)

    @Query("SELECT * FROM deleted_messages WHERE chatId = :chatId ORDER BY timestamp DESC")
    suspend fun getDeletedMessagesForChat(chatId: Long): List<DeletedMessageEntity>
}
