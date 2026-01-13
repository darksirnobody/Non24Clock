package com.non24.clock.ui.settings

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.non24.clock.ClockWidget
import com.non24.clock.R

@Composable
fun SettingsScreen(
    notificationEnabled: Boolean,
    onNotificationToggle: (Boolean) -> Unit,
    onAddWidget: () -> Unit,
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Check permissions state
    var notificationPermissionGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    var batteryOptimizationDisabled by remember { mutableStateOf(checkBatteryOptimization(context)) }
    var overlayPermissionGranted by remember { mutableStateOf(checkOverlayPermission(context)) }

    // Refresh permissions when screen is visible
    LaunchedEffect(Unit) {
        notificationPermissionGranted = checkNotificationPermission(context)
        batteryOptimizationDisabled = checkBatteryOptimization(context)
        overlayPermissionGranted = checkOverlayPermission(context)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.nav_settings),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        // Permissions section
        SectionHeader(stringResource(R.string.permissions_section))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                PermissionItem(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.permission_notifications),
                    subtitle = stringResource(R.string.permission_notifications_desc),
                    isGranted = notificationPermissionGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                PermissionItem(
                    icon = Icons.Outlined.BatteryFull,
                    title = stringResource(R.string.permission_battery),
                    subtitle = stringResource(R.string.permission_battery_desc),
                    isGranted = batteryOptimizationDisabled,
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                PermissionItem(
                    icon = Icons.Outlined.Layers,
                    title = stringResource(R.string.permission_overlay),
                    subtitle = stringResource(R.string.permission_overlay_desc),
                    isGranted = overlayPermissionGranted,
                    onClick = {
                        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Widget section
        SectionHeader(stringResource(R.string.widget_section))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            var showBackgroundDialog by remember { mutableStateOf(false) }
            var currentWidgetBg by remember { mutableStateOf(ClockWidget.getBackground(context)) }
            var showSizeDialog by remember { mutableStateOf(false) }
            var currentWidgetSize by remember { mutableStateOf(ClockWidget.getSize(context)) }
            var showLabel by remember { mutableStateOf(ClockWidget.getShowLabel(context)) }


            Column {
                // Add widget
                SettingItem(
                    icon = Icons.Outlined.AddCircleOutline,
                    title = stringResource(R.string.widget_add),
                    subtitle = stringResource(R.string.widget_add_desc),
                    onClick = {
                        requestPinWidget(context)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Widget background
                SettingItem(
                    icon = Icons.Outlined.Palette,
                    title = stringResource(R.string.widget_background),
                    subtitle = when (currentWidgetBg) {
                        ClockWidget.BG_LIGHT -> stringResource(R.string.widget_background_light)
                        ClockWidget.BG_TRANSPARENT -> stringResource(R.string.widget_background_transparent)
                        else -> stringResource(R.string.widget_background_dark)
                    },
                    onClick = { showBackgroundDialog = true }
                )

                if (showBackgroundDialog) {
                    WidgetBackgroundDialog(
                        currentBackground = currentWidgetBg,
                        onBackgroundSelected = { bg ->
                            ClockWidget.setBackground(context, bg)
                            currentWidgetBg = bg
                            showBackgroundDialog = false
                        },
                        onDismiss = { showBackgroundDialog = false }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Widget size
                SettingItem(
                    icon = Icons.Outlined.FormatSize,
                    title = stringResource(R.string.widget_size),
                    subtitle = when (currentWidgetSize) {
                        ClockWidget.SIZE_SMALL -> stringResource(R.string.widget_size_small)
                        ClockWidget.SIZE_LARGE -> stringResource(R.string.widget_size_large)
                        ClockWidget.SIZE_XLARGE -> stringResource(R.string.widget_size_xlarge)
                        else -> stringResource(R.string.widget_size_medium)
                    },
                    onClick = { showSizeDialog = true }
                )

                if (showSizeDialog) {
                    WidgetSizeDialog(
                        currentSize = currentWidgetSize,
                        onSizeSelected = { size ->
                            ClockWidget.setSize(context, size)
                            currentWidgetSize = size
                            showSizeDialog = false
                        },
                        onDismiss = { showSizeDialog = false }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                // Show label toggle
                SettingItem(
                    icon = Icons.Outlined.Label,
                    title = stringResource(R.string.widget_show_label),
                    subtitle = stringResource(R.string.widget_show_label_desc),
                    trailing = {
                        Switch(
                            checked = showLabel,
                            onCheckedChange = {
                                ClockWidget.setShowLabel(context, it)
                                showLabel = it
                            }
                        )
                    }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

// Bold text toggle

            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Clock section
        SectionHeader(stringResource(R.string.clock_section))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                SettingItem(
                    icon = Icons.Outlined.Notifications,
                    title = stringResource(R.string.notification),
                    subtitle = stringResource(R.string.notification_desc),
                    trailing = {
                        Switch(
                            checked = notificationEnabled,
                            onCheckedChange = onNotificationToggle
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // App section
        SectionHeader(stringResource(R.string.app_section))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column {
                SettingItem(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.language),
                    subtitle = stringResource(R.string.language_system),
                    onClick = {
                        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
                        context.startActivity(intent)
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                var showThemeDialog by remember { mutableStateOf(false) }

                SettingItem(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.theme),
                    subtitle = when (currentTheme) {
                        "light" -> stringResource(R.string.theme_light)
                        "dark" -> stringResource(R.string.theme_dark)
                        else -> stringResource(R.string.theme_system)
                    },
                    onClick = { showThemeDialog = true }
                )

                if (showThemeDialog) {
                    ThemeDialog(
                        currentTheme = currentTheme,
                        onThemeSelected = {
                            onThemeChange(it)
                            showThemeDialog = false
                        },
                        onDismiss = { showThemeDialog = false }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accessibility section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            SettingItem(
                icon = Icons.Outlined.Accessibility,
                title = stringResource(R.string.accessibility),
                subtitle = stringResource(R.string.talkback_info),
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isGranted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        Text(
            text = if (isGranted)
                stringResource(R.string.permission_granted)
            else
                stringResource(R.string.permission_not_granted),
            style = MaterialTheme.typography.bodySmall,
            color = if (isGranted)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        trailing?.invoke()
    }
}

@Composable
private fun ThemeDialog(
    currentTheme: String,
    onThemeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.theme)) },
        text = {
            Column {
                DialogOption(
                    text = stringResource(R.string.theme_system),
                    selected = currentTheme == "system",
                    onClick = { onThemeSelected("system") }
                )
                DialogOption(
                    text = stringResource(R.string.theme_light),
                    selected = currentTheme == "light",
                    onClick = { onThemeSelected("light") }
                )
                DialogOption(
                    text = stringResource(R.string.theme_dark),
                    selected = currentTheme == "dark",
                    onClick = { onThemeSelected("dark") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun WidgetBackgroundDialog(
    currentBackground: String,
    onBackgroundSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widget_background)) },
        text = {
            Column {
                DialogOption(
                    text = stringResource(R.string.widget_background_dark),
                    selected = currentBackground == ClockWidget.BG_DARK,
                    onClick = { onBackgroundSelected(ClockWidget.BG_DARK) }
                )
                DialogOption(
                    text = stringResource(R.string.widget_background_light),
                    selected = currentBackground == ClockWidget.BG_LIGHT,
                    onClick = { onBackgroundSelected(ClockWidget.BG_LIGHT) }
                )
                DialogOption(
                    text = stringResource(R.string.widget_background_transparent),
                    selected = currentBackground == ClockWidget.BG_TRANSPARENT,
                    onClick = { onBackgroundSelected(ClockWidget.BG_TRANSPARENT) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun WidgetSizeDialog(
    currentSize: String,
    onSizeSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.widget_size)) },
        text = {
            Column {
                DialogOption(
                    text = stringResource(R.string.widget_size_small),
                    selected = currentSize == ClockWidget.SIZE_SMALL,
                    onClick = { onSizeSelected(ClockWidget.SIZE_SMALL) }
                )
                DialogOption(
                    text = stringResource(R.string.widget_size_medium),
                    selected = currentSize == ClockWidget.SIZE_MEDIUM,
                    onClick = { onSizeSelected(ClockWidget.SIZE_MEDIUM) }
                )
                DialogOption(
                    text = stringResource(R.string.widget_size_large),
                    selected = currentSize == ClockWidget.SIZE_LARGE,
                    onClick = { onSizeSelected(ClockWidget.SIZE_LARGE) }
                )
                DialogOption(
                    text = stringResource(R.string.widget_size_xlarge),
                    selected = currentSize == ClockWidget.SIZE_XLARGE,
                    onClick = { onSizeSelected(ClockWidget.SIZE_XLARGE) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun DialogOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

private fun requestPinWidget(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val widgetProvider = ComponentName(context, ClockWidget::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            appWidgetManager.requestPinAppWidget(widgetProvider, null, null)
        }
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

private fun checkBatteryOptimization(context: Context): Boolean {
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    return powerManager.isIgnoringBatteryOptimizations(context.packageName)
}

private fun checkOverlayPermission(context: Context): Boolean {
    return Settings.canDrawOverlays(context)
}