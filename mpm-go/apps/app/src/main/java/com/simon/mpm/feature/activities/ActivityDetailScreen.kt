package com.simon.mpm.feature.activities

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 活动详情/编辑页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityDetailScreen(
    activityId: Int,
    onBack: () -> Unit,
    viewModel: ActivityDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // 加载活动数据
    LaunchedEffect(activityId) {
        if (activityId > 0) {
            viewModel.loadActivity(activityId)
        }
    }
    
    // 保存成功后返回
    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            onBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (activityId == 0) "创建活动" else "编辑活动") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.saveActivity() },
                        enabled = !uiState.isSaving && uiState.name.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 活动名称
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = { viewModel.updateName(it) },
                    label = { Text("活动名称 *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = uiState.name.isBlank() && uiState.showValidation
                )
                
                // 描述
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
                
                // 开始日期
                OutlinedTextField(
                    value = uiState.startDate,
                    onValueChange = { viewModel.updateStartDate(it) },
                    label = { Text("开始日期 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如: 2024-01-01") }
                )
                
                // 结束日期
                OutlinedTextField(
                    value = uiState.endDate,
                    onValueChange = { viewModel.updateEndDate(it) },
                    label = { Text("结束日期 (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("例如: 2024-01-31") }
                )
                
                // 纬度
                OutlinedTextField(
                    value = uiState.latitude,
                    onValueChange = { viewModel.updateLatitude(it) },
                    label = { Text("纬度") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("例如: 39.9042") }
                )
                
                // 经度
                OutlinedTextField(
                    value = uiState.longitude,
                    onValueChange = { viewModel.updateLongitude(it) },
                    label = { Text("经度") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("例如: 116.4074") }
                )
                
                // 错误提示
                if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                // 提示信息
                Text(
                    text = "* 必填项",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 加载指示器
            if (uiState.isLoading || uiState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .padding(8.dp)
                )
            }
        }
    }
}
