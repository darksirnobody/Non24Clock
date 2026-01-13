# Non24Clock - Project Summary

## Overview
Android app for people with Non-24-Hour Sleep-Wake Disorder. Tracks internal biological time (CB) vs system time (CS).

## Project Location
`C:\Users\adamm\AndroidStudioProjects\Non24Clock`

## Repository
https://github.com/darksirnobody/Non24Clock

## Modules
- **app/** - Main phone application
- **wear/** - Wear OS watchface module

## Key Features Implemented

### Core
- **Non24Clock.kt** - Core clock logic (epoch time, cycle length, internal time calculation)
- Cycle length configurable (default 25h)
- Seconds synchronized: CB seconds always match CS seconds

### UI (Jetpack Compose)
- **5 tabs**: Alarm, Calendar, Integration, Account, Settings
- Material 3 design, dark theme

### Alarm System
- Alarms in biological time (CB)
- Full-screen wake activity with ringtone
- Alarm groups support
- Uses AlarmManager + BroadcastReceiver

### Widget
- Home screen widget showing CB time
- Configurable: background (dark/light/transparent), size, show label
- Auto-refresh every minute via AlarmManager

### Watchface (Wear OS)
- Dual time display: CB (large, white) + CS (small, dimmed)
- Icons: Person (CB), Globe (CS)
- Swap button to switch which time is primary
- Battery indicator
- Ambient mode support
- Syncs settings from phone via Wearable Data Layer API

### Calendar
- **Day view**: System hours (left axis) + biological hours (right axis)
- **Week view**: 7-day grid with sun/moon icons (awake/sleep)
- Current time indicator (red line)
- Click day in week view → opens day view
- Sleep window based on user settings

### Settings
- Sleep window (wake time + sleep duration)
- Notification toggle (persistent notification with CB time)
- Widget customization
- Theme selection
- Permissions management

### Sync (Phone ↔ Watch)
- **WearSyncService.kt** - Sends config to watch
- **DataListenerService.kt** (wear) - Receives config
- Syncs: anchor_time, cycle_length_ms, swap_clocks

## Key Files

### Phone App (app/)
```
app/src/main/java/com/non24/clock/
├── Non24Clock.kt          # Core clock logic
├── MainActivity.kt         # Main activity + navigation
├── ClockWidget.kt         # Widget provider
├── ClockService.kt        # Foreground service for notification
├── sync/
│   └── WearSyncService.kt # Phone→Watch sync
├── alarm/
│   ├── AlarmScheduler.kt
│   ├── AlarmReceiver.kt
│   └── AlarmActivity.kt   # Full-screen wake
├── data/
│   ├── Alarm.kt           # Alarm entity
│   └── AlarmDao.kt        # Room DAO
└── ui/
    ├── alarm/             # Alarm screens
    ├── calendar/          # Calendar screens
    ├── account/           # Account + SetTime/SetCycle/SetSleep screens
    ├── settings/          # Settings screen
    └── components/        # Reusable components (ClockDisplay, WheelTimePicker)
```

### Wear Module (wear/)
```
wear/src/main/java/com/non24/clock/
├── Non24WatchFace.kt      # WatchFaceService + Renderer
└── DataListenerService.kt # Receives sync from phone
```

### Resources
```
app/src/main/res/
├── layout/
│   ├── widget_clock.xml           # Dark widget
│   ├── widget_clock_light.xml     # Light widget
│   └── widget_clock_transparent.xml
├── values/
│   └── strings.xml                # English strings
├── values-pl/
│   └── strings.xml                # Polish strings
└── xml/
    └── non24_clock_widget_info.xml
```

## Technical Notes

### Time Calculation
- `epochTime` = timestamp when CB was 00:00:00
- `cycleLengthMillis` = cycle length in ms (default 25h = 90,000,000ms)
- CB time = `(currentTime - epochTime) % cycleLengthMillis`

### Calendar Sleep Detection
- Uses `wakeHour`, `wakeMinute`, `sleepHours`, `sleepMinutes` from Non24Clock
- Sleep window = wake time - sleep duration → wake time

### Build
- Gradle with Kotlin DSL
- Min SDK 26 (app), 30 (wear)
- Target SDK 34
- JDK 17 required (set in gradle.properties)

## TODO / Future Features
- [ ] Bold option for widget (requires separate layouts)
- [ ] Text view for calendar (shareable availability)
- [ ] Google Calendar integration
- [ ] Philips Hue integration
- [ ] Non24Clock Friends app (for family/friends)

## Strings
- App supports EN and PL
- All user-facing strings in `strings.xml`

## Dependencies
- Jetpack Compose + Material 3
- Room (alarms database)
- Wear OS libraries
- Play Services Wearable (phone↔watch sync)