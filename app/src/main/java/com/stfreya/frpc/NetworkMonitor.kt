package com.stfreya.frpc

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(private val context: Context) : DefaultLifecycleObserver {
    
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private val _connectionType = MutableStateFlow(ConnectionType.UNKNOWN)
    val connectionType: StateFlow<ConnectionType> = _connectionType.asStateFlow()
    
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateConnectionStatus()
        }
        
        override fun onLost(network: Network) {
            updateConnectionStatus()
        }
        
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateConnectionStatus()
        }
    }
    
    enum class ConnectionType {
        WIFI, MOBILE, ETHERNET, VPN, UNKNOWN
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        registerNetworkCallback()
        updateConnectionStatus()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        unregisterNetworkCallback()
    }
    
    private fun registerNetworkCallback() {
        try {
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        } catch (e: Exception) {
            // 权限不足或其他错误
            _isConnected.value = true // 假设有网络连接
        }
    }
    
    private fun unregisterNetworkCallback() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // 忽略注销错误
        }
    }
    
    private fun updateConnectionStatus() {
        try {
            val activeNetwork = connectivityManager.activeNetwork
            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            
            val isConnected = networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            _isConnected.value = isConnected
            
            if (isConnected) {
                _connectionType.value = when {
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> ConnectionType.WIFI
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> ConnectionType.MOBILE
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> ConnectionType.ETHERNET
                    networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> ConnectionType.VPN
                    else -> ConnectionType.UNKNOWN
                }
            } else {
                _connectionType.value = ConnectionType.UNKNOWN
            }
        } catch (e: Exception) {
            _isConnected.value = false
            _connectionType.value = ConnectionType.UNKNOWN
        }
    }
    
    fun isNetworkAvailable(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val activeNetwork = connectivityManager.activeNetwork
                val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
                networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            } else {
                @Suppress("DEPRECATION")
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo?.isConnectedOrConnecting == true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    fun getConnectionTypeString(): String {
        return when (_connectionType.value) {
            ConnectionType.WIFI -> "WiFi"
            ConnectionType.MOBILE -> "Mobile Data"
            ConnectionType.ETHERNET -> "Ethernet"
            ConnectionType.VPN -> "VPN"
            ConnectionType.UNKNOWN -> "Unknown"
        }
    }
}
