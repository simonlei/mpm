package com.simon.mpm.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.simon.mpm.data.database.dao.SyncDirectoryDao
import com.simon.mpm.data.database.dao.SyncFileDao
import com.simon.mpm.data.database.entity.SyncDirectory
import com.simon.mpm.data.database.entity.SyncFile

/**
 * MPM应用数据库
 */
@Database(
    entities = [
        SyncFile::class,
        SyncDirectory::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class MpmDatabase : RoomDatabase() {
    
    abstract fun syncFileDao(): SyncFileDao
    abstract fun syncDirectoryDao(): SyncDirectoryDao
    
    companion object {
        const val DATABASE_NAME = "mpm_database"
    }
}
