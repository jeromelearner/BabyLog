package com.wongchoi500.babylog.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.wongchoi500.babylog.data.BabyLog
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LogItemView(
    log: BabyLog,
    slotColors: SlotColors,
    onLongClick: () -> Unit
) {
    val displayTime = if (log.type == "SLEEP" && log.endTime != null) log.endTime else log.startTime
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = getBackgroundColor(displayTime, slotColors)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 时间显示区域放大
            Text(
                text = formatTime(displayTime),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.width(65.dp),
                color = Color.Black
            )

            Spacer(modifier = Modifier.width(12.dp))

            Icon(
                imageVector = getIconForType(log.type),
                contentDescription = log.type,
                tint = Color.Black,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                if (log.type == "SOLIDS") {
                    val pref = log.foodAmount?.substringBefore("]", "")?.removePrefix("[") ?: ""
                    val amount = log.foodAmount?.substringAfter("] ", "") ?: ""
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = translateType(log.type),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Black
                        )
                        if (pref.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = pref,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Black
                            )
                        }
                    }
                    if (!log.foodContent.isNullOrEmpty()) {
                        Text(
                            text = log.foodContent,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                    if (amount.isNotEmpty()) {
                        Text(
                            text = amount,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                } else if (log.type == "DIAPER") {
                    Text(
                        text = translateType(log.type),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    if (log.hasPee == true) {
                        val peeLabels = mapOf("Small" to "少", "Medium" to "中", "Large" to "多")
                        Text(
                            text = "小便 | ${peeLabels[log.peeAmount] ?: log.peeAmount}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                    if (log.hasPoop == true) {
                        Text(
                            text = "大便 | ${log.poopDetails}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }
                } else {
                    Text(
                        text = translateType(log.type),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = getSummary(log),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

private fun getBackgroundColor(timestamp: Long, slotColors: SlotColors): Color {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    val hour = dateTime.hour
    return when (hour) {
        in 0..5 -> Color(slotColors.slot1)
        in 6..11 -> Color(slotColors.slot2)
        in 12..17 -> Color(slotColors.slot3)
        else -> Color(slotColors.slot4)
    }
}

private fun translateType(type: String): String {
    return when (type) {
        "MILK" -> "喂奶"
        "DIAPER" -> "尿布"
        "SOLIDS" -> "辅食"
        "SLEEP" -> "睡眠"
        else -> type
    }
}

private fun getIconForType(type: String): ImageVector {
    return when (type) {
        "MILK" -> Icons.Default.WaterDrop
        "DIAPER" -> Icons.Default.BabyChangingStation
        "SOLIDS" -> Icons.Default.Restaurant
        "SLEEP" -> Icons.Default.Bedtime
        else -> Icons.Default.Info
    }
}

private fun formatTime(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
    return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

private fun getSummary(log: BabyLog): String {
    return when (log.type) {
        "MILK" -> "${log.amountMl} 毫升"
        "DIAPER" -> {
            val details = mutableListOf<String>()
            val peeLabels = mapOf("Small" to "少", "Medium" to "中", "Large" to "多")
            if (log.hasPee == true) details.add("小便 (${peeLabels[log.peeAmount] ?: log.peeAmount})")
            if (log.hasPoop == true) details.add("大便 (${log.poopDetails})")
            details.joinToString(", ")
        }
        "SOLIDS" -> "${log.foodContent}: ${log.foodAmount}"
        "SLEEP" -> {
            if (log.endTime != null) {
                val durationMin = (log.endTime - log.startTime) / (1000 * 60)
                if (durationMin >= 60) {
                    val hours = durationMin / 60.0
                    // 格式化为1位小数，如果是整数则不带小数
                    val formattedHours = if (durationMin % 60 == 0L) {
                        hours.toInt().toString()
                    } else {
                        "%.1f".format(hours)
                    }
                    val nightWakeStr = if (log.isNightWake == true) " [夜醒]" else ""
                    "时长: $formattedHours 小时$nightWakeStr"
                } else {
                    val nightWakeStr = if (log.isNightWake == true) " [夜醒]" else ""
                    "时长: ${durationMin} 分钟$nightWakeStr"
                }
            } else {
                "睡眠中，从 ${formatTime(log.startTime)} 开始"
            }
        }
        else -> ""
    }
}
