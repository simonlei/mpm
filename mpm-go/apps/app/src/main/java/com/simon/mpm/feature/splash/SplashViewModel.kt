package com.simon.mpm.feature.splash

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.data.datastore.PreferencesManager
import com.simon.mpm.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 启动页ViewModel
 * 检查登录状态并决定导航目标
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        viewModelScope.launch {
            // 验证和修复服务器URL
            validateAndFixServerUrl()
            
            // 显示启动画面至少1秒
            delay(1000)
            
            // 检查是否已登录
            val isLoggedIn = authRepository.isLoggedIn().first()
            
            if (isLoggedIn) {
                // 已登录，验证认证信息是否有效
                _uiState.value = SplashUiState.NavigateToHome
            } else {
                // 未登录，跳转到登录页
                _uiState.value = SplashUiState.NavigateToLogin
            }
        }
    }
    
    /**
     * 验证和修复服务器URL
     */
    private suspend fun validateAndFixServerUrl() {
        try {
            val currentUrl = preferencesManager.serverUrl.first()
            Log.d("SplashViewModel", "Current server URL: $currentUrl")
            
            // 验证URL格式
            val isValid = currentUrl.isNotBlank() && 
                         (currentUrl.startsWith("http://") || currentUrl.startsWith("https://")) &&
                         currentUrl.length > 10
            
            if (!isValid) {
                // URL格式不正确，重置为默认值
                val defaultUrl = "http://10.0.2.2:8080"
                Log.w("SplashViewModel", "Invalid URL detected: $currentUrl, resetting to: $defaultUrl")
                preferencesManager.setServerUrl(defaultUrl)
            }
        } catch (e: Exception) {
            Log.e("SplashViewModel", "Error validating server URL", e)
            // 发生错误时，重置为默认值
            preferencesManager.setServerUrl("http://10.0.2.2:8080")
        }
    }
}

/**
 * 启动页UI状态
 */
sealed class SplashUiState {
    data object Loading : SplashUiState()
    data object NavigateToLogin : SplashUiState()
    data object NavigateToHome : SplashUiState()
}