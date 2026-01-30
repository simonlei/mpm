package com.simon.mpm.feature.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.simon.mpm.feature.activities.ActivitiesScreen
import com.simon.mpm.feature.activities.ActivityDetailScreen
import com.simon.mpm.feature.albums.AlbumsScreen
import com.simon.mpm.feature.locations.LocationsScreen
import com.simon.mpm.feature.people.PeopleScreen
import com.simon.mpm.feature.photos.PhotoListScreen
import com.simon.mpm.feature.settings.SettingsScreen
import com.simon.mpm.feature.timeline.TimelineScreen
import com.simon.mpm.feature.upload.UploadScreen
import com.simon.mpm.navigation.BottomNavItem
import com.simon.mpm.navigation.Routes

/**
 * 主页面 - 包含底部导航栏的主容器
 */
@Composable
fun HomeScreen(
    onLogout: () -> Unit,
    onNavigateToPhotoDetail: (Int) -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToActivityDetail: (Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    Scaffold(
        bottomBar = {
            // 所有页面都显示底部导航栏
            BottomNavigationBar(navController = navController)
        }
    ) { paddingValues ->
        HomeNavGraph(
            navController = navController,
            navPaddingValues = paddingValues,
            onLogout = onLogout,
            onNavigateToPhotoDetail = onNavigateToPhotoDetail,
            onNavigateToTrash = onNavigateToTrash,
            onNavigateToActivityDetail = onNavigateToActivityDetail,
            onNavigateToSettings = {
                navController.navigate(Routes.SETTINGS) {
                    launchSingleTop = true
                }
            },
            homeViewModel = viewModel
        )
    }
}

/**
 * 底部导航栏
 */
@Composable
private fun BottomNavigationBar(
    navController: NavHostController
) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        
        BottomNavItem.items.forEach { item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        // 避免重复导航到同一目的地
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // 避免多个相同目的地的副本
                        launchSingleTop = true
                        // 恢复状态
                        restoreState = true
                    }
                }
            )
        }
    }
}

/**
 * 主页面导航图
 */
@Composable
private fun HomeNavGraph(
    navController: NavHostController,
    navPaddingValues: PaddingValues,
    onLogout: () -> Unit,
    onNavigateToPhotoDetail: (Int) -> Unit,
    onNavigateToTrash: () -> Unit,
    onNavigateToActivityDetail: (Int) -> Unit,
    onNavigateToSettings: () -> Unit,
    homeViewModel: HomeViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.ALL_PHOTOS,
        modifier = Modifier.padding(navPaddingValues)
    ) {
        // 全部照片页面
        composable(Routes.ALL_PHOTOS) {
            PhotoListScreen(
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id)
                },
                onNavigateToTrash = onNavigateToTrash,
                onNavigateToSettings = onNavigateToSettings,
                onLogout = {
                    homeViewModel.logout()
                    onLogout()
                }
            )
        }
        
        // 相册页面
        composable(Routes.ALBUMS) {
            AlbumsScreen(
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id)
                }
            )
        }
        
        // 时间轴页面
        composable(Routes.TIMELINE) {
            TimelineScreen(
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id)
                }
            )
        }
        
        // 位置页面
        composable(Routes.LOCATIONS) {
            LocationsScreen(
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id)
                }
            )
        }
        
        // 人脸页面
        composable(Routes.PEOPLE) {
            PeopleScreen(
                onPhotoClick = { photo ->
                    onNavigateToPhotoDetail(photo.id)
                }
            )
        }
        
        // 设置页面
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onLogout = onLogout,
                onNavigateToActivities = {
                    navController.navigate(Routes.ACTIVITIES) {
                        launchSingleTop = true
                    }
                },
                onNavigateToUpload = {
                    navController.navigate(Routes.UPLOAD) {
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // 保留旧路由用于兼容（从设置页跳转）
        composable(Routes.ACTIVITIES) {
            ActivitiesScreen(
                onActivityClick = { activityId ->
                    onNavigateToActivityDetail(activityId)
                }
            )
        }
        
        composable(Routes.UPLOAD) {
            UploadScreen()
        }
    }
}