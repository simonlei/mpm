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
                    navController.navigate(Routes.TRASH)
                }
            )
        }
        
        // 回收站
        composable(Routes.TRASH) {
            TrashScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // 照片列表
        composable(Routes.PHOTO_LIST) {
            PhotoListScreen(
                onPhotoClick = { photo ->
                    navController.navigate(Routes.photoDetail(photo.id))
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