package com.wongchoi500.babylog.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    viewModel: HomeViewModel,
    onNavigateToBabyInfo: () -> Unit = {}
) {
    val logs by viewModel.filteredLogs.collectAsStateWithLifecycle()
    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummary.collectAsStateWithLifecycle()
    val slotColors by viewModel.slotColors.collectAsStateWithLifecycle()
    val babyNickname = viewModel.babyNickname
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showColorSettings by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Scaffold(
        modifier = Modifier.drawBehind {
            // 在背景画一些可爱的淡色圆圈
            drawCircle(
                color = primaryColor.copy(alpha = 0.05f),
                radius = 400f,
                center = Offset(size.width * 0.9f, size.height * 0.1f)
            )
            drawCircle(
                color = secondaryColor.copy(alpha = 0.05f),
                radius = 300f,
                center = Offset(size.width * 0.1f, size.height * 0.8f)
            )
            drawCircle(
                color = tertiaryColor.copy(alpha = 0.05f),
                radius = 200f,
                center = Offset(size.width * 0.5f, size.height * 0.5f)
            )
        },
        topBar = {
            Column {
                TopAppBar(
                    title = { 
                        Text(
                            if (babyNickname.isNotEmpty()) "${babyNickname}的日志" else "宝宝日志",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        ) 
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary
                    ),
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                if (selectedDate != LocalDate.now()) {
                                    Text(
                                        text = "回到今天",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                        modifier = Modifier.clickable { viewModel.updateSelectedDate(LocalDate.now()) }
                                    )
                                }
                            }
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "选择日期",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "更多选项",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("宝宝信息") },
                                    onClick = {
                                        showMenu = false
                                        onNavigateToBabyInfo()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("颜色设置") },
                                    onClick = {
                                        showMenu = false
                                        showColorSettings = true
                                    }
                                )
                            }
                        }
                    }
                )
                // 汇总栏
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SummaryItem(
                                icon = Icons.Default.LocalDrink,
                                text = "${dailySummary.totalMilkMl}ml"
                            )
                            SummaryItem(
                                icon = Icons.Default.ChildCare,
                                text = "${dailySummary.diaperCount}次"
                            )
                            SummaryItem(
                                icon = Icons.Default.Restaurant,
                                text = "${dailySummary.solidsCount}次"
                            )
                            SummaryItem(
                                icon = Icons.Default.Bedtime,
                                text = String.format("%.1fh", dailySummary.totalSleepDurationHours)
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
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
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChildCare,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "记录宝宝的成长点滴",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    Text(
                        "点击下方的 + 号开始吧",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
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

@Composable
fun SummaryItem(icon: ImageVector, text: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    }
}
