package com.simon.mpm.util

import android.content.Context
import android.net.Uri
import android.util.Log

/**
 * 统一的 MIME 类型检测工具
 * 优先级：
 * 1. 文件头字节检测（最准确，可识别动态照片等特殊情况）
 * 2. 文件扩展名推断（兜底方案）
 */
object MimeTypeHelper {

    private const val TAG = "MimeTypeHelper"

    /**
     * 根据URI和文件名获取Content-Type
     * 优先读取文件头判断真实MIME类型
     * 如果读取失败，则根据文件扩展名判断
     */
    fun getContentType(context: Context, uri: Uri, fileName: String): String {
        // 优先尝试读取文件头来判断真实的MIME类型
        val mimeTypeFromHeader = try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val header = ByteArray(12)
                val bytesRead = inputStream.read(header)
                if (bytesRead > 0) {
                    detectMimeTypeFromHeader(header, bytesRead)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "无法读取文件头: $uri", e)
            null
        }

        if (!mimeTypeFromHeader.isNullOrBlank()) {
            Log.d(TAG, "从文件头检测到MIME类型: $mimeTypeFromHeader")
            return mimeTypeFromHeader
        }

        // 回退到基于文件扩展名的判断
        Log.d(TAG, "使用文件扩展名判断MIME类型")
        return getContentTypeByExtension(fileName)
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    fun getContentTypeByExtension(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            // 图片格式
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "bmp" -> "image/bmp"
            "heic", "heif" -> "image/heic"
            // 视频格式
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            "avi" -> "video/x-msvideo"
            "mkv" -> "video/x-matroska"
            "webm" -> "video/webm"
            "3gp" -> "video/3gpp"
            "m4v" -> "video/x-m4v"
            "wmv" -> "video/x-ms-wmv"
            "flv" -> "video/x-flv"
            // 默认
            else -> "application/octet-stream"
        }
    }

    /**
     * 根据文件头字节判断MIME类型
     */
    internal fun detectMimeTypeFromHeader(header: ByteArray, size: Int): String? {
        if (size < 4) return null

        // JPEG: FF D8 FF
        if (header[0] == 0xFF.toByte() && header[1] == 0xD8.toByte() && header[2] == 0xFF.toByte()) {
            return "image/jpeg"
        }

        // PNG: 89 50 4E 47
        if (header[0] == 0x89.toByte() && header[1] == 0x50.toByte() &&
            header[2] == 0x4E.toByte() && header[3] == 0x47.toByte()) {
            return "image/png"
        }

        // GIF: 47 49 46 38
        if (header[0] == 0x47.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x38.toByte()) {
            return "image/gif"
        }

        // WebP: 52 49 46 46 ... 57 45 42 50
        if (size >= 12 && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x46.toByte() &&
            header[8] == 0x57.toByte() && header[9] == 0x45.toByte() &&
            header[10] == 0x42.toByte() && header[11] == 0x50.toByte()) {
            return "image/webp"
        }

        // MP4/MOV: 检查 ftyp box (偏移4字节处)
        if (size >= 12) {
            val ftyp = String(header, 4, 4, Charsets.ISO_8859_1)
            if (ftyp.startsWith("ftyp")) {
                val brand = String(header, 8, 4, Charsets.ISO_8859_1)
                // HEIC/HEIF 检测
                if (brand.startsWith("heic") || brand.startsWith("heix") ||
                    brand.startsWith("hevc") || brand.startsWith("hevx") ||
                    brand.startsWith("mif1")) {
                    return "image/heic"
                }
                // 视频格式检测
                return when {
                    brand.startsWith("mp4") || brand.startsWith("isom") ||
                    brand.startsWith("M4V") || brand.startsWith("M4A") -> "video/mp4"
                    brand.startsWith("qt") -> "video/quicktime"
                    else -> "video/mp4"
                }
            }
        }

        // AVI: 52 49 46 46 ... 41 56 49 20
        if (size >= 12 && header[0] == 0x52.toByte() && header[1] == 0x49.toByte() &&
            header[2] == 0x46.toByte() && header[3] == 0x46.toByte() &&
            header[8] == 0x41.toByte() && header[9] == 0x56.toByte() &&
            header[10] == 0x49.toByte() && header[11] == 0x20.toByte()) {
            return "video/x-msvideo"
        }

        return null
    }
}
