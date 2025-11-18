package com.stfreya.frpc

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.stfreya.frpc.utils.Logger
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

/**
 * FRP进程守护机制
 * 监控FRP进程状态，如果进程异常退出则自动重启
 */
class ProcessGuard(
    private val context: Context,
    private val onProcessExit: (FrpConfig) -> Unit,
    private val onRestartFailed: (FrpConfig, Int) -> Unit
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val monitoringJobs = ConcurrentHashMap<FrpConfig, Job>()
    private val restartCounts = ConcurrentHashMap<FrpConfig, Int>()
    private val maxRestartAttempts = 5 // 最大重启次数
    private val restartDelay = 3000L // 重启延迟（毫秒）
    private val checkInterval = 2000L // 检查间隔（毫秒）
    
    /**
     * 开始监控指定配置的进程
     */
    fun startMonitoring(config: FrpConfig, shellThread: ShellThread) {
        if (monitoringJobs.containsKey(config)) {
            Logger.w("配置 ${config.fileName} 已在监控中")
            return
        }
        
        restartCounts[config] = 0
        val job = scope.launch {
            monitorProcess(config, shellThread)
        }
        monitoringJobs[config] = job
        Logger.i("开始监控配置: ${config.fileName}")
    }
    
    /**
     * 停止监控指定配置
     */
    fun stopMonitoring(config: FrpConfig) {
        monitoringJobs[config]?.cancel()
        monitoringJobs.remove(config)
        restartCounts.remove(config)
        Logger.i("停止监控配置: ${config.fileName}")
    }
    
    /**
     * 停止所有监控
     */
    fun stopAll() {
        monitoringJobs.values.forEach { it.cancel() }
        monitoringJobs.clear()
        restartCounts.clear()
        Logger.i("停止所有进程监控")
    }
    
    /**
     * 监控进程状态
     */
    private suspend fun monitorProcess(config: FrpConfig, shellThread: ShellThread) {
        while (isActive) {
            delay(checkInterval)
            
            // 检查线程是否存活
            if (!shellThread.isAlive) {
                Logger.w("检测到配置 ${config.fileName} 的进程已退出")
                
                // 检查是否超过最大重启次数
                val restartCount = restartCounts[config] ?: 0
                if (restartCount >= maxRestartAttempts) {
                    Logger.e("配置 ${config.fileName} 已达到最大重启次数 ($maxRestartAttempts)，停止自动重启")
                    onRestartFailed(config, restartCount)
                    stopMonitoring(config)
                    return
                }
                
                // 延迟后重启
                delay(restartDelay)
                if (isActive) {
                    restartProcess(config, restartCount + 1)
                }
            }
        }
    }
    
    /**
     * 重启进程
     */
    private suspend fun restartProcess(config: FrpConfig, newRestartCount: Int) {
        withContext(Dispatchers.Main) {
            try {
                Logger.i("正在重启配置: ${config.fileName} (第 $newRestartCount 次)")
                restartCounts[config] = newRestartCount
                
                // 通知进程退出
                onProcessExit(config)
                
                // 延迟后重新启动
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(context, ShellService::class.java)
                    intent.action = ShellServiceAction.START
                    intent.putExtra(IntentExtraKey.FrpConfig, arrayListOf(config))
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                }, 1000)
                
            } catch (e: Exception) {
                Logger.e("重启配置 ${config.fileName} 时出错", e)
                onRestartFailed(config, newRestartCount)
            }
        }
    }
    
    /**
     * 获取配置的重启次数
     */
    fun getRestartCount(config: FrpConfig): Int {
        return restartCounts[config] ?: 0
    }
    
    /**
     * 重置重启计数
     */
    fun resetRestartCount(config: FrpConfig) {
        restartCounts[config] = 0
    }
    
    /**
     * 清理资源
     */
    fun cleanup() {
        stopAll()
        scope.cancel()
    }
}

