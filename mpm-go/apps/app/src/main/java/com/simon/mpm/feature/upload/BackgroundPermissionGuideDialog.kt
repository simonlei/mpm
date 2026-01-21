package com.simon.mpm.feature.upload

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

/**
 * 后台权限引导对话框
 * 引导用户设置后台运行权限
 */
@Composable
fun BackgroundPermissionGuideDialog(
    manufacturer: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("允许后台上传") },
        text = {
            Column {
                Text("为了确保照片能在后台上传，请进行以下设置：")
                Spacer(modifier = Modifier.height(12.dp))

                // 根据手机品牌显示不同的引导
                when (manufacturer) {
                    "vivo", "iqoo" -> {
                        Text("1. 打开 i管家 > 应用管理")
                        Text("2. 找到 MPM 应用")
                        Text("3. 允许「自启动」和「后台运行」")
                    }
                    "oppo", "oneplus", "realme" -> {
                        Text("1. 打开 设置 > 应用管理")
                        Text("2. 找到 MPM 应用")
                        Text("3. 允许「后台运行」")
                    }
                    "xiaomi", "redmi" -> {
                        Text("1. 打开 设置 > 应用设置")
                        Text("2. 找到 MPM 应用")
                        Text("3. 关闭「省电优化」")
                        Text("4. 允许「后台弹出界面」")
                    }
                    "huawei", "honor" -> {
                        Text("1. 打开 设置 > 应用")
                        Text("2. 找到 MPM 应用")
                        Text("3. 打开「应用启动管理」")
                        Text("4. 允许「后台活动」")
                    }
                    else -> {
                        Text("1. 打开系统设置")
                        Text("2. 找到电池或应用管理")
                        Text("3. 允许本应用后台运行")
                        Text("4. 关闭省电优化")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "提示：设置后可以锁屏或切换应用，上传会在后台继续进行。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // 打开应用设置页面
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // 如果失败，打开通用设置页面
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }
                onDismiss()
            }) {
                Text("去设置")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("稍后")
            }
        }
    )
}
