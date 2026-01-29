package com.simon.mpm

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * 测试文件的Content-Type检测
 */
@RunWith(AndroidJUnit4::class)
class ContentTypeTest {

    @Test
    fun testFileContentType() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        
        // 测试文件路径
        val testFile = File("D:/work/mpm/mpm-go/apps/app/src/test/6f11c687-ebba-4e6c-b2b8-df9280316777.jpg")
        
        if (!testFile.exists()) {
            println("文件不存在: ${testFile.absolutePath}")
            return
        }
        
        println("文件存在: ${testFile.absolutePath}")
        println("文件大小: ${testFile.length()} bytes")
        
        // 方法1: 使用文件扩展名判断
        val extension = testFile.extension.lowercase()
        val mimeTypeByExtension = when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "mp4" -> "video/mp4"
            "mov" -> "video/quicktime"
            else -> "application/octet-stream"
        }
        println("根据扩展名判断的MIME类型: $mimeTypeByExtension")
        
        // 方法2: 读取文件头判断（魔数）
        val fileHeader = testFile.inputStream().use { input ->
            val buffer = ByteArray(12)
            val bytesRead = input.read(buffer)
            buffer.take(bytesRead).map { String.format("%02X", it) }.joinToString(" ")
        }
        println("文件头 (前12字节): $fileHeader")
        
        // 判断文件类型
        val actualMimeType = when {
            fileHeader.startsWith("FF D8 FF") -> "image/jpeg"
            fileHeader.startsWith("89 50 4E 47") -> "image/png"
            fileHeader.startsWith("47 49 46") -> "image/gif"
            fileHeader.contains("66 74 79 70") -> "video/mp4" // ftyp
            fileHeader.startsWith("00 00 00") && fileHeader.contains("66 74 79 70") -> "video/quicktime"
            else -> "unknown"
        }
        println("根据文件头判断的实际MIME类型: $actualMimeType")
        
        // 方法3: 尝试使用ContentResolver (需要URI)
        try {
            val uri = Uri.fromFile(testFile)
            val mimeTypeFromUri = context.contentResolver.getType(uri)
            println("ContentResolver返回的MIME类型: $mimeTypeFromUri")
        } catch (e: Exception) {
            println("ContentResolver获取失败: ${e.message}")
        }
        
        // 总结
        println("\n=== 总结 ===")
        println("文件名: ${testFile.name}")
        println("扩展名判断: $mimeTypeByExtension")
        println("文件头判断: $actualMimeType")
        
        if (actualMimeType != mimeTypeByExtension && actualMimeType != "unknown") {
            println("⚠️ 警告: 这是一个动态照片或伪装文件！")
            println("   文件扩展名显示为: $extension")
            println("   但实际内容是: $actualMimeType")
        }
    }
}
