package com.simon.mpm.data.model

/**
 * 同步冲突处理策略
 */
enum class SyncConflictStrategy(val displayName: String) {
    /**
     * 跳过已存在的文件
     */
    SKIP("跳过"),
    
    /**
     * 覆盖已存在的文件
     */
    OVERWRITE("覆盖"),
    
    /**
     * 重命名新文件（添加后缀）
     */
    RENAME("重命名");
    
    companion object {
        /**
         * 从字符串获取策略
         */
        fun fromString(value: String): SyncConflictStrategy {
            return values().find { it.name == value } ?: SKIP
        }
    }
}
