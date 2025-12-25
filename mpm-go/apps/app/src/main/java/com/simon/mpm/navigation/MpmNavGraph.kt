package com.simon.mpm.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.simon.mpm.network.model.Photo
import com.simon.mpm.feature.auth.LoginScreen
import com.simon.mpm.feature.home.HomeScreen
import com.simon.mpm.feature.photos.PhotoDetailScreen
import com.simon.mpm.feature.photos.PhotoListScreen
import com.simon.mpm.feature.splash.SplashScreen
import com.simon.mpm.feature.trash.TrashScreen

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
                    navController.navigate(Routes.PHOTO_LIST) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        // 登录页
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.PHOTO_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        
        // 主页
        composable(Routes.HOME) {
            HomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onNavigateToPhotos = {
                    navController.navigate(Routes.PHOTO_LIST)
                },
                onNavigateToTrash = {
                    navController.navigate(Routes.trash())
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
        ) {
            TrashScreen(
                onBack = { navController.popBackStack() },
                onPhotoClick = { photo ->
                    navController.navigate(Routes.photoDetail(photo.id))
                }
            )
        }
        
        // 照片列表（主屏幕）
        composable(Routes.PHOTO_LIST) {
            PhotoListScreen(
                onPhotoClick = { photo ->
                    navController.navigate(Routes.photoDetail(photo.id))
                },
                onNavigateToTrash = {
                    navController.navigate(Routes.trash())
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PHOTO_LIST) { inclusive = true }
                    }
                }
            )
        }
        
        // 照片详情
        composable(
            route = Routes.PHOTO_DETAIL,
            arguments = listOf(
                navArgument("photoId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val photoId = backStackEntry.arguments?.getInt("photoId") ?: 0
            PhotoDetailScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}