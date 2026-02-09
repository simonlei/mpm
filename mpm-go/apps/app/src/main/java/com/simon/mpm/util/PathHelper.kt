package com.simon.mpm.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 文件路径构建工具
 * 统一 UploadViewModel 和 PhotoSyncService 中的路径构建逻辑
 */
object PathHelper {

    /**
     * 构建上传目标路径：用户名/年份/月份/原文件名
     */
    fun buildTargetPath(account: String, lastModified: Long, fileName: String): String {
        val date = Date(lastModified)
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MM", Locale.getDefault())

        val year = yearFormat.format(date)
        val month = monthFormat.format(date)

        return "$account/$year/$month/$fileName"
    }
}
