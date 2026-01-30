package com.simon.mpm.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 底部导航项（6个Tab）
 */
sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object AllPhotos : BottomNavItem(
        route = Routes.ALL_PHOTOS,
        title = "全部",
        icon = Icons.Default.Photo
    )
    
    object Albums : BottomNavItem(
        route = Routes.ALBUMS,
        title = "相册",
        icon = Icons.Default.PhotoAlbum
    )
    
    object Timeline : BottomNavItem(
        route = Routes.TIMELINE,
        title = "时间轴",
        icon = Icons.Default.DateRange
    )
    
    object Locations : BottomNavItem(
        route = Routes.LOCATIONS,
        title = "位置",
        icon = Icons.Default.LocationOn
    )
    
    object People : BottomNavItem(
        route = Routes.PEOPLE,
        title = "人脸",
        icon = Icons.Default.Face
    )
    
    object Settings : BottomNavItem(
        route = Routes.SETTINGS,
        title = "设置",
        icon = Icons.Default.Settings
    )
    
    companion object {
        val items = listOf(AllPhotos, Albums, Timeline, Locations, People, Settings)
    }
}
