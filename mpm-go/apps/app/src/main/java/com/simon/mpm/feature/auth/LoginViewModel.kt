package com.simon.mpm.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.common.Result
import com.simon.mpm.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录页面ViewModel
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    // UI状态
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()
    
    init {
        // 加载服务器地址
        loadServerUrl()
    }
    
    /**
     * 加载服务器地址
     */
    private fun loadServerUrl() {
        viewModelScope.launch {
            val serverUrl = authRepository.getServerUrl().first()
            _uiState.value = _uiState.value.copy(serverUrl = serverUrl)
        }
    }
    
    /**
     * 更新服务器地址
     */
    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url)
    }
    
    /**
     * 更新账号
     */
    fun updateAccount(account: String) {
        _uiState.value = _uiState.value.copy(account = account)
    }
    
    /**
     * 更新密码
     */
    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }
    
    /**
     * 执行登录
     */
    fun login() {
        val state = _uiState.value
        
        // 验证输入
        if (state.serverUrl.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入服务器地址")
            return
        }
        
        if (state.account.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入账号")
            return
        }
        
        if (state.password.isBlank()) {
            _uiState.value = state.copy(errorMessage = "请输入密码")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = null)
            
            // 保存服务器地址
            authRepository.setServerUrl(state.serverUrl)
            
            // 执行登录
            when (val result = authRepository.login(state.account, state.password)) {
                is Result.Success -> {
                    _uiState.value = state.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        errorMessage = null
                    )
                }
                is Result.Error -> {
                    _uiState.value = state.copy(
                        isLoading = false,
                        errorMessage = result.message ?: "登录失败"
                    )
                }
                is Result.Loading -> {
                    // 不应该到这里
                }
            }
        }
    }
    
    /**
     * 清除错误消息
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * 登录页面UI状态
 */
data class LoginUiState(
    val serverUrl: String = "",
    val account: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val errorMessage: String? = null
)