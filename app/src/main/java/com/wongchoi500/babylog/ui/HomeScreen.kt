package com.wongchoi500.babylog.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wongchoi500.babylog.data.BabyLog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val logs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val slotColors by viewModel.slotColors.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showColorSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("宝宝日志 - 时间轴") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "更多选项")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("颜色设置") },
                                onClick = {
                                    showMenu = false
                                    showColorSettings = true
                                }
                            )
                        }
                    }
                )
                // 日期选择条
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "选择日期",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (selectedDate != LocalDate.now()) {
                            Spacer(modifier = Modifier.weight(1f))
                            TextButton(onClick = { viewModel.updateSelectedDate(LocalDate.now()) }) {
                                Text("回到今天")
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "添加记录")
            }
        }
    ) { paddingValues ->
        if (logs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text("暂无记录。点击 + 开始。", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(logs, key = { it.id }) { log ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    LogItemView(
                        log = log,
                        slotColors = slotColors,
                        onLongClick = { showDeleteConfirm = true }
                    )

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("删除记录") },
                            text = { Text("确定要删除这条记录吗？") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteLog(log)
                                    showDeleteConfirm = false
                                }) {
                                    Text("删除")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("取消")
                                }
                            }
                        )
                    }
                }
            }
        }

        if (showAddDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showAddDialog = false }
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .wrapContentHeight()
                ) {
                    AddLogScreen(
                        onSave = { newLog ->
                            viewModel.addLog(newLog)
                            showAddDialog = false
                        },
                        onCancel = { showAddDialog = false }
                    )
                }
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
                            val date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.updateSelectedDate(date)
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

        if (showColorSettings) {
            ColorSettingsDialog(
                slotColors = slotColors,
                onColorUpdate = { slot, color -> viewModel.updateSlotColor(slot, color) },
                onDismiss = { showColorSettings = false }
            )
        }
    }
}

@Composable
fun ColorSettingsDialog(
    slotColors: SlotColors,
    onColorUpdate: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    val colorOptions = listOf(
        0xFFE0F2F1, 0xFFFFF9C4, 0xFFE3F2FD, 0xFFF3E5F5,
        0xFFFCE4EC, 0xFFFFF3E0, 0xFFF1F8E9, 0xFFF5F5F5,
        0xFFFFEBEE, 0xFFE8F5E9, 0xFFE1F5FE, 0xFFFFF8E1,
        0xFFEFEBE9, 0xFFFAFAFA, 0xFFECEFF1, 0xFFFFCCBC
    ).map { it.toInt() }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("颜色设置", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(16.dp))

                val slots = listOf(
                    "0:00 - 6:00" to 1,
                    "6:00 - 12:00" to 2,
                    "12:00 - 18:00" to 3,
                    "18:00 - 24:00" to 4
                )

                slots.forEach { (label, slot) ->
                    Text(label, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val currentColor = when(slot) {
                        1 -> slotColors.slot1
                        2 -> slotColors.slot2
                        3 -> slotColors.slot3
                        else -> slotColors.slot4
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colorOptions.forEach { colorInt ->
                            val color = Color(colorInt)
                            Surface(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onColorUpdate(slot, colorInt) },
                                color = color,
                                shape = CircleShape,
                                border = if (currentColor == colorInt) {
                                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                } else null
                            ) {}
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}
