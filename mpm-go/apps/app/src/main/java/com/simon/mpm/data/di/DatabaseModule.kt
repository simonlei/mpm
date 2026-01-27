package com.simon.mpm.data.di

import android.content.Context
import androidx.room.Room
import com.simon.mpm.data.database.MpmDatabase
import com.simon.mpm.data.database.dao.SyncDirectoryDao
import com.simon.mpm.data.database.dao.SyncFileDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块
 * 提供Room数据库和DAO的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * 提供数据库实例
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): MpmDatabase {
        return Room.databaseBuilder(
            context,
            MpmDatabase::class.java,
            MpmDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration() // 开发阶段使用，生产环境需要提供迁移策略
            .build()
    }
    
    /**
     * 提供SyncFileDao
     */
    @Provides
    @Singleton
    fun provideSyncFileDao(database: MpmDatabase): SyncFileDao {
        return database.syncFileDao()
    }
    
    /**
     * 提供SyncDirectoryDao
     */
    @Provides
    @Singleton
    fun provideSyncDirectoryDao(database: MpmDatabase): SyncDirectoryDao {
        return database.syncDirectoryDao()
    }
}
