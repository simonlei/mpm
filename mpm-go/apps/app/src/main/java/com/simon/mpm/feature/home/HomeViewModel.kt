package com.simon.mpm.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simon.mpm.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 主页面ViewModel
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    /**
     * 登出
     */
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}