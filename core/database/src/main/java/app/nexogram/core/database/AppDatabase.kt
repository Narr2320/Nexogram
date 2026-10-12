package app.nexogram.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import app.nexogram.core.database.dao.DeletedMessageDao
import app.nexogram.core.database.entity.DeletedMessageEntity

@Database(entities = [DeletedMessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deletedMessageDao(): DeletedMessageDao
}
