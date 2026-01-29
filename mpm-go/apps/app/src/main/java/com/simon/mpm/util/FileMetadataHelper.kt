package com.simon.mpm.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 文件元数据辅助工具
 * 用于提取照片的拍摄日期等元数据信息
 */
object FileMetadataHelper {
    private const val TAG = "FileMetadataHelper"

    /**
     * 获取照片的最佳日期时间
     * 优先级：EXIF拍摄日期 > MediaStore DATE_TAKEN > 文件修改时间
     *
     * @param context 上下文
     * @param uri 文件URI
     * @param fallbackModifiedTime 备用的文件修改时间（毫秒时间戳）
     * @return 最佳日期时间（毫秒时间戳）
     */
    fun getBestDateTime(
        context: Context,
        uri: Uri,
        fallbackModifiedTime: Long
    ): Long {
        var exifDateTime = 0L
        var dateTaken = 0L

        try {
            // 1. 尝试从EXIF读取拍摄日期（最准确）
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                try {
                    val exif = ExifInterface(inputStream)
                    val dateTimeOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)

                    // 优先使用原始拍摄时间
                    val exifDateStr = dateTimeOriginal ?: dateTime
                    if (exifDateStr != null) {
                        // EXIF日期格式：yyyy:MM:dd HH:mm:ss
                        val exifFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                        exifDateTime = exifFormat.parse(exifDateStr)?.time ?: 0L
                        Log.d(TAG, "EXIF日期: $exifDateStr -> $exifDateTime")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "读取EXIF失败", e)
                }
            }

            // 2. 尝试从MediaStore读取DATE_TAKEN（次选）
            if (exifDateTime == 0L) {
                val projection = arrayOf(MediaStore.Images.Media.DATE_TAKEN)
                context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
                        if (dateTakenIndex >= 0) {
                            dateTaken = cursor.getLong(dateTakenIndex)
                            Log.d(TAG, "MediaStore DATE_TAKEN: $dateTaken")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "获取日期信息失败", e)
        }

        // 3. 返回最佳日期（优先级：EXIF > MediaStore > 文件修改时间）
        val bestDateTime = when {
            exifDateTime > 0 -> exifDateTime
            dateTaken > 0 -> dateTaken
            else -> fallbackModifiedTime
        }

        Log.d(TAG, "最佳日期: $bestDateTime (EXIF=$exifDateTime, MediaStore=$dateTaken, 文件修改=$fallbackModifiedTime)")
        return bestDateTime
    }

    /**
     * 从文件路径获取最佳日期时间
     *
     * @param context 上下文
     * @param filePath 文件路径
     * @param fallbackModifiedTime 备用的文件修改时间（毫秒时间戳）
     * @return 最佳日期时间（毫秒时间戳）
     */
    fun getBestDateTimeFromPath(
        context: Context,
        filePath: String,
        fallbackModifiedTime: Long
    ): Long {
        return try {
            Log.d(TAG, "getBestDateTimeFromPath: $filePath")
            
            // 将文件路径转换为URI
            val uri = if (filePath.startsWith("content://")) {
                Uri.parse(filePath)
            } else {
                // 通过MediaStore查询文件URI
                val projection = arrayOf(MediaStore.Files.FileColumns._ID)
                val selection = "${MediaStore.Files.FileColumns.DATA} = ?"
                val selectionArgs = arrayOf(filePath)

                context.contentResolver.query(
                    MediaStore.Files.getContentUri("external"),
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID))
                        Uri.withAppendedPath(MediaStore.Files.getContentUri("external"), id.toString())
                    } else {
                        Log.w(TAG, "MediaStore未找到文件，尝试直接读取: $filePath")
                        null
                    }
                }
            }

            // 如果成功获取URI，使用getBestDateTime
            if (uri != null) {
                return getBestDateTime(context, uri, fallbackModifiedTime)
            }
            
            // 如果MediaStore查询失败，尝试直接从文件路径读取EXIF
            var exifDateTime = 0L
            try {
                val file = java.io.File(filePath)
                if (file.exists() && file.canRead()) {
                    val exif = ExifInterface(file)
                    val dateTimeOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)
                    val dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME)
                    
                    val exifDateStr = dateTimeOriginal ?: dateTime
                    if (exifDateStr != null) {
                        val exifFormat = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
                        exifDateTime = exifFormat.parse(exifDateStr)?.time ?: 0L
                        Log.d(TAG, "直接读取EXIF成功: $exifDateStr -> $exifDateTime")
                    }
                } else {
                    Log.w(TAG, "文件不存在或无法读取: $filePath")
                }
            } catch (e: Exception) {
                Log.w(TAG, "直接读取EXIF失败: ${e.message}")
            }
            
            // 返回EXIF日期或备用时间
            val result = if (exifDateTime > 0) exifDateTime else fallbackModifiedTime
            Log.d(TAG, "最终日期: $result (EXIF=$exifDateTime, 备用=$fallbackModifiedTime)")
            result
        } catch (e: Exception) {
            Log.w(TAG, "从路径获取日期失败: $filePath", e)
            fallbackModifiedTime
        }
    }
}
