package com.wongchoi500.babylog.ui

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.wongchoi500.babylog.data.BabyLog
import com.wongchoi500.babylog.data.BabyProfileExport
import com.wongchoi500.babylog.data.ExportPackage
import com.wongchoi500.babylog.data.LogRepository
import com.wongchoi500.babylog.data.SlotColorsExport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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

    val shouldShowWelcome: Boolean
        get() = !prefs.getBoolean("welcome_completed", false)

    fun markWelcomeCompleted() {
        prefs.edit().putBoolean("welcome_completed", true).apply()
    }

    val babyNickname: String
        get() = prefs.getString("baby_nickname", "") ?: ""

    val babyBirthday: String
        get() = prefs.getString("baby_birthday", "") ?: ""

    private val _babyAge = MutableStateFlow(calculateBabyAge())
    val babyAge: StateFlow<String> = _babyAge.asStateFlow()

    private fun calculateBabyAge(): String {
        val birthdayStr = babyBirthday
        if (birthdayStr.isEmpty()) return ""
        return try {
            val birthDate = LocalDate.parse(birthdayStr)
            val currentDate = LocalDate.now()
            if (birthDate.isAfter(currentDate)) return ""
            
            val period = Period.between(birthDate, currentDate)
            when {
                period.years == 0 && period.months == 0 -> {
                    val days = ChronoUnit.DAYS.between(birthDate, currentDate)
                    "${days}天"
                }
                period.years == 0 -> {
                    "${period.months}月${period.days}天"
                }
                else -> {
                    "${period.years}年${period.months}月"
                }
            }
        } catch (e: Exception) {
            ""
        }
    }

    val babyGender: String
        get() = prefs.getString("baby_gender", "") ?: ""

    private val _babyInfoUpdated = MutableStateFlow(0)
    val babyInfoUpdated: StateFlow<Int> = _babyInfoUpdated.asStateFlow()

    fun saveBabyInfo(nickname: String, birthday: String, gender: String) {
        prefs.edit()
            .putString("baby_nickname", nickname)
            .putString("baby_birthday", birthday)
            .putString("baby_gender", gender)
            .apply()
        _babyAge.value = calculateBabyAge()
        _babyInfoUpdated.value++
    }

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

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    fun updateSelectedMonth(month: YearMonth) {
        if (month.isAfter(YearMonth.now())) return
        _selectedMonth.value = month
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyLogs = _selectedMonth
        .flatMapLatest { yearMonth ->
            val startOfMonth = yearMonth.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfMonth = yearMonth.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.getLogsByDateRange(startOfMonth, endOfMonth)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

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

    fun exportData(context: Context, onResult: (Uri?) -> Unit) {
        viewModelScope.launch {
            try {
                val logs = repository.getAllLogsList()
                val profile = BabyProfileExport(babyNickname, babyBirthday, babyGender)
                val colors = SlotColorsExport(
                    _slotColors.value.slot1,
                    _slotColors.value.slot2,
                    _slotColors.value.slot3,
                    _slotColors.value.slot4
                )
                val exportPackage = ExportPackage(
                    babyProfile = profile,
                    slotColors = colors,
                    logs = logs
                )

                val jsonString = Json.encodeToString(exportPackage)
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")
                val timestamp = LocalDateTime.now().format(formatter)
                val fileName = "babylog_backup_$timestamp.json"

                // 清理旧的备份文件
                context.cacheDir.listFiles()?.forEach {
                    if (it.name.startsWith("babylog_backup") && it.name.endsWith(".json")) {
                        it.delete()
                    }
                }

                val file = File(context.cacheDir, fileName)
                file.writeText(jsonString)

                val uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
                onResult(uri)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }
    }

    fun importData(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                if (jsonString == null) {
                    onResult(false)
                    return@launch
                }

                val importPackage: ExportPackage = Json.decodeFromString(jsonString)

                // 1. 合并宝宝信息
                if (importPackage.babyProfile != null && babyNickname.isEmpty()) {
                    saveBabyInfo(
                        importPackage.babyProfile.nickname,
                        importPackage.babyProfile.birthday,
                        importPackage.babyProfile.gender
                    )
                }

                // 2. 合并颜色设置
                val colors = importPackage.slotColors
                if (colors != null) {
                    prefs.edit()
                        .putInt("slot1", colors.slot1)
                        .putInt("slot2", colors.slot2)
                        .putInt("slot3", colors.slot3)
                        .putInt("slot4", colors.slot4)
                        .apply()
                    _slotColors.value = loadColors()
                }

                // 3. 合并日志
                val existingLogs = repository.getAllLogsList()
                val importedLogs = importPackage.logs
                val newLogs = importedLogs.filter { newLog ->
                    existingLogs.none { it.type == newLog.type && it.startTime == newLog.startTime }
                }

                newLogs.forEach {
                    repository.insert(it.copy(id = 0))
                }

                onResult(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(false)
            }
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
