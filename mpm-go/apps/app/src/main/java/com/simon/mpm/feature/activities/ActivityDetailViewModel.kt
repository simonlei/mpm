package com.simon.mpm.feature.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.ActivityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 活动详情ViewModel
 */
@HiltViewModel
class ActivityDetailViewModel @Inject constructor(
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityDetailUiState())
    val uiState: StateFlow<ActivityDetailUiState> = _uiState.asStateFlow()

    /**
     * 加载活动数据
     */
    fun loadActivity(activityId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, activityId = activityId) }
            
            activityRepository.getActivities().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val activity = result.data.find { it.id == activityId }
                        if (activity != null) {
                            _uiState.update {
                                it.copy(
                                    activityId = activityId,
                                    name = activity.name,
                                    description = activity.description ?: "",
                                    startDate = activity.startDate ?: "",
                                    endDate = activity.endDate ?: "",
                                    latitude = activity.latitude?.toString() ?: "",
                                    longitude = activity.longitude?.toString() ?: "",
                                    isLoading = false,
                                    error = null
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "活动不存在"
                                )
                            }
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                    is Result.Loading -> {
                        // 已经设置了loading状态
                    }
                }
            }
        }
    }

    /**
     * 保存活动
     */
    fun saveActivity() {
        val state = _uiState.value
        
        // 验证
        if (state.name.isBlank()) {
            _uiState.update { it.copy(showValidation = true) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            
            val latitude = state.latitude.toDoubleOrNull()
            val longitude = state.longitude.toDoubleOrNull()
            
            val flow = if (state.activityId == 0) {
                // 创建
                activityRepository.createActivity(
                    name = state.name,
                    description = state.description.ifBlank { null },
                    startDate = state.startDate.ifBlank { null },
                    endDate = state.endDate.ifBlank { null },
                    latitude = latitude,
                    longitude = longitude
                )
            } else {
                // 更新
                activityRepository.updateActivity(
                    id = state.activityId,
                    name = state.name,
                    description = state.description.ifBlank { null },
                    startDate = state.startDate.ifBlank { null },
                    endDate = state.endDate.ifBlank { null },
                    latitude = latitude,
                    longitude = longitude
                )
            }
            
            flow.collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                error = result.message
                            )
                        }
                    }
                    is Result.Loading -> {
                        // 已经设置了saving状态
                    }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateStartDate(startDate: String) {
        _uiState.update { it.copy(startDate = startDate) }
    }

    fun updateEndDate(endDate: String) {
        _uiState.update { it.copy(endDate = endDate) }
    }

    fun updateLatitude(latitude: String) {
        _uiState.update { it.copy(latitude = latitude) }
    }

    fun updateLongitude(longitude: String) {
        _uiState.update { it.copy(longitude = longitude) }
    }
}

/**
 * 活动详情UI状态
 */
data class ActivityDetailUiState(
    val activityId: Int = 0,
    val name: String = "",
    val description: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val showValidation: Boolean = false,
    val error: String? = null
)
