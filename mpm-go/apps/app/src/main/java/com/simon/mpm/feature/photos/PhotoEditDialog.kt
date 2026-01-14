package com.simon.mpm.feature.photos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.simon.mpm.network.model.Activity
import com.simon.mpm.network.model.Photo
import java.text.SimpleDateFormat
import java.util.*

/**
 * 照片编辑对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotoEditDialog(
    photo: Photo,
    activities: List<Activity>,
    allTags: List<String>,
    onDismiss: () -> Unit,
    onSave: (PhotoEditData) -> Unit
) {
    var takenDate by remember { mutableStateOf(photo.takenDate ?: "") }
    var latitude by remember { mutableStateOf(photo.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(photo.longitude?.toString() ?: "") }
    var selectedActivityId by remember { mutableStateOf(photo.activity ?: 0) }
    var tags by remember { mutableStateOf(photo.tag ?: "") }
    var showActivityMenu by remember { mutableStateOf(false) }
    var showTagMenu by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 600.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "编辑照片信息",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "关闭")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 可滚动内容
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // 拍摄日期
                    OutlinedTextField(
                        value = takenDate,
                        onValueChange = { takenDate = it },
                        label = { Text("拍摄日期") },
                        placeholder = { Text("yyyy-MM-dd'T'HH:mm:ss'Z'") },
                        leadingIcon = {
                            Icon(Icons.Default.CalendarToday, null)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 纬度
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("纬度") },
                        placeholder = { Text("例如: 39.9042") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 经度
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("经度") },
                        placeholder = { Text("例如: 116.4074") },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 活动选择
                    Box {
                        OutlinedTextField(
                            value = activities.find { it.id == selectedActivityId }?.name ?: "无",
                            onValueChange = {},
                            label = { Text("活动") },
                            leadingIcon = {
                                Icon(Icons.Default.Event, null)
                            },
                            trailingIcon = {
                                IconButton(onClick = { showActivityMenu = true }) {
                                    Icon(Icons.Default.ArrowDropDown, null)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            singleLine = true
                        )

                        DropdownMenu(
                            expanded = showActivityMenu,
                            onDismissRequest = { showActivityMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("无") },
                                onClick = {
                                    selectedActivityId = 0
                                    showActivityMenu = false
                                }
                            )
                            activities.forEach { activity ->
                                DropdownMenuItem(
                                    text = { Text(activity.name) },
                                    onClick = {
                                        selectedActivityId = activity.id
                                        showActivityMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 标签输入
                    Box {
                        OutlinedTextField(
                            value = tags,
                            onValueChange = { tags = it },
                            label = { Text("标签") },
                            placeholder = { Text("多个标签用逗号分隔") },
                            leadingIcon = {
                                Icon(Icons.Default.Label, null)
                            },
                            trailingIcon = {
                                if (allTags.isNotEmpty()) {
                                    IconButton(onClick = { showTagMenu = true }) {
                                        Icon(Icons.Default.ArrowDropDown, null)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )

                        // 标签快速选择菜单
                        DropdownMenu(
                            expanded = showTagMenu,
                            onDismissRequest = { showTagMenu = false }
                        ) {
                            allTags.forEach { tag ->
                                DropdownMenuItem(
                                    text = { Text(tag) },
                                    onClick = {
                                        // 添加标签（如果还没有）
                                        val currentTags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        if (!currentTags.contains(tag)) {
                                            tags = if (tags.isEmpty()) tag else "$tags, $tag"
                                        }
                                        showTagMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                PhotoEditData(
                                    takenDate = takenDate.ifEmpty { null },
                                    latitude = latitude.toDoubleOrNull(),
                                    longitude = longitude.toDoubleOrNull(),
                                    activity = if (selectedActivityId > 0) selectedActivityId else null,
                                    tags = tags.ifEmpty { null }
                                )
                            )
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

/**
 * 照片编辑数据
 */
data class PhotoEditData(
    val takenDate: String?,
    val latitude: Double?,
    val longitude: Double?,
    val activity: Int?,
    val tags: String?
)
