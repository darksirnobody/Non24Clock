package com.non24.clock.ui.components

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.non24.clock.R

@Composable
fun WheelTimePicker(
    initialHour: Int,
    initialMinute: Int,
    maxHours: Int = 23,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    LaunchedEffect(selectedHour, selectedMinute) {
        onTimeSelected(selectedHour, selectedMinute)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WheelPicker(
            items = (0..maxHours).toList(),
            initialValue = initialHour,
            onValueChanged = { selectedHour = it },
            label = stringResource(R.string.hours),
            modifier = Modifier.weight(1f)
        )

        Text(
            text = ":",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        WheelPicker(
            items = (0..59).toList(),
            initialValue = initialMinute,
            onValueChanged = { selectedMinute = it },
            label = stringResource(R.string.minutes),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun WheelPicker(
    items: List<Int>,
    initialValue: Int,
    onValueChanged: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    val itemHeightDp = 60.dp
    val itemHeightPx = 60
    val initialIndex = items.indexOf(initialValue).coerceAtLeast(0)

    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
    val snapBehavior = rememberSnapFlingBehavior(lazyListState = listState)

    // Selected is simply firstVisibleItemIndex (centered by contentPadding)
    val selectedIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex in items.indices) {
            onValueChanged(items[selectedIndex])
        }
    }

    Column(
        modifier = modifier.semantics { contentDescription = label },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            state = listState,
            flingBehavior = snapBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeightDp),
            modifier = Modifier.height(itemHeightDp * 3)
        ) {
            items(items.size) { index ->
                val isSelected = index == selectedIndex
                val distance = kotlin.math.abs(index - selectedIndex)
                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.5f
                    else -> 0.3f
                }

                Box(
                    modifier = Modifier
                        .height(itemHeightDp)
                        .alpha(alpha),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = String.format("%02d", items[index]),
                        fontSize = if (isSelected) 48.sp else 32.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}