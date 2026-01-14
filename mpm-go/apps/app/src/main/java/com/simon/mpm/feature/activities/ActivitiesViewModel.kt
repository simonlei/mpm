package com.simon.mpm.feature.activities

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.ActivityRepository
import com.simon.mpm.network.model.Activity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 活动列表ViewModel
 */
@HiltViewModel
class ActivitiesViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivitiesUiState())
    val uiState: StateFlow<ActivitiesUiState> = _uiState.asStateFlow()

    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadActivities()
    }

    /**
     * 加载活动列表
     */
    fun loadActivities() {
        Log.d(TAG, "[loadActivities] 开始加载活动列表")
        
        // 取消之前的加载任务
        if (loadJob != null) {
            Log.d(TAG, "[loadActivities] 取消之前的加载任务")
            loadJob?.cancel()
        }
        
        loadJob = viewModelScope.launch {
            Log.d(TAG, "[loadActivities] 协程已启动")
            _uiState.update { it.copy(isLoading = true, error = null) }
            Log.d(TAG, "[loadActivities] 设置 isLoading = true")
            
            Log.d(TAG, "[loadActivities] 调用 repository.getActivities()")
            activityRepository.getActivities().collect { result ->
                Log.d(TAG, "[loadActivities] 收到结果: ${result::class.simpleName}")
                when (result) {
                    is Result.Loading -> {
                        Log.d(TAG, "[loadActivities] Result.Loading")
                    }
                    is Result.Success -> {
                        Log.d(TAG, "[loadActivities] Result.Success, 活动数量: ${result.data.size}")
                        _uiState.update {
                            it.copy(
                                activities = result.data,
                                isLoading = false,
                                error = null
                            )
                        }
                        Log.d(TAG, "[loadActivities] UI状态已更新")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[loadActivities] Result.Error: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除活动
     */
    fun deleteActivity(activityId: Int) {
        Log.d(TAG, "[deleteActivity] 开始删除活动, ID: $activityId")
        viewModelScope.launch {
            Log.d(TAG, "[deleteActivity] 协程已启动")
            Log.d(TAG, "[deleteActivity] 调用 repository.deleteActivity($activityId)")
            activityRepository.deleteActivity(activityId).collect { result ->
                Log.d(TAG, "[deleteActivity] 收到结果: ${result::class.simpleName}")
                when (result) {
                    is Result.Loading -> {
                        Log.d(TAG, "[deleteActivity] Result.Loading")
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Result.Success -> {
                        Log.d(TAG, "[deleteActivity] Result.Success, 删除成功")
                        Log.d(TAG, "[deleteActivity] 准备调用 loadActivities() 刷新列表")
                        // 删除成功，重新加载列表
                        loadActivities()
                        Log.d(TAG, "[deleteActivity] loadActivities() 已调用")
                    }
                    is Result.Error -> {
                        Log.e(TAG, "[deleteActivity] Result.Error: ${result.message}")
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * 活动列表UI状态
 */
data class ActivitiesUiState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

private const val TAG = "ActivitiesViewModel"
