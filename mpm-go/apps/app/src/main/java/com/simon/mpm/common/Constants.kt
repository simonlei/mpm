package com.simon.mpm.common

/**
 * 应用常量
 */
object Constants {
    // 网络相关
    const val DEFAULT_TIMEOUT = 30L // 秒
    const val DEFAULT_PAGE_SIZE = 75
    
    // 本地存储Key
    const val PREF_SERVER_URL = "server_url"
    const val PREF_ACCOUNT = "account"
    const val PREF_SIGNATURE = "signature"
    const val PREF_IMAGE_QUALITY = "image_quality"
    const val PREF_THEME_MODE = "theme_mode"
    const val PREF_UPLOAD_WIFI_ONLY = "upload_wifi_only"
    
    // 自动同步相关
    const val PREF_AUTO_SYNC_ENABLED = "auto_sync_enabled"
    const val PREF_SYNC_DIRECTORIES = "sync_directories"
    const val PREF_SYNC_INTERVAL = "sync_interval"
    const val PREF_SYNC_WIFI_ONLY = "sync_wifi_only"
    const val PREF_SYNC_FILE_TYPES = "sync_file_types"
    const val PREF_LAST_SYNC_TIME = "last_sync_time"
    const val PREF_SYNC_CONFLICT_STRATEGY = "sync_conflict_strategy"
    
    // 图片质量
    const val IMAGE_QUALITY_ORIGINAL = "original"
    const val IMAGE_QUALITY_HIGH = "high"
    const val IMAGE_QUALITY_STANDARD = "standard"
    
    // 主题模式
    const val THEME_MODE_SYSTEM = "system"
    const val THEME_MODE_LIGHT = "light"
    const val THEME_MODE_DARK = "dark"
    
    // 同步间隔
    const val SYNC_INTERVAL_IMMEDIATE = "immediate"
    const val SYNC_INTERVAL_HOURLY = "hourly"
    const val SYNC_INTERVAL_DAILY = "daily"
    const val SYNC_INTERVAL_WIFI_ONLY = "wifi_only"
    
    // 同步文件类型
    const val SYNC_FILE_TYPE_ALL = "all"
    const val SYNC_FILE_TYPE_IMAGES = "images"
    const val SYNC_FILE_TYPE_VIDEOS = "videos"
    
    // API路径
    const val API_CHECK_PASSWORD = "/api/checkPassword"
    const val API_GET_PICS = "/api/getPics"
    const val API_UPDATE_IMAGE = "/api/updateImage"
    const val API_TRASH_PHOTOS = "/api/trashPhotos"
    const val API_EMPTY_TRASH = "/api/emptyTrash"
    const val API_GET_PROGRESS = "/api/getProgress"
    const val API_UPLOAD_PHOTO = "/api/uploadPhoto"
    const val API_GET_ACTIVITIES = "/api/getActivities"
    const val API_CREATE_OR_UPDATE_ACTIVITY = "/api/createOrUpdateActivity"
    const val API_DELETE_ACTIVITY = "/api/deleteActivity"
    const val API_GET_FACES = "/api/getFaces"
    const val API_UPDATE_FACE = "/api/updateFace"
    const val API_MERGE_FACE = "/api/mergeFace"
    const val API_GET_PICS_DATE = "/api/getPicsDate"
    const val API_GET_FOLDERS_DATA = "/api/getFoldersData"
    const val API_GET_ALL_TAGS = "/api/getAllTags"
    const val API_GET_FACES_WITH_NAME = "/api/getFacesWithName"
    const val API_GET_FACES_FOR_PHOTO = "/api/getFacesForPhoto"
    const val API_REMOVE_PHOTO_FACE_INFO = "/api/removePhotoFaceInfo"
    const val API_RESCAN_FACE = "/api/rescanFace"
    const val API_MOVE_FOLDER = "/api/moveFolder"
    const val API_SWITCH_TRASH_FOLDER = "/api/switchTrashFolder"
    const val API_UPDATE_FOLDER_DATE = "/api/updateFolderDate"
    const val API_UPDATE_FOLDER_GIS = "/api/updateFolderGis"
    const val GEO_JSON_API_LOAD_MARKERS = "/geo_json_api/loadMarkersGeoJson"
    const val COS_PATH = "/cos/"
    const val FACE_IMG_PATH = "/get_face_img"
}
