package com.wongchoi500.babylog.ui

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wongchoi500.babylog.data.BabyLog
import com.wongchoi500.babylog.data.LogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class SlotColors(
    val slot1: Int, // 0-6
    val slot2: Int, // 6-12
    val slot3: Int, // 12-18
    val slot4: Int  // 18-24
)

data class DailySummary(
    val totalMilkMl: Int = 0,
    val milkCount: Int = 0,
    val diaperCount: Int = 0,
    val solidsCount: Int = 0,
    val totalSleepDurationHours: Double = 0.0
)

class HomeViewModel(
    private val repository: LogRepository,
    private val prefs: SharedPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // 默认颜色
    private val defaultColors = SlotColors(
        slot1 = 0xFFE0F2F1.toInt(),
        slot2 = 0xFFFFF9C4.toInt(),
        slot3 = 0xFFE3F2FD.toInt(),
        slot4 = 0xFFF3E5F5.toInt()
    )

    private val _slotColors = MutableStateFlow(loadColors())
    val slotColors: StateFlow<SlotColors> = _slotColors.asStateFlow()

    private fun loadColors(): SlotColors {
        return SlotColors(
            slot1 = prefs.getInt("slot1", defaultColors.slot1),
            slot2 = prefs.getInt("slot2", defaultColors.slot2),
            slot3 = prefs.getInt("slot3", defaultColors.slot3),
            slot4 = prefs.getInt("slot4", defaultColors.slot4)
        )
    }

    fun updateSlotColor(slot: Int, color: Int) {
        val current = _slotColors.value
        val updated = when (slot) {
            1 -> current.copy(slot1 = color)
            2 -> current.copy(slot2 = color)
            3 -> current.copy(slot3 = color)
            4 -> current.copy(slot4 = color)
            else -> current
        }
        _slotColors.value = updated
        prefs.edit().putInt("slot$slot", color).apply()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredLogs: StateFlow<List<BabyLog>> = _selectedDate
        .flatMapLatest { date ->
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.getLogsByDateRange(startOfDay, endOfDay)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dailySummary: StateFlow<DailySummary> = filteredLogs
        .map { logs ->
            var milkMl = 0
            var milkCnt = 0
            var diaperCnt = 0
            var solidsCnt = 0
            var sleepMs = 0L

            logs.forEach { log ->
                when (log.type) {
                    "MILK" -> {
                        milkMl += log.amountMl ?: 0
                        milkCnt++
                    }
                    "DIAPER" -> diaperCnt++
                    "SOLIDS" -> solidsCnt++
                    "SLEEP" -> {
                        if (log.endTime != null) {
                            sleepMs += (log.endTime - log.startTime)
                        }
                    }
                }
            }

            DailySummary(
                totalMilkMl = milkMl,
                milkCount = milkCnt,
                diaperCount = diaperCnt,
                solidsCount = solidsCnt,
                totalSleepDurationHours = sleepMs.toDouble() / (1000 * 60 * 60)
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DailySummary()
        )

    fun updateSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addLog(log: BabyLog) {
        viewModelScope.launch {
            repository.insert(log)
        }
    }

    fun deleteLog(log: BabyLog) {
        viewModelScope.launch {
            repository.delete(log)
        }
    }

    companion object {
        fun provideFactory(
            repository: LogRepository,
            prefs: SharedPreferences
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                return HomeViewModel(repository, prefs) as T
            }
        }
    }
}
