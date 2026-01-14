package com.simon.mpm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Photos : BottomNavItem(
        route = Routes.PHOTOS,
        title = "照片",
        icon = Icons.Default.Photo
    )
    
    object Activities : BottomNavItem(
        route = Routes.ACTIVITIES,
        title = "活动",
        icon = Icons.Default.Event
    )
    
    object Albums : BottomNavItem(
        route = Routes.ALBUMS,
        title = "相册",
        icon = Icons.Default.PhotoAlbum
    )
    
    object Upload : BottomNavItem(
        route = Routes.UPLOAD,
        title = "上传",
        icon = Icons.Default.CloudUpload
    )
    
    object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        title = "设置",
        icon = Icons.Default.Settings
    )
    
    companion object {
        val items = listOf(Photos, Activities, Albums, Upload, Settings)
    }
}
