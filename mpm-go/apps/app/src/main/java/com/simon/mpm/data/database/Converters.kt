package com.simon.mpm.data.database

import androidx.room.TypeConverter
import com.simon.mpm.data.database.entity.SyncStatus

/**
 * Room类型转换器
 */
class Converters {
    
    /**
     * SyncStatus转String
     */
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }
    
    /**
     * String转SyncStatus
     */
    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus {
        return SyncStatus.valueOf(value)
    }
}
