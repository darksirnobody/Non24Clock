package com.non24.clock

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.non24.clock.alarm.AlarmScheduler
import com.non24.clock.data.database.Non24Database
import com.non24.clock.data.model.Alarm
import com.non24.clock.data.model.AlarmGroup
import com.non24.clock.ui.account.AccountScreen
import com.non24.clock.ui.account.SetCycleScreen
import com.non24.clock.ui.account.SetTimeScreen
import com.non24.clock.ui.alarm.AlarmEditScreen
import com.non24.clock.ui.alarm.AlarmScreen
import com.non24.clock.ui.integration.IntegrationScreen
import com.non24.clock.ui.navigation.Screen
import com.non24.clock.ui.settings.SettingsScreen
import com.non24.clock.ui.theme.Non24ClockTheme
import kotlinx.coroutines.launch
import com.non24.clock.sync.WearSyncService
import com.non24.clock.ui.calendar.CalendarScreen
import com.non24.clock.ui.account.SetSleepScreen

class MainActivity : ComponentActivity() {

    private lateinit var clock: Non24Clock
    private lateinit var database: Non24Database

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handle result */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        clock = Non24Clock(this)
        database = Non24Database.getDatabase(this)

        requestNotificationPermission()

        setContent {
            var themeMode by remember { mutableStateOf("system") }

            Non24ClockTheme(themeMode = themeMode) {
                MainScreen(
                    clock = clock,
                    database = database,
                    themeMode = themeMode,
                    onThemeChange = { themeMode = it }
                )
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun MainScreen(
    clock: Non24Clock,
    database: Non24Database,
    themeMode: String,
    onThemeChange: (String) -> Unit
) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Alarm state
    val groups by database.alarmDao().getAllGroups().collectAsState(initial = emptyList())
    var selectedGroupId by remember { mutableLongStateOf(1L) }
    val alarms by database.alarmDao().getAlarmsByGroup(selectedGroupId).collectAsState(initial = emptyList())

    // Notification state
    var notificationEnabled by remember { mutableStateOf(ClockService.isRunning) }

    // Current alarm being edited
    var editingAlarm by remember { mutableStateOf<Alarm?>(null) }

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            // Hide bottom bar on sub-screens
            val showBottomBar = Screen.bottomNavItems.any {
                currentDestination?.hierarchy?.any { dest -> dest.route == it.route } == true
            }

            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true

                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                    contentDescription = null
                                )
                            },
                            label = { Text(stringResource(screen.labelResId)) },
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Alarm.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Alarm screen
            composable(Screen.Alarm.route) {
                AlarmScreen(
                    groups = groups,
                    alarms = alarms,
                    selectedGroupId = selectedGroupId,
                    onGroupSelected = { selectedGroupId = it },
                    onAddGroup = {
                        coroutineScope.launch {
                            val count = database.alarmDao().getGroupCount()
                            if (count < 5) {
                                database.alarmDao().insertGroup(
                                    AlarmGroup(name = "Group ${count + 1}", order = count)
                                )
                            }
                        }
                    },
                    onRenameGroup = { group ->
                        coroutineScope.launch {
                            database.alarmDao().updateGroup(group)
                        }
                    },
                    onDeleteGroup = { group ->
                        coroutineScope.launch {
                            database.alarmDao().deleteGroup(group)
                            if (selectedGroupId == group.id) {
                                selectedGroupId = 1L
                            }
                        }
                    },
                    onAddAlarm = {
                        editingAlarm = null
                        navController.navigate(Screen.AlarmEdit.createRoute(-1))
                    },
                    onAlarmClick = { alarm ->
                        editingAlarm = alarm
                        navController.navigate(Screen.AlarmEdit.createRoute(alarm.id))
                    },
                    onAlarmToggle = { alarm, enabled ->
                        coroutineScope.launch {
                            database.alarmDao().setAlarmEnabled(alarm.id, enabled)
                            // Schedule or cancel alarm
                            if (enabled) {
                                AlarmScheduler.scheduleAlarm(context, alarm.copy(enabled = true))
                            } else {
                                AlarmScheduler.cancelAlarm(context, alarm.id)
                            }
                        }
                    }
                )
            }

            // Alarm edit screen
            composable(Screen.AlarmEdit.route) {
                AlarmEditScreen(
                    clock = clock,
                    alarm = editingAlarm,
                    groupId = selectedGroupId,
                    onSave = { alarm ->
                        coroutineScope.launch {
                            val alarmId = database.alarmDao().insertAlarm(alarm)
                            // Schedule the alarm
                            val savedAlarm = if (alarm.id == 0L) alarm.copy(id = alarmId) else alarm
                            if (savedAlarm.enabled) {
                                AlarmScheduler.scheduleAlarm(context, savedAlarm)
                            }
                            navController.popBackStack()
                        }
                    },
                    onCancel = { navController.popBackStack() },
                    onDelete = if (editingAlarm != null) {
                        {
                            coroutineScope.launch {
                                editingAlarm?.let {
                                    AlarmScheduler.cancelAlarm(context, it.id)
                                    database.alarmDao().deleteAlarm(it)
                                }
                                navController.popBackStack()
                            }
                        }
                    } else null
                )
            }
// Calendar screen
            composable(Screen.Calendar.route) {
                CalendarScreen(clock = clock)
            }
            // Integration screen
            composable(Screen.Integration.route) {
                IntegrationScreen()
            }

            // Account screen
            composable(Screen.Account.route) {
                AccountScreen(
                    clock = clock,
                    onNavigateToSetTime = { navController.navigate(Screen.SetTime.route) },
                    onNavigateToSetCycle = { navController.navigate(Screen.SetCycle.route) },
                    onNavigateToSetSleep = { navController.navigate(Screen.SetSleep.route) }
                )
            }

            // Set time screen
            composable(Screen.SetTime.route) {
                SetTimeScreen(
                    clock = clock,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }

            // Set cycle screen
            composable(Screen.SetCycle.route) {
                SetCycleScreen(
                    clock = clock,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
// Set sleep screen
            composable(Screen.SetSleep.route) {
                SetSleepScreen(
                    clock = clock,
                    onSave = { navController.popBackStack() },
                    onCancel = { navController.popBackStack() }
                )
            }
            // Settings screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    notificationEnabled = notificationEnabled,
                    onNotificationToggle = { enabled ->
                        notificationEnabled = enabled
                        val intent = Intent(context, ClockService::class.java)
                        if (enabled) {
                            ContextCompat.startForegroundService(context, intent)
                        } else {
                            context.stopService(intent)
                        }
                    },
                    onAddWidget = {
                        // TODO: Show widget picker or instructions
                    },
                    currentTheme = themeMode,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}