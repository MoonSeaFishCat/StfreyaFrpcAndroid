package com.stfreya.frpc

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _isStartup = MutableStateFlow(false)
    val isStartup: StateFlow<Boolean> = _isStartup.asStateFlow()
    
    private val _logText = MutableStateFlow("")
    val logText: StateFlow<String> = _logText.asStateFlow()
    
    private val _frpcConfigList = MutableStateFlow<List<FrpConfig>>(emptyList())
    val frpcConfigList: StateFlow<List<FrpConfig>> = _frpcConfigList.asStateFlow()
    
    private val _runningConfigList = MutableStateFlow<List<FrpConfig>>(emptyList())
    val runningConfigList: StateFlow<List<FrpConfig>> = _runningConfigList.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private lateinit var preferences: SharedPreferences
    
    init {
        preferences = getApplication<Application>().getSharedPreferences("data", 0)
        _isStartup.value = preferences.getBoolean(PreferencesKey.AUTO_START, false)
        updateConfigList()
    }
    
    fun updateConfigList() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val context = getApplication<Application>()
                _frpcConfigList.value = (FrpType.FRPC.getDir(context).list()?.toList() ?: listOf()).map {
                    FrpConfig(FrpType.FRPC, it)
                }
                
                // 清理无效的自启动配置
                cleanupAutoStartConfigs()
            } catch (e: Exception) {
                _errorMessage.value = "Error updating config list: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateRunningConfigs(runningConfigs: List<FrpConfig>) {
        _runningConfigList.value = runningConfigs
    }
    
    fun updateLogText(logText: String) {
        _logText.value = logText
    }
    
    fun clearLog() {
        _logText.value = ""
    }
    
    fun setStartup(enabled: Boolean) {
        _isStartup.value = enabled
        preferences.edit().putBoolean(PreferencesKey.AUTO_START, enabled).apply()
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    private fun cleanupAutoStartConfigs() {
        val frpcAutoStartList = preferences.getStringSet(PreferencesKey.AUTO_START_FRPC_LIST, emptySet())?.filter {
            _frpcConfigList.value.any { config -> config.fileName == it }
        }
        
        preferences.edit().apply {
            putStringSet(PreferencesKey.AUTO_START_FRPC_LIST, frpcAutoStartList?.toSet())
            apply()
        }
    }
    
    fun deleteConfig(config: FrpConfig) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val file = config.getFile(context)
                if (file.exists()) {
                    file.delete()
                }
                updateConfigList()
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting config: ${e.message}"
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        // 清理资源
        _logText.value = ""
        _errorMessage.value = null
    }
}
