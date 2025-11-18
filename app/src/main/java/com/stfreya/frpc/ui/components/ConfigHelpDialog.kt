package com.stfreya.frpc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
                    Icons.AutoMirrored.Filled.Help,
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
                    title = stringResource(R.string.help_what_is_frp),
                    content = stringResource(R.string.help_frp_description),
                    icon = Icons.Default.Info
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 使用场景
                HelpSection(
                    title = stringResource(R.string.help_common_scenarios),
                    content = "",
                    icon = Icons.AutoMirrored.Filled.List
                ) {
                    ServiceType.values().forEach { type ->
                        ServiceHelpItem(
                            type = type,
                            description = when (type) {
                                ServiceType.HTTP -> stringResource(R.string.help_http_scenario)
                                ServiceType.SSH -> stringResource(R.string.help_ssh_scenario)
                                ServiceType.RDP -> stringResource(R.string.help_rdp_scenario)
                                ServiceType.TCP -> stringResource(R.string.help_tcp_scenario)
                                ServiceType.UDP -> stringResource(R.string.help_udp_scenario)
                                ServiceType.HTTPS -> stringResource(R.string.help_https_scenario)
                                ServiceType.STCP -> stringResource(R.string.help_stcp_scenario)
                                ServiceType.SUDP -> stringResource(R.string.help_sudp_scenario)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 配置步骤
                HelpSection(
                    title = stringResource(R.string.help_config_steps),
                    content = "",
                    icon = Icons.Default.Settings
                ) {
                    StepItem(
                        step = 1,
                        title = stringResource(R.string.help_step1_title),
                        description = stringResource(R.string.help_step1_desc)
                    )
                    StepItem(
                        step = 2,
                        title = stringResource(R.string.help_step2_title),
                        description = stringResource(R.string.help_step2_desc)
                    )
                    StepItem(
                        step = 3,
                        title = stringResource(R.string.help_step3_title),
                        description = stringResource(R.string.help_step3_desc)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 配置示例
                HelpSection(
                    title = stringResource(R.string.help_config_examples),
                    content = "",
                    icon = Icons.Default.Code
                ) {
                    ConfigExample(
                        title = stringResource(R.string.help_example_web_title),
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
                        title = stringResource(R.string.help_example_ssh_title),
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
                    title = stringResource(R.string.help_notes_title),
                    content = "",
                    icon = Icons.Default.Warning
                ) {
                    val notes = listOf(
                        stringResource(R.string.help_note1),
                        stringResource(R.string.help_note2),
                        stringResource(R.string.help_note3),
                        stringResource(R.string.help_note4),
                        stringResource(R.string.help_note5)
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
                Text(stringResource(R.string.help_got_it))
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
                text = stringResource(type.displayNameRes),
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
