package com.simon.mpm.navigation

/**
 * 导航路由定义
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    
    // 底部导航路由
    const val PHOTOS = "photos"
    const val ACTIVITIES = "activities"
    const val ALBUMS = "albums"
    const val SETTINGS = "settings"
    
    // 详情页路由
    const val PHOTO_LIST = "photo_list"
    const val PHOTO_DETAIL = "photo_detail/{photoId}?fromTrash={fromTrash}"
    const val TRASH = "trash?trashed={trashed}"
    
    fun photoDetail(photoId: Int, fromTrash: Boolean = false) = "photo_detail/$photoId?fromTrash=$fromTrash"
    fun trash() = "trash?trashed=true"
}