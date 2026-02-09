package com.wongchoi500.babylog.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wongchoi500.babylog.data.BabyLog
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogScreen(
    onSave: (BabyLog) -> Unit,
    onCancel: () -> Unit
) {
    var type by remember { mutableStateOf("MILK") }
    var amountMl by remember { mutableStateOf("") }
    
    val isMilkAmountError = remember(type, amountMl) {
        type == "MILK" && amountMl.isNotEmpty() && (amountMl.toIntOrNull() == null || amountMl.toIntOrNull()!! > 9999)
    }

    var hasPee by remember { mutableStateOf(false) }
    var peeAmount by remember { mutableStateOf("中") }
    var hasPoop by remember { mutableStateOf(false) }
    var poopDetails by remember { mutableStateOf("") }
    var foodContent by remember { mutableStateOf("") }
    var foodAmount by remember { mutableStateOf("") }
    var foodPreference by remember { mutableStateOf("一般") }
    
    // 默认日期和时间
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePickerForGeneral by remember { mutableStateOf(false) }

    // 睡眠专用状态
    var fallAsleepTime by remember { mutableStateOf(LocalTime.now().minusHours(1)) }
    var wakeUpTime by remember { mutableStateOf(LocalTime.now()) }
    var showTimePickerForFallAsleep by remember { mutableStateOf(false) }
    var showTimePickerForWakeUp by remember { mutableStateOf(false) }
    var isNightWake by remember { mutableStateOf(false) }

    val types = listOf("MILK", "DIAPER", "SOLIDS", "SLEEP")
    val typeLabels = mapOf(
        "MILK" to "喂奶",
        "DIAPER" to "尿布",
        "SOLIDS" to "辅食",
        "SLEEP" to "睡眠"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .heightIn(max = 600.dp)
    ) {
        Text(
            "记录宝宝时刻",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "类型",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { t ->
                    val isSelected = type == t
                    Surface(
                        onClick = { type = t },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = typeLabels[t] ?: t,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("日期和时间", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedCard(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                if (type != "SLEEP") {
                    OutlinedCard(
                        onClick = { showTimePickerForGeneral = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                            modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                )
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let {
                                selectedDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            }
                            showDatePicker = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("取消") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showTimePickerForGeneral) {
                val timePickerState = rememberTimePickerState(
                    initialHour = selectedTime.hour,
                    initialMinute = selectedTime.minute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showTimePickerForGeneral = false },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePickerForGeneral = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerForGeneral = false }) { Text("取消") }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            if (showTimePickerForFallAsleep) {
                val timePickerState = rememberTimePickerState(
                    initialHour = fallAsleepTime.hour,
                    initialMinute = fallAsleepTime.minute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showTimePickerForFallAsleep = false },
                    confirmButton = {
                        TextButton(onClick = {
                            fallAsleepTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePickerForFallAsleep = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerForFallAsleep = false }) { Text("取消") }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            if (showTimePickerForWakeUp) {
                val timePickerState = rememberTimePickerState(
                    initialHour = wakeUpTime.hour,
                    initialMinute = wakeUpTime.minute,
                    is24Hour = true
                )
                AlertDialog(
                    onDismissRequest = { showTimePickerForWakeUp = false },
                    confirmButton = {
                        TextButton(onClick = {
                            wakeUpTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            showTimePickerForWakeUp = false
                        }) { Text("确定") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTimePickerForWakeUp = false }) { Text("取消") }
                    },
                    text = { TimePicker(state = timePickerState) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (type) {
                "MILK" -> {
                    OutlinedTextField(
                        value = amountMl,
                        onValueChange = { 
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                amountMl = it
                            }
                        },
                        label = { Text("奶量 (ml)") },
                        isError = isMilkAmountError,
                        supportingText = {
                            if (isMilkAmountError) {
                                Text("奶量不能超过 9999 ml")
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                "DIAPER" -> {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasPee, onCheckedChange = { hasPee = it })
                            Text("小便")
                        }
                        if (hasPee) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(start = 24.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val peeLabels = mapOf("Small" to "少", "Medium" to "中", "Large" to "多")
                                listOf("Small", "Medium", "Large").forEach {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = peeAmount == it, onClick = { peeAmount = it })
                                        Text(peeLabels[it] ?: it, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = hasPoop, onCheckedChange = { hasPoop = it })
                            Text("大便")
                        }
                        if (hasPoop) {
                            OutlinedTextField(
                                value = poopDetails,
                                onValueChange = { poopDetails = it },
                                label = { Text("大便详情 (颜色/性状)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
                "SOLIDS" -> {
                    Column {
                        OutlinedTextField(
                            value = foodContent,
                            onValueChange = { foodContent = it },
                            label = { Text("辅食内容 (如：苹果泥)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("宝宝反应", style = MaterialTheme.typography.titleSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val preferences = listOf("爱吃", "一般", "不吃", "其他")
                            preferences.forEach { pref ->
                                val isSelected = foodPreference == pref
                                Surface(
                                    onClick = { foodPreference = pref },
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                    ),
                                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    modifier = Modifier.weight(1f).height(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = pref,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = foodAmount,
                            onValueChange = { foodAmount = it },
                            label = { Text("数量和备注") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                "SLEEP" -> {
                    Column {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("入睡时间", style = MaterialTheme.typography.titleMedium)
                        OutlinedCard(
                            onClick = { showTimePickerForFallAsleep = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = fallAsleepTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("睡醒时间", style = MaterialTheme.typography.titleMedium)
                        OutlinedCard(
                            onClick = { showTimePickerForWakeUp = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = wakeUpTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                modifier = Modifier.padding(12.dp).align(Alignment.CenterHorizontally),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        // 判断是否在18:00到06:00之间
                        val showNightWakeOption = fallAsleepTime.hour >= 18 || fallAsleepTime.hour < 6
                        if (showNightWakeOption) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("夜醒", style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.weight(1f))
                                Switch(checked = isNightWake, onCheckedChange = { isNightWake = it })
                                Text(if (isNightWake) "是" else "否", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("取消")
            }
            Button(
                onClick = {
                    val finalEndTime: Long?
                    val finalStartTime: Long
                    
                    if (type == "SLEEP") {
                        var startDateTime = LocalDateTime.of(selectedDate, fallAsleepTime)
                        var wakeUpDateTime = LocalDateTime.of(selectedDate, wakeUpTime)
                        
                        // 如果睡醒时间早于入睡时间，通常意味着跨天了
                        if (wakeUpDateTime.isBefore(startDateTime)) {
                            wakeUpDateTime = wakeUpDateTime.plusDays(1)
                        }
                        
                        finalStartTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        finalEndTime = wakeUpDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    } else {
                        val startDateTime = LocalDateTime.of(selectedDate, selectedTime)
                        finalStartTime = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                        finalEndTime = null
                    }

                    onSave(
                        BabyLog(
                            type = type,
                            startTime = finalStartTime,
                            endTime = finalEndTime,
                            amountMl = if (type == "MILK") (amountMl.toIntOrNull() ?: 0) else null,
                            hasPee = if (type == "DIAPER") hasPee else null,
                            peeAmount = if (type == "DIAPER" && hasPee) peeAmount else null,
                            hasPoop = if (type == "DIAPER") hasPoop else null,
                            poopDetails = if (type == "DIAPER" && hasPoop) poopDetails else null,
                            foodContent = if (type == "SOLIDS") foodContent else null,
                            foodAmount = if (type == "SOLIDS") {
                                if (foodAmount.isEmpty()) "[$foodPreference]" else "[$foodPreference] $foodAmount"
                            } else null,
                            isNightWake = if (type == "SLEEP") {
                                val showNightWakeOption = fallAsleepTime.hour >= 18 || fallAsleepTime.hour < 6
                                if (showNightWakeOption) isNightWake else false
                            } else null
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !isMilkAmountError
            ) {
                Text("确定")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinuteWheelPicker(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val items = (10..480 step 10).toList() // 10分钟到8小时，步长10
    val initialIndex = items.indexOf(value).coerceAtLeast(0)
    val state = rememberLazyLazyListState(initialIndex)
    
    val itemHeight = 40.dp
    val density = LocalDensity.current
    val itemHeightPx = with(density) { itemHeight.toPx() }
    
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            val centerIndex = (state.firstVisibleItemScrollOffset + itemHeightPx / 2) / itemHeightPx
            val finalIndex = state.firstVisibleItemIndex + centerIndex.toInt()
            if (finalIndex in items.indices) {
                onValueChange(items[finalIndex])
                state.animateScrollToItem(finalIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .height(itemHeight * 3)
            .width(100.dp),
        contentAlignment = Alignment.Center
    ) {
        // Selection highlight
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            shape = MaterialTheme.shapes.small
        ) {}

        LazyColumn(
            state = state,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = state),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeight)
        ) {
            items(items.size) { index ->
                val item = items[index]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$item",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = if (value == item) 20.sp else 16.sp,
                            color = if (value == item) MaterialTheme.colorScheme.primary else Color.Gray
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun rememberLazyLazyListState(initialIndex: Int): androidx.compose.foundation.lazy.LazyListState {
    return rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)
}
