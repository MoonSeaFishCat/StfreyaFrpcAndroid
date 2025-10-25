package com.stfreya.frpc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stfreya.frpc.R

@Composable
fun ConfigHelpDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.config_help_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 基本介绍
                HelpSection(
                    title = "什么是FRP？",
                    content = "FRP是一个高性能的反向代理应用，支持TCP、UDP、HTTP、HTTPS等协议。它可以帮助您将内网服务暴露到公网，实现内网穿透。",
                    icon = Icons.Default.Info
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 使用场景
                HelpSection(
                    title = "常用场景",
                    content = "",
                    icon = Icons.Default.List
                ) {
                    ServiceType.values().forEach { type ->
                        ServiceHelpItem(
                            type = type,
                            description = when (type) {
                                ServiceType.HTTP -> "将内网网站服务暴露到公网，让外网用户可以访问您的网站"
                                ServiceType.SSH -> "通过公网SSH连接到内网设备，进行远程管理"
                                ServiceType.RDP -> "通过公网远程桌面连接到内网Windows设备"
                                ServiceType.TCP -> "其他TCP服务的内网穿透"
                                ServiceType.UDP -> "UDP服务的内网穿透"
                                ServiceType.HTTPS -> "HTTPS网站服务的内网穿透"
                                ServiceType.STCP -> "安全TCP点对点连接"
                                ServiceType.SUDP -> "安全UDP点对点连接"
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 配置步骤
                HelpSection(
                    title = "配置步骤",
                    content = "",
                    icon = Icons.Default.Settings
                ) {
                    StepItem(
                        step = 1,
                        title = "准备服务器",
                        description = "需要一台具有公网IP的服务器来运行FRP服务端"
                    )
                    StepItem(
                        step = 2,
                        title = "配置服务端",
                        description = "在服务器上运行frps，设置端口和认证令牌"
                    )
                    StepItem(
                        step = 3,
                        title = "配置客户端",
                        description = "使用本应用配置frpc，连接服务器并设置要穿透的服务"
                    )
                    StepItem(
                        step = 4,
                        title = "开始使用",
                        description = "启动配置，通过公网地址访问内网服务"
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 配置示例
                HelpSection(
                    title = "配置示例",
                    content = "",
                    icon = Icons.Default.Code
                ) {
                    ConfigExample(
                        title = "网站服务穿透",
                        config = """[common]
server_addr = your-server.com
server_port = 7000
token = your-token

[web]
type = http
local_ip = 127.0.0.1
local_port = 8080
custom_domains = your-domain.com"""
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ConfigExample(
                        title = "SSH远程连接",
                        config = """[common]
server_addr = your-server.com
server_port = 7000
token = your-token

[ssh]
type = tcp
local_ip = 127.0.0.1
local_port = 22
remote_port = 6000"""
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 注意事项
                HelpSection(
                    title = "注意事项",
                    content = "",
                    icon = Icons.Default.Warning
                ) {
                    val notes = listOf(
                        "确保服务器防火墙开放相应端口",
                        "使用强密码和安全的认证令牌",
                        "定期更新FRP版本以获得安全修复",
                        "不要将敏感服务暴露到公网",
                        "建议使用HTTPS保护网站服务"
                    )
                    
                    notes.forEach { note ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "•",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("知道了")
            }
        }
    )
}

@Composable
fun HelpSection(
    title: String,
    content: String,
    icon: ImageVector,
    contentComposable: @Composable (() -> Unit)? = null
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        if (content.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        contentComposable?.invoke()
    }
}

@Composable
fun ServiceHelpItem(
    type: ServiceType,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            when (type) {
                ServiceType.HTTP -> Icons.Default.Web
                ServiceType.SSH -> Icons.Default.Terminal
                ServiceType.RDP -> Icons.Default.DesktopWindows
                ServiceType.TCP -> Icons.Default.Settings
                ServiceType.UDP -> Icons.Default.NetworkCheck
                ServiceType.HTTPS -> Icons.Default.Lock
                ServiceType.STCP -> Icons.Default.Security
                ServiceType.SUDP -> Icons.Default.Security
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = type.displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StepItem(
    step: Int,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(24.dp),
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = step.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ConfigExample(
    title: String,
    config: String
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = config,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                style = MaterialTheme.typography.bodySmall.merge(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
