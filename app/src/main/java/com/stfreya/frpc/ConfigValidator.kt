package com.stfreya.frpc

import com.stfreya.frpc.utils.Logger
import java.io.File

object ConfigValidator {
    
    data class ValidationResult(
        val isValid: Boolean,
        val errors: List<String> = emptyList(),
        val warnings: List<String> = emptyList()
    )
    
    fun validateConfig(config: FrpConfig, configFile: File): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        try {
            if (!configFile.exists()) {
                errors.add("Configuration file does not exist")
                return ValidationResult(false, errors)
            }
            
            val content = configFile.readText()
            if (content.isBlank()) {
                errors.add("Configuration file is empty")
                return ValidationResult(false, errors)
            }
            
            // 基本TOML格式检查
            validateTomlFormat(content, errors, warnings)
            
            // FRP特定配置检查
            validateFrpcConfig(content, errors, warnings)
            
        } catch (e: Exception) {
            Logger.e("验证配置时出错", e)
            errors.add("Error reading configuration file: ${e.message}")
        }
        
        return ValidationResult(errors.isEmpty(), errors, warnings)
    }
    
    private fun validateTomlFormat(content: String, errors: MutableList<String>, warnings: MutableList<String>) {
        val lines = content.lines()
        
        // 检查是否有基本的TOML结构
        val hasSections = lines.any { it.trim().startsWith("[") && it.trim().endsWith("]") }
        if (!hasSections) {
            warnings.add("No TOML sections found - configuration may be invalid")
        }
        
        // 检查常见的TOML语法错误
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isNotEmpty() && !trimmedLine.startsWith("#")) {
                // 检查未闭合的引号
                if (trimmedLine.count { it == '"' } % 2 != 0) {
                    errors.add("Unclosed quotes in line ${index + 1}")
                }
                
                // 检查等号使用
                if (trimmedLine.contains("=") && !trimmedLine.contains(" = ")) {
                    warnings.add("Consider using spaces around '=' in line ${index + 1}")
                }
            }
        }
    }
    
    private fun validateFrpcConfig(content: String, errors: MutableList<String>, warnings: MutableList<String>) {
        // 检查必需的FRPC配置项
        val requiredSections = listOf("serverAddr", "serverPort")
        val missingRequired = requiredSections.filter { !content.contains(it) }
        
        if (missingRequired.isNotEmpty()) {
            errors.add("Missing required FRPC configuration: ${missingRequired.joinToString(", ")}")
        }
        
        // 检查服务器地址格式
        val serverAddrMatch = Regex("serverAddr\\s*=\\s*\"([^\"]+)\"").find(content)
        if (serverAddrMatch != null) {
            val serverAddr = serverAddrMatch.groupValues[1]
            if (serverAddr.isBlank()) {
                errors.add("Server address cannot be empty")
            } else if (!isValidHost(serverAddr)) {
                warnings.add("Server address format may be invalid: $serverAddr")
            }
        }
        
        // 检查端口号
        val portMatch = Regex("serverPort\\s*=\\s*(\\d+)").find(content)
        if (portMatch != null) {
            val port = portMatch.groupValues[1].toIntOrNull()
            if (port == null || port <= 0 || port > 65535) {
                errors.add("Invalid server port: $port")
            }
        }
        
        // 检查代理配置
        val proxySections = content.split("[[proxies]]").size - 1
        if (proxySections == 0) {
            warnings.add("No proxy configurations found - FRPC may not be useful without proxies")
        }
    }
    
    private fun isValidHost(host: String): Boolean {
        return try {
            // 简单的IP地址或域名验证
            host.matches(Regex("^[a-zA-Z0-9.-]+$")) && host.length <= 253
        } catch (e: Exception) {
            false
        }
    }
    
    fun getConfigSummary(config: FrpConfig, configFile: File): String {
        return try {
            if (!configFile.exists()) {
                "Configuration file not found"
            } else {
                val content = configFile.readText()
                val lines = content.lines().filter { it.trim().isNotEmpty() && !it.trim().startsWith("#") }
                "${config.type.typeName.uppercase()} configuration with ${lines.size} settings"
            }
        } catch (e: Exception) {
            "Error reading configuration"
        }
    }
}
