package com.stfreya.frpc.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stfreya.frpc.FrpConfig

// Color definitions
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)

@Composable
fun ConfigCard(
    config: FrpConfig,
    isRunning: Boolean,
    onStartStop: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isRunning) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                StatusIndicator(isRunning = isRunning)
                ConfigInfo(config = config, isRunning = isRunning)
            }
            
            ConfigActions(
                isRunning = isRunning,
                onEdit = onEdit,
                onDelete = onDelete,
                onStartStop = onStartStop
            )
        }
    }
}

@Composable
private fun StatusIndicator(isRunning: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isRunning) Success else MaterialTheme.colorScheme.outline
    ) {
        Icon(
            if (isRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
            contentDescription = null,
            modifier = Modifier
                .padding(8.dp)
                .size(16.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun ConfigInfo(config: FrpConfig, isRunning: Boolean) {
    Column {
        Text(
            text = config.fileName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = if (isRunning) 
                MaterialTheme.colorScheme.onPrimaryContainer 
            else 
                MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = config.type.typeName.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = if (isRunning) 
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ConfigActions(
    isRunning: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartStop: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onEdit,
            enabled = !isRunning
        ) {
            Icon(
                Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit),
                tint = if (isRunning) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.primary
            )
        }
        IconButton(
            onClick = onDelete,
            enabled = !isRunning
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                tint = if (isRunning) 
                    MaterialTheme.colorScheme.onSurfaceVariant 
                else 
                    MaterialTheme.colorScheme.error
            )
        }
        Switch(
            checked = isRunning,
            onCheckedChange = onStartStop,
            enabled = true
        )
    }
}

@Composable
fun StatusOverviewCard(
    frpcCount: Int,
    frpsCount: Int,
    runningCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.status_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatusItem(
                    label = "FRPC",
                    count = frpcCount,
                    icon = Icons.Default.CloudUpload,
                    color = MaterialTheme.colorScheme.primary
                )
                StatusItem(
                    label = "FRPS",
                    count = frpsCount,
                    icon = Icons.Default.CloudDownload,
                    color = MaterialTheme.colorScheme.secondary
                )
                StatusItem(
                    label = stringResource(R.string.running),
                    count = runningCount,
                    icon = Icons.Default.PlayArrow,
                    color = Success
                )
            }
        }
    }
}

@Composable
private fun StatusItem(
    label: String,
    count: Int,
    icon: ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
