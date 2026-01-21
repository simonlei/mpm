package com.simon.mpm.util

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.PowerManager

/**
 * 后台限制检测器
 * 检测系统是否对后台任务有限制
 */
class BackgroundRestrictionChecker(private val context: Context) {

    /**
     * 检查是否有后台限制
     */
    fun isBackgroundRestricted(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            return am.isBackgroundRestricted
        }
        return false
    }

    /**
     * 检查是否处于省电模式
     */
    fun isPowerSaveMode(): Boolean {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isPowerSaveMode
    }

    /**
     * 是否应该显示后台权限引导
     */
    fun shouldShowWarning(): Boolean {
        return isBackgroundRestricted() || isPowerSaveMode() || isChineseManufacturer()
    }

    /**
     * 检查是否是国产手机品牌
     */
    private fun isChineseManufacturer(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return manufacturer in listOf(
            "xiaomi", "redmi", "oppo", "vivo", "oneplus", 
            "huawei", "honor", "realme", "meizu"
        )
    }

    /**
     * 获取手机品牌
     */
    fun getManufacturer(): String {
        return Build.MANUFACTURER.lowercase()
    }
}
