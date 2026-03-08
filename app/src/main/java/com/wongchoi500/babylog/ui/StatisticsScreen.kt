package com.wongchoi500.babylog.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowLeft
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wongchoi500.babylog.data.BabyLog
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val selectedMonth by viewModel.selectedMonth.collectAsStateWithLifecycle()
    val monthlyLogs by viewModel.monthlyLogs.collectAsStateWithLifecycle()
    val slotColors by viewModel.slotColors.collectAsStateWithLifecycle()
    val babyAge by viewModel.babyAge.collectAsStateWithLifecycle()
    val babyNickname = viewModel.babyNickname
    val babyInfoUpdated by viewModel.babyInfoUpdated.collectAsStateWithLifecycle()
    val nickname = remember(babyInfoUpdated) { babyNickname }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("喂奶", "尿布", "睡眠")

    var showMonthPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(
                                if (nickname.isNotEmpty()) "${nickname}的日志" else "宝宝日志",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            if (babyAge.isNotEmpty()) {
                                Text(
                                    text = babyAge,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    if (selectedMonth != YearMonth.now()) {
                        IconButton(onClick = { viewModel.updateSelectedMonth(YearMonth.now()) }) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "返回当月",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Selector
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            // Month Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.updateSelectedMonth(selectedMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowLeft, contentDescription = "上个月")
                }
                Text(
                    text = selectedMonth.format(DateTimeFormatter.ofPattern("yyyy年MM月")),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .clickable { showMonthPicker = true }
                        .padding(horizontal = 16.dp)
                )
                val isCurrentMonth = selectedMonth == YearMonth.now()
                IconButton(
                    onClick = { viewModel.updateSelectedMonth(selectedMonth.plusMonths(1)) },
                    enabled = !isCurrentMonth
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = "下个月",
                        tint = if (isCurrentMonth) Color.LightGray else LocalContentColor.current
                    )
                }
            }

            // Calendar Grid
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount > 50) {
                                viewModel.updateSelectedMonth(selectedMonth.minusMonths(1))
                            } else if (dragAmount < -50) {
                                viewModel.updateSelectedMonth(selectedMonth.plusMonths(1))
                            }
                        }
                    }
            ) {
                CalendarGrid(
                    yearMonth = selectedMonth,
                    logs = monthlyLogs,
                    categoryIndex = selectedTab,
                    slotColors = slotColors
                )
            }
        }
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = { year, month ->
                viewModel.updateSelectedMonth(YearMonth.of(year, month))
                showMonthPicker = false
            }
        )
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    logs: List<BabyLog>,
    categoryIndex: Int,
    slotColors: SlotColors
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfWeek = yearMonth.atDay(1).dayOfWeek.value % 7 // 0 for Sunday
    val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")

    val logsByDay = remember(logs, yearMonth) {
        logs.groupBy {
            Instant.ofEpochMilli(it.startTime).atZone(ZoneId.systemDefault()).toLocalDate()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
        // Week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.Gray
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Empty cells before the first day
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier.aspectRatio(0.7f))
            }

            items(daysInMonth) { dayIndex ->
                val day = dayIndex + 1
                val date = yearMonth.atDay(day)
                val dayLogs = logsByDay[date] ?: emptyList()
                
                CalendarDayCell(
                    day = day,
                    isToday = date == LocalDate.now(),
                    logs = dayLogs,
                    categoryIndex = categoryIndex,
                    slotColors = slotColors
                )
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    logs: List<BabyLog>,
    categoryIndex: Int,
    slotColors: SlotColors
) {
    Column(
        modifier = Modifier
            .aspectRatio(0.7f)
            .padding(1.dp)
            .background(
                color = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isToday) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.padding(top = 2.dp)
        )
        
        Spacer(modifier = Modifier.height(2.dp))

        when (categoryIndex) {
            0 -> FeedingStats(logs, Color(slotColors.slot1))
            1 -> DiaperStats(logs, Color(slotColors.slot3))
            2 -> SleepStats(logs, Color(slotColors.slot4))
        }
    }
}

@Composable
fun FeedingStats(logs: List<BabyLog>, color: Color) {
    val milkLogs = logs.filter { it.type == "MILK" }
    if (milkLogs.isNotEmpty()) {
        val totalAmount = milkLogs.sumOf { it.amountMl ?: 0 }
        
        StatBadge(text = "${milkLogs.size}次", color = color)
        Spacer(modifier = Modifier.height(2.dp))
        StatBadge(text = "${totalAmount}ml", color = color)
    }
}

@Composable
fun DiaperStats(logs: List<BabyLog>, color: Color) {
    val diaperLogs = logs.filter { it.type == "DIAPER" }
    if (diaperLogs.isNotEmpty()) {
        val poopCount = diaperLogs.count { it.hasPoop == true }
        
        StatBadge(text = "尿布${diaperLogs.size}次", color = color)
        Spacer(modifier = Modifier.height(2.dp))
        StatBadge(text = "大便${poopCount}次", color = color)
    }
}

@Composable
fun SleepStats(logs: List<BabyLog>, color: Color) {
    val sleepLogs = logs.filter { it.type == "SLEEP" && it.endTime != null }
    if (sleepLogs.isNotEmpty()) {
        val totalDurationMillis = sleepLogs.sumOf { it.endTime!! - it.startTime }
        val totalHours = totalDurationMillis / (1000.0 * 60 * 60)
        
        StatBadge(text = "${sleepLogs.size}觉", color = color)
        Spacer(modifier = Modifier.height(2.dp))
        StatBadge(text = String.format(Locale.getDefault(), "%.1fh", totalHours), color = color)
    }
}

@Composable
fun StatBadge(text: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
            textAlign = TextAlign.Center,
            maxLines = 1,
            color = Color.Black,
            modifier = Modifier.padding(vertical = 1.dp)
        )
    }
}

@Composable
fun MonthYearPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableIntStateOf(currentMonth.monthValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择年月") },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Simplified selection
                NumberPicker(
                    value = selectedYear,
                    range = (currentMonth.year - 10)..(LocalDate.now().year),
                    onValueChange = { selectedYear = it }
                )
                NumberPicker(
                    value = selectedMonth,
                    range = 1..12,
                    onValueChange = { selectedMonth = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val newMonth = YearMonth.of(selectedYear, selectedMonth)
                if (!newMonth.isAfter(YearMonth.now())) {
                    onConfirm(selectedYear, selectedMonth)
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun NumberPicker(
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = { if (value < range.last) onValueChange(value + 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, modifier = Modifier.rotate(90f), contentDescription = "增加")
        }
        Text(text = value.toString(), style = MaterialTheme.typography.titleLarge)
        IconButton(onClick = { if (value > range.first) onValueChange(value - 1) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowLeft, modifier = Modifier.rotate(-90f), contentDescription = "减少")
        }
    }
}
