package com.stfreya.frpc

import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Copy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stfreya.frpc.ui.theme.StfreyaFrpcTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class ConfigActivity : ComponentActivity() {
    private val configEditText = MutableStateFlow(TextFieldValue(""))
    private val isAutoStart = MutableStateFlow(false)
    private lateinit var configFile: File
    private lateinit var autoStartPreferencesKey: String
    private lateinit var preferences: SharedPreferences
    private var hasUnsavedChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val frpConfig = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.extras?.getParcelable(IntentExtraKey.FrpConfig, FrpConfig::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.extras?.getParcelable(IntentExtraKey.FrpConfig)
        }
        if (frpConfig == null) {
            Log.e("adx", "frp config is null")
            Toast.makeText(this, "frp config is null", Toast.LENGTH_SHORT).show()
            setResult(RESULT_CANCELED)
            finish()
            return
        }
        configFile = frpConfig.getFile(this)
        autoStartPreferencesKey = frpConfig.type.getAutoStartPreferencesKey()
        preferences = getSharedPreferences("data", MODE_PRIVATE)
        readConfig()
        readIsAutoStart()

        enableEdgeToEdge()
        setContent {
            StfreyaFrpcTheme {
                ConfigScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ConfigScreen() {
        val configText by configEditText.collectAsStateWithLifecycle(TextFieldValue(""))
        val isAutoStart by isAutoStart.collectAsStateWithLifecycle(false)
        val clipboardManager = LocalClipboardManager.current

        var showRenameDialog by remember { mutableStateOf(false) }
        var showSaveDialog by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "Edit Configuration",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = configFile.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            if (configText.text != configFile.readText()) {
                                showSaveDialog = true
                            } else {
                                finish()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            clipboardManager.setText(AnnotatedString(configText.text))
                            Toast.makeText(this@ConfigActivity, "Configuration copied to clipboard", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Outlined.Copy, contentDescription = "Copy")
                        }
                        IconButton(onClick = { showRenameDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { 
                        saveConfig()
                        finish()
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save")
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Auto Start Switch Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    text = stringResource(R.string.auto_start_switch),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Start this configuration automatically on boot",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isAutoStart,
                            onCheckedChange = { setAutoStart(it) }
                        )
                    }
                }

                // Configuration Editor Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Code,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Configuration Editor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Edit your ${configFile.name} configuration file:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = configText,
                            onValueChange = { 
                                configEditText.value = it
                                hasUnsavedChanges = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(400.dp),
                            textStyle = MaterialTheme.typography.bodyMedium.merge(
                                fontFamily = FontFamily.Monospace
                            ),
                            placeholder = {
                                Text(
                                    text = "Enter your configuration here...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { 
                            if (hasUnsavedChanges) {
                                showSaveDialog = true
                            } else {
                                finish()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = { 
                            saveConfig()
                            finish()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.saveConfigButton))
                    }
                }
            }
        }

        // Rename Dialog
        if (showRenameDialog) {
            RenameDialog(
                currentName = configFile.name.removeSuffix(".toml"),
                onDismiss = { showRenameDialog = false },
                onRename = { newName ->
                    renameConfig("$newName.toml")
                    showRenameDialog = false
                }
            )
        }

        // Save Confirmation Dialog
        if (showSaveDialog) {
            SaveConfirmationDialog(
                onDismiss = { showSaveDialog = false },
                onSave = {
                    saveConfig()
                    finish()
                },
                onDiscard = {
                    finish()
                }
            )
        }
    }

    @Composable
    fun RenameDialog(
        currentName: String,
        onDismiss: () -> Unit,
        onRename: (String) -> Unit
    ) {
        var newName by remember { mutableStateOf(currentName) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.rename),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a new name for this configuration:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Configuration Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { onRename(newName) },
                    enabled = newName.isNotBlank() && newName != currentName
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.dismiss))
                }
            }
        )
    }

    @Composable
    fun SaveConfirmationDialog(
        onDismiss: () -> Unit,
        onSave: () -> Unit,
        onDiscard: () -> Unit
    ) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Save Changes?",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = "You have unsaved changes. Do you want to save them before closing?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(onClick = onSave) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onDiscard) {
                    Text("Discard")
                }
            }
        )
    }

    fun readConfig() {
        try {
            if (configFile.exists()) {
                val content = configFile.readText()
                configEditText.value = TextFieldValue(content)
            } else {
                Log.e("adx", "config file does not exist")
                Toast.makeText(this, getString(R.string.config_file_not_exist), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("adx", "Error reading config file", e)
            Toast.makeText(this, "Error reading config file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun saveConfig() {
        try {
            configFile.writeText(configEditText.value.text)
            hasUnsavedChanges = false
            Toast.makeText(this, getString(R.string.configuration_saved), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("adx", "Error saving config file", e)
            Toast.makeText(this, "Error saving config file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun renameConfig(newName: String) {
        val originAutoStart = isAutoStart.value
        setAutoStart(false)
        val newFile = File(configFile.parent, newName)
        configFile.renameTo(newFile)
        configFile = newFile
        setAutoStart(originAutoStart)
        Toast.makeText(this, "Configuration renamed successfully", Toast.LENGTH_SHORT).show()
    }

    fun readIsAutoStart() {
        isAutoStart.value =
            preferences.getStringSet(autoStartPreferencesKey, emptySet())?.contains(configFile.name)
                ?: false
    }

    fun setAutoStart(value: Boolean) {
        val editor = preferences.edit()
        val set = preferences.getStringSet(autoStartPreferencesKey, emptySet())?.toMutableSet()
        if (value) {
            set?.add(configFile.name)
        } else {
            set?.remove(configFile.name)
        }
        editor.putStringSet(autoStartPreferencesKey, set)
        editor.apply()
        isAutoStart.value = value
    }
}