package com.simon.mpm.feature.albums

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 相册页面（占位）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("相册") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "相册视图",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "功能开发中...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
