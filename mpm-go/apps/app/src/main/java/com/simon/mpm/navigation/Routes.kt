package com.simon.mpm.navigation

/**
 * 导航路由定义
 */
object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    
    // 底部导航路由（6个Tab）
    const val ALL_PHOTOS = "all_photos"      // 全部照片
    const val ALBUMS = "albums"              // 相册
    const val TIMELINE = "timeline"          // 时间轴
    const val LOCATIONS = "locations"        // 位置
    const val PEOPLE = "people"              // 人脸
    const val SETTINGS = "settings"          // 设置
    
    // 保留旧路由用于兼容（将在设置页中使用）
    const val PHOTOS = "photos"              // 已废弃，保留用于兼容
    const val ACTIVITIES = "activities"      // 活动页面（在设置中访问）
    const val UPLOAD = "upload"              // 上传页面（在设置中访问）
    
    // 详情页路由
    const val PHOTO_LIST = "photo_list"
    const val PHOTO_DETAIL = "photo_detail/{photo_id}?fromTrash={fromTrash}"
    const val TRASH = "trash?trashed={trashed}"
    const val ACTIVITY_DETAIL = "activity_detail/{activityId}"
    
    fun photoDetail(photoId: Int, fromTrash: Boolean = false) = "photo_detail/$photoId?fromTrash=$fromTrash"
    fun trash() = "trash?trashed=true"
    fun activityDetail(activityId: Int) = "activity_detail/$activityId"
}