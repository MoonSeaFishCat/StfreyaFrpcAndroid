package com.stfreya.frpc.data

import android.content.Context
import com.stfreya.frpc.FrpConfig
import com.stfreya.frpc.FrpType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class ConfigRepository(private val context: Context) {
    
    private val configCache = ConcurrentHashMap<String, FrpConfig>()
    private val fileCache = ConcurrentHashMap<String, File>()
    
    suspend fun getAllConfigs(): List<FrpConfig> = withContext(Dispatchers.IO) {
        FrpType.FRPC.getDir(context).listFiles()
            ?.filter { it.isFile && it.name.endsWith(".toml") }
            ?.map { file ->
                val config = FrpConfig(FrpType.FRPC, file.name)
                configCache[file.absolutePath] = config
                fileCache[file.absolutePath] = file
                config
            } ?: emptyList()
    }
    
    suspend fun getConfigFile(config: FrpConfig): File = withContext(Dispatchers.IO) {
        val file = config.getFile(context)
        fileCache[file.absolutePath] = file
        file
    }
    
    suspend fun saveConfig(config: FrpConfig, content: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = config.getFile(context)
            file.writeText(content)
            fileCache[file.absolutePath] = file
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteConfig(config: FrpConfig): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = config.getFile(context)
            if (file.exists()) {
                file.delete()
            }
            configCache.remove(file.absolutePath)
            fileCache.remove(file.absolutePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun renameConfig(config: FrpConfig, newName: String): Result<FrpConfig> = withContext(Dispatchers.IO) {
        try {
            val oldFile = config.getFile(context)
            val newFile = File(oldFile.parent, newName)
            
            if (oldFile.exists()) {
                oldFile.renameTo(newFile)
            }
            
            val newConfig = FrpConfig(config.type, newName)
            configCache.remove(oldFile.absolutePath)
            fileCache.remove(oldFile.absolutePath)
            configCache[newFile.absolutePath] = newConfig
            fileCache[newFile.absolutePath] = newFile
            
            Result.success(newConfig)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun createConfig(type: FrpType, fileName: String, content: String): Result<FrpConfig> = withContext(Dispatchers.IO) {
        try {
            val config = FrpConfig(type, fileName)
            val file = config.getFile(context)
            
            // 确保目录存在
            file.parentFile?.mkdirs()
            
            file.writeText(content)
            configCache[file.absolutePath] = config
            fileCache[file.absolutePath] = file
            
            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun getCachedConfig(filePath: String): FrpConfig? = configCache[filePath]
    
    fun getCachedFile(filePath: String): File? = fileCache[filePath]
    
    fun clearCache() {
        configCache.clear()
        fileCache.clear()
    }
    
    suspend fun getConfigSize(config: FrpConfig): Long = withContext(Dispatchers.IO) {
        try {
            val file = config.getFile(context)
            if (file.exists()) file.length() else 0L
        } catch (e: Exception) {
            0L
        }
    }
    
    suspend fun getConfigLastModified(config: FrpConfig): Long = withContext(Dispatchers.IO) {
        try {
            val file = config.getFile(context)
            if (file.exists()) file.lastModified() else 0L
        } catch (e: Exception) {
            0L
        }
    }
}
