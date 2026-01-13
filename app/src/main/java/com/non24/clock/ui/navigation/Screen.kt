package com.non24.clock.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.non24.clock.R

sealed class Screen(
    val route: String,
    @StringRes val labelResId: Int = 0,
    val selectedIcon: ImageVector = Icons.Filled.Home,
    val unselectedIcon: ImageVector = Icons.Outlined.Home
) {
    // Bottom nav screens
    object Alarm : Screen(
        route = "alarm",
        labelResId = R.string.nav_alarm,
        selectedIcon = Icons.Filled.Alarm,
        unselectedIcon = Icons.Outlined.Alarm
    )

    object Calendar : Screen(
        route = "calendar",
        labelResId = R.string.nav_calendar,
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth
    )

    object Integration : Screen(
        route = "integration",
        labelResId = R.string.nav_integration,
        selectedIcon = Icons.Filled.Sync,
        unselectedIcon = Icons.Outlined.Sync
    )

    object Account : Screen(
        route = "account",
        labelResId = R.string.nav_account,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )

    object Settings : Screen(
        route = "settings",
        labelResId = R.string.nav_settings,
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    // Sub-screens
    object AlarmEdit : Screen(route = "alarm/edit/{alarmId}") {
        fun createRoute(alarmId: Long) = "alarm/edit/$alarmId"
    }

    object SetTime : Screen(route = "account/settime")
    object SetCycle : Screen(route = "account/setcycle")

    object SetSleep : Screen(route = "account/setsleep")

    companion object {
        val bottomNavItems = listOf(Alarm, Calendar, Integration, Account, Settings)
    }
}