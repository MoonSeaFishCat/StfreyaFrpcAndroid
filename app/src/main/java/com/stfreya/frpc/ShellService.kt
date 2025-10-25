package com.stfreya.frpc

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import java.util.Random


class ShellService : LifecycleService() {
    private val _processThreads = MutableStateFlow(mutableMapOf<FrpConfig, ShellThread>())
    val processThreads = _processThreads.asStateFlow()

    private val _logText = MutableStateFlow("")
    val logText: StateFlow<String> = _logText
    
    private val _serviceState = MutableStateFlow(ServiceState.STOPPED)
    val serviceState = _serviceState.asStateFlow()
    
    private var isServiceDestroyed = false
    
    enum class ServiceState {
        STARTING, RUNNING, STOPPING, STOPPED, ERROR
    }

    fun clearLog() {
        _logText.value = ""
    }

    // Binder given to clients
    private val binder = LocalBinder()

    // Random number generator
    private val mGenerator = Random()

    /** method for clients  */
    val randomNumber: Int
        get() = mGenerator.nextInt(100)

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
            Log.w("adx", "Service is being destroyed, ignoring start command")
            return START_NOT_STICKY
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
            Log.e("adx", "frpConfig is null")
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
                    Log.e("adx", "Error starting service", e)
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
                    Log.e("adx", "Error stopping service", e)
                    _serviceState.value = ServiceState.ERROR
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun startFrp(config: FrpConfig) {
        Log.d("adx", "start config is $config")
        val dir = config.getDir(this)
        val file = config.getFile(this)
        
        if (!file.exists()) {
            Log.w("adx", "Config file does not exist: ${file.absolutePath}")
            Toast.makeText(this, getString(R.string.file_not_exist), Toast.LENGTH_SHORT).show()
            return
        }
        
        if (_processThreads.value.contains(config)) {
            Log.w("adx", "FRP is already running for config: ${config.fileName}")
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
            Log.d("adx", "Starting FRP with command: ${commandList.joinToString(" ")}")
            Log.d("adx", "Working directory: ${dir.absolutePath}")
            
            val thread = runCommand(commandList, dir)
            _processThreads.update { it.toMutableMap().apply { put(config, thread) } }
            Log.i("adx", "Successfully started FRP for config: ${config.fileName}")
            
        } catch (e: SecurityException) {
            Log.e("adx", "Security error starting FRP", e)
            Toast.makeText(this, "Security error: ${e.message}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("adx", "Error starting FRP", e)
            Toast.makeText(this, "Error starting FRP: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopFrp(config: FrpConfig) {
        val thread = _processThreads.value.get(config)
//        thread?.interrupt()
        thread?.stopProcess()
        _processThreads.update {
            it.toMutableMap().apply { remove(config) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceDestroyed = true
        _serviceState.value = ServiceState.STOPPING
        
        if (!_processThreads.value.isEmpty()) {
            _processThreads.value.forEach { (config, thread) ->
                try {
                    thread.stopProcess()
                    Log.d("adx", "Stopped process for config: ${config.fileName}")
                } catch (e: Exception) {
                    Log.e("adx", "Error stopping process for ${config.fileName}", e)
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
                    "No configurations running"
                }
            )
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        if (runningCount > 0) {
                            "Running configurations:\n" + runningConfigs.joinToString("\n") { it.fileName }
                        } else {
                            "No configurations are currently running"
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