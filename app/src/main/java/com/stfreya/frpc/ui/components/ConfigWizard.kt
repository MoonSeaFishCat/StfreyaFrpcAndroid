package com.stfreya.frpc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.stfreya.frpc.R

data class WizardStep(
    val title: String,
    val description: String,
    val icon: ImageVector
)

data class ServerInfo(
    val address: String = "",
    val port: String = "7000",
    val token: String = ""
)

data class ServiceInfo(
    val name: String = "",
    val type: ServiceType = ServiceType.HTTP,
    val localPort: String = "",
    val remotePort: String = ""
)

enum class ServiceType(val displayName: String, val example: String, val frpType: String) {
    HTTP("网站服务 (HTTP)", "例如：80, 8080, 3000", "http"),
    SSH("SSH远程连接", "默认22", "tcp"),
    RDP("远程桌面 (RDP)", "默认3389", "tcp"),
    TCP("TCP服务", "自定义端口", "tcp"),
    UDP("UDP服务", "自定义端口", "udp"),
    HTTPS("HTTPS服务", "例如：443, 8443", "https"),
    STCP("安全TCP", "点对点连接", "stcp"),
    SUDP("安全UDP", "点对点连接", "sudp")
}

@Composable
fun ConfigWizard(
    onConfigGenerated: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var serverInfo by remember { mutableStateOf(ServerInfo()) }
    var serviceInfo by remember { mutableStateOf(ServiceInfo()) }
    var selectedTemplate by remember { mutableStateOf<ServiceType?>(null) }
    
    val steps = listOf(
        WizardStep(
            title = stringResource(R.string.config_templates),
            description = stringResource(R.string.template_description),
            icon = Icons.Default.List
        ),
        WizardStep(
            title = stringResource(R.string.server_info),
            description = "配置服务器连接信息",
            icon = Icons.Default.Cloud
        ),
        WizardStep(
            title = stringResource(R.string.service_type),
            description = stringResource(R.string.select_service_type),
            icon = Icons.Default.Settings
        ),
        WizardStep(
            title = stringResource(R.string.config_preview),
            description = stringResource(R.string.config_generated_description),
            icon = Icons.Default.Preview
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(24.dp)
        ) {
            // 标题
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.config_wizard_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.config_wizard_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 步骤指示器
            StepIndicator(
                currentStep = currentStep,
                totalSteps = steps.size,
                steps = steps
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 步骤内容 - 使用可滚动区域
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (currentStep) {
                    0 -> TemplateSelectionStep(
                        selectedTemplate = selectedTemplate,
                        onTemplateSelected = { 
                            selectedTemplate = it
                            serviceInfo = serviceInfo.copy(type = it)
                        }
                    )
                    1 -> ServerInfoStep(
                        serverInfo = serverInfo,
                        onServerInfoChanged = { serverInfo = it }
                    )
                    2 -> ServiceConfigStep(
                        serviceInfo = serviceInfo,
                        onServiceInfoChanged = { serviceInfo = it }
                    )
                    3 -> ConfigPreviewStep(
                        serverInfo = serverInfo,
                        serviceInfo = serviceInfo,
                        onConfigGenerated = onConfigGenerated
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 导航按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentStep > 0) {
                    OutlinedButton(
                        onClick = { currentStep-- },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.previous_step))
                    }
                } else {
                    // 占位符，保持按钮对齐
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                if (currentStep < steps.size - 1) {
                    Button(
                        onClick = { currentStep++ },
                        modifier = Modifier.weight(1f),
                        enabled = when (currentStep) {
                            0 -> selectedTemplate != null
                            1 -> serverInfo.address.isNotBlank() && serverInfo.port.isNotBlank()
                            2 -> serviceInfo.name.isNotBlank() && serviceInfo.localPort.isNotBlank() && serviceInfo.remotePort.isNotBlank()
                            else -> true
                        }
                    ) {
                        Text(stringResource(R.string.next_step))
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.finish_setup))
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    steps: List<WizardStep>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        steps.forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 步骤圆圈
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .selectable(
                            selected = index == currentStep,
                            onClick = { },
                            role = Role.RadioButton
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = if (index <= currentStep) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.outline,
                        contentColor = if (index <= currentStep) 
                            MaterialTheme.colorScheme.onPrimary 
                        else 
                            MaterialTheme.colorScheme.onSurface
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (index < currentStep) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                // 步骤信息
                if (index == currentStep) {
                    Column {
                        Text(
                            text = step.title,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = step.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // 连接线
                if (index < totalSteps - 1) {
                    Spacer(modifier = Modifier.width(16.dp))
                }
            }
        }
    }
}

@Composable
fun TemplateSelectionStep(
    selectedTemplate: ServiceType?,
    onTemplateSelected: (ServiceType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.config_templates),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.template_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Column(
            modifier = Modifier.selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ServiceType.values().forEach { type ->
                TemplateCard(
                    type = type,
                    isSelected = selectedTemplate == type,
                    onClick = { onTemplateSelected(type) }
                )
            }
        }
    }
}

@Composable
fun TemplateCard(
    type: ServiceType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = isSelected,
                onClick = onClick,
                role = Role.RadioButton
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) 
            CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)
            ) 
        else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                tint = if (isSelected) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = type.example,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ServerInfoStep(
    serverInfo: ServerInfo,
    onServerInfoChanged: (ServerInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.server_info),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.server_help_content),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = serverInfo.address,
            onValueChange = { onServerInfoChanged(serverInfo.copy(address = it)) },
            label = { Text(stringResource(R.string.server_address)) },
            placeholder = { Text(stringResource(R.string.server_address_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Cloud, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = serverInfo.port,
            onValueChange = { onServerInfoChanged(serverInfo.copy(port = it)) },
            label = { Text(stringResource(R.string.server_port)) },
            placeholder = { Text(stringResource(R.string.server_port_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = serverInfo.token,
            onValueChange = { onServerInfoChanged(serverInfo.copy(token = it)) },
            label = { Text("${stringResource(R.string.server_token)} (可选)") },
            placeholder = { Text("${stringResource(R.string.server_token_hint)} (可选)") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Key, contentDescription = null)
            },
            supportingText = {
                Text(
                    text = "如果服务器启用了认证，请输入 token；否则留空",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
    }
}

@Composable
fun ServiceConfigStep(
    serviceInfo: ServiceInfo,
    onServiceInfoChanged: (ServiceInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.service_type),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.service_help_content),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        OutlinedTextField(
            value = serviceInfo.name,
            onValueChange = { onServiceInfoChanged(serviceInfo.copy(name = it)) },
            label = { Text(stringResource(R.string.service_name)) },
            placeholder = { Text(stringResource(R.string.service_name_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Label, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = serviceInfo.localPort,
            onValueChange = { onServiceInfoChanged(serviceInfo.copy(localPort = it)) },
            label = { Text(stringResource(R.string.local_port)) },
            placeholder = { Text(stringResource(R.string.local_port_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Home, contentDescription = null)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = serviceInfo.remotePort,
            onValueChange = { onServiceInfoChanged(serviceInfo.copy(remotePort = it)) },
            label = { Text(stringResource(R.string.remote_port)) },
            placeholder = { Text(stringResource(R.string.remote_port_hint)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Public, contentDescription = null)
            }
        )
    }
}

@Composable
fun ConfigPreviewStep(
    serverInfo: ServerInfo,
    serviceInfo: ServiceInfo,
    onConfigGenerated: (String) -> Unit
) {
    val configText = remember(serverInfo, serviceInfo) {
        generateConfig(serverInfo, serviceInfo)
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.config_preview),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.config_generated_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = configText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyMedium.merge(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = { onConfigGenerated(configText) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.save_config))
        }
    }
}

private fun generateConfig(serverInfo: ServerInfo, serviceInfo: ServiceInfo): String {
    return buildString {
        // TOML 格式配置 - 使用正确的 FRP 0.65.0 格式
        appendLine("serverAddr = \"${serverInfo.address}\"")
        appendLine("serverPort = ${serverInfo.port}")
        appendLine()
        
        // 添加认证配置（可选）
        if (serverInfo.token.isNotBlank()) {
            appendLine("[auth]")
            appendLine("token = \"${serverInfo.token}\"")
            appendLine()
        }
        
        // 添加日志配置
        appendLine("[log]")
        appendLine("level = \"info\"")
        appendLine("disablePrintColor = true")
        appendLine()
        
        // 添加代理配置
        appendLine("[[proxies]]")
        appendLine("name = \"${serviceInfo.name}\"")
        appendLine("type = \"${serviceInfo.type.frpType}\"")
        appendLine("localIP = \"127.0.0.1\"")
        appendLine("localPort = ${serviceInfo.localPort}")
        appendLine("remotePort = ${serviceInfo.remotePort}")
        
        // 根据服务类型添加特定配置
        when (serviceInfo.type) {
            ServiceType.HTTP -> {
                appendLine("customDomains = [\"your-domain.com\"]")
            }
            ServiceType.HTTPS -> {
                appendLine("customDomains = [\"your-domain.com\"]")
            }
            ServiceType.SSH -> {
                // SSH 不需要额外配置
            }
            ServiceType.RDP -> {
                // RDP 不需要额外配置
            }
            ServiceType.TCP -> {
                // TCP 不需要额外配置
            }
            ServiceType.UDP -> {
                // UDP 不需要额外配置
            }
            ServiceType.STCP -> {
                // STCP 需要 secretKey
                appendLine("secretKey = \"your-secret-key\"")
            }
            ServiceType.SUDP -> {
                // SUDP 需要 secretKey
                appendLine("secretKey = \"your-secret-key\"")
            }
        }
    }
}
