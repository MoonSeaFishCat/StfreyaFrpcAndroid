package com.stfreya.frpc

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import com.stfreya.frpc.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File


class ShellService : LifecycleService() {
    private val _processThreads = MutableStateFlow(mutableMapOf<FrpConfig, ShellThread>())
    val processThreads = _processThreads.asStateFlow()

    private val _logText = MutableStateFlow("")
    val logText: StateFlow<String> = _logText
    
    private val _serviceState = MutableStateFlow(ServiceState.STOPPED)
    val serviceState = _serviceState.asStateFlow()
    
    private var isServiceDestroyed = false
    private var processGuard: ProcessGuard? = null
    
    enum class ServiceState {
        STARTING, RUNNING, STOPPING, STOPPED, ERROR
    }

    fun clearLog() {
        _logText.value = ""
    }

    // Binder given to clients
    private val binder = LocalBinder()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder(), IBinder {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): ShellService = this@ShellService
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        
        if (isServiceDestroyed) {
            Logger.w("服务正在销毁，忽略启动命令")
            return START_NOT_STICKY
        }
        
        // 初始化进程守护
        if (processGuard == null) {
            processGuard = ProcessGuard(
                context = this,
                onProcessExit = { config ->
                    _logText.value += getString(R.string.process_restarting) + "\n"
                    Logger.i("配置 ${config.fileName} 的进程退出，准备重启")
                },
                onRestartFailed = { config, count ->
                    _logText.value += getString(R.string.process_restart_failed) + "\n"
                    Logger.e("配置 ${config.fileName} 重启失败，已达到最大重启次数: $count")
                    Toast.makeText(this, getString(R.string.process_restart_failed), Toast.LENGTH_LONG).show()
                }
            )
        }
        
        val frpConfig: ArrayList<FrpConfig>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent?.extras?.getParcelableArrayList(
                    IntentExtraKey.FrpConfig, FrpConfig::class.java
                )
            } else {
                @Suppress("DEPRECATION") intent?.extras?.getParcelableArrayList(IntentExtraKey.FrpConfig)
            }
        if (frpConfig == null) {
            Logger.e("frpConfig 为空")
            _serviceState.value = ServiceState.ERROR
            Toast.makeText(this, getString(R.string.frp_config_null), Toast.LENGTH_SHORT).show()
            return START_NOT_STICKY
        }
        
        when (intent?.action) {
            ShellServiceAction.START -> {
                _serviceState.value = ServiceState.STARTING
                try {
                    for (config in frpConfig) {
                        startFrp(config)
                    }
                    _serviceState.value = ServiceState.RUNNING
                    Toast.makeText(this, getString(R.string.service_start_toast), Toast.LENGTH_SHORT)
                        .show()
                    startForeground(1, showNotification())
                } catch (e: Exception) {
                    Logger.e("启动服务时出错", e)
                    _serviceState.value = ServiceState.ERROR
                }
            }

            ShellServiceAction.STOP -> {
                _serviceState.value = ServiceState.STOPPING
                try {
                    for (config in frpConfig) {
                        stopFrp(config)
                    }
                    startForeground(1, showNotification())
                    if (_processThreads.value.isEmpty()) {
                        _serviceState.value = ServiceState.STOPPED
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            stopForeground(STOP_FOREGROUND_REMOVE)
                        } else {
                            @Suppress("DEPRECATION") stopForeground(true)
                        }
                        stopSelf()
                        Toast.makeText(this, getString(R.string.service_stop_toast), Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        _serviceState.value = ServiceState.RUNNING
                    }
                } catch (e: Exception) {
                    Logger.e("停止服务时出错", e)
                    _serviceState.value = ServiceState.ERROR
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startFrp(config: FrpConfig) {
        Logger.d("启动配置: $config")
        val dir = config.getDir(this)
        val file = config.getFile(this)
        
        if (!file.exists()) {
            Logger.w("配置文件不存在: ${file.absolutePath}")
            Toast.makeText(this, getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (_processThreads.value.contains(config)) {
            Logger.w("配置 ${config.fileName} 已在运行")
            Toast.makeText(this, getString(R.string.frp_already_running), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val ainfo = packageManager.getApplicationInfo(
                packageName, PackageManager.GET_SHARED_LIBRARY_FILES
            )
            val commandList = listOf(
                "${ainfo.nativeLibraryDir}/${config.type.getLibName()}", 
                "-c", 
                config.fileName
            )
            Logger.d("启动FRP命令: ${commandList.joinToString(" ")}")
            Logger.d("工作目录: ${dir.absolutePath}")
            
            val thread = runCommand(commandList, dir)
            _processThreads.update { it.toMutableMap().apply { put(config, thread) } }
            
            // 启动进程守护
            processGuard?.startMonitoring(config, thread)
            processGuard?.resetRestartCount(config)
            
            Logger.i("成功启动配置: ${config.fileName}")
            _logText.value += getString(R.string.process_guard_enabled) + "\n"
            
        } catch (e: SecurityException) {
            Logger.e("启动FRP时安全错误", e)
            Toast.makeText(this, getString(R.string.security_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Logger.e("启动FRP时出错", e)
            Toast.makeText(this, getString(R.string.error_starting_frp, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun stopFrp(config: FrpConfig) {
        // 停止进程守护
        processGuard?.stopMonitoring(config)
        
        val thread = _processThreads.value.get(config)
        thread?.stopProcess()
        _processThreads.update {
            it.toMutableMap().apply { remove(config) }
        }
        Logger.i("已停止配置: ${config.fileName}")
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceDestroyed = true
        _serviceState.value = ServiceState.STOPPING
        
        // 停止所有进程守护
        processGuard?.stopAll()
        processGuard = null
        
        if (!_processThreads.value.isEmpty()) {
            _processThreads.value.forEach { (config, thread) ->
                try {
                    thread.stopProcess()
                    Logger.d("已停止配置: ${config.fileName}")
                } catch (e: Exception) {
                    Logger.e("停止配置 ${config.fileName} 时出错", e)
                }
            }
            _processThreads.update { mutableMapOf() }
        }
        
        // 清理资源
        _logText.value = ""
        _serviceState.value = ServiceState.STOPPED
    }

    private fun runCommand(command: List<String>, dir: File): ShellThread {
        val process_thread = ShellThread(command, dir) { _logText.value += it + "\n" }
        process_thread.start()
        return process_thread;
    }

    private fun showNotification(): Notification {
        val pendingIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
            }
        
        val runningCount = _processThreads.value.size
        val runningConfigs = _processThreads.value.keys
        
        val notification = NotificationCompat.Builder(this, "shell_bg")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(getString(R.string.frp_notification_title))
            .setContentText(
                if (runningCount > 0) {
                    getString(R.string.frp_notification_content, runningCount)
                } else {
                    getString(R.string.no_configs_running)
                }
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        if (runningCount > 0) {
                            getString(R.string.running_configs, runningConfigs.joinToString("\n") { it.fileName })
                        } else {
                            getString(R.string.no_configs_currently_running)
                        }
                    )
            )
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return notification.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        } else {
            return notification.build()
        }
    }
}