package com.simon.mpm.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.simon.mpm.feature.activities.ActivityDetailScreen
import com.simon.mpm.feature.auth.LoginScreen
import com.simon.mpm.feature.home.HomeScreen
import com.simon.mpm.feature.photos.PhotoDetailScreen
import com.simon.mpm.feature.trash.TrashScreen
import com.simon.mpm.feature.splash.SplashScreen

/**
 * 应用导航图
 */
@Composable
fun MpmNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.SPLASH
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // 启动页
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        // 登录页
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // 主页（包含底部导航）
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToPhotoDetail = { photoId ->
                    navController.navigate(Routes.photoDetail(photoId))
                },
                onNavigateToTrash = {
                    navController.navigate(Routes.trash())
                },
                onNavigateToActivityDetail = { activityId ->
                    navController.navigate(Routes.activityDetail(activityId))
                }
            )
        }
        
        // 回收站
        composable(
            route = Routes.TRASH,
            arguments = listOf(
                navArgument("trashed") { 
                    type = NavType.BoolType
                    defaultValue = true
                }
            )
        ) { backStackEntry ->
            TrashScreen(
                onBack = { 
                    // 返回到照片列表时也要刷新
                    navController.previousBackStackEntry?.savedStateHandle?.set("refresh_list", true)
                    navController.popBackStack()
                },
                onPhotoClick = { photoId ->
                    navController.navigate(Routes.photoDetail(photoId, fromTrash = true))
                }
            )
        }

        // 活动详情
        composable(
            route = Routes.ACTIVITY_DETAIL,
            arguments = listOf(
                navArgument("activityId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val activityId = backStackEntry.arguments?.getInt("activityId") ?: 0
            
            ActivityDetailScreen(
                activityId = activityId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        // 照片详情
        composable(
            route = Routes.PHOTO_DETAIL,
            arguments = listOf(
                navArgument("photo_id") { type = NavType.IntType },
                navArgument("fromTrash") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getInt("photo_id") ?: 0
            val fromTrash = backStackEntry.arguments?.getBoolean("fromTrash") ?: false
            
            PhotoDetailScreen(
                onBack = {
                    // 返回时通知上一个页面刷新
                    val previousEntry = navController.previousBackStackEntry
                    if (fromTrash) {
                        // 从回收站详情返回，刷新回收站列表
                        previousEntry?.savedStateHandle?.set("refresh_trash", true)
                    } else {
                        // 从正常详情返回，刷新照片列表
                        previousEntry?.savedStateHandle?.set("refresh_list", true)
                    }
                    navController.popBackStack()
                }
            )
        }
    }
}