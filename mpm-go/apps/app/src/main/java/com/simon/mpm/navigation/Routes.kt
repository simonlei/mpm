package com.simon.mpm.navigation

/**
 * 导航路由定义
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PHOTO_LIST = "photo_list"
    const val PHOTO_DETAIL = "photo_detail/{photoId}"
    const val TRASH = "trash?trashed={trashed}"
    
    fun photoDetail(photoId: Int) = "photo_detail/$photoId"
    fun trash() = "trash?trashed=true"
}