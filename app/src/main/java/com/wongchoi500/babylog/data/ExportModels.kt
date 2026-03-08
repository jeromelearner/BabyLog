package com.wongchoi500.babylog.data

import kotlinx.serialization.Serializable

@Serializable
data class BabyProfileExport(
    val nickname: String,
    val birthday: String,
    val gender: String
)

@Serializable
data class SlotColorsExport(
    val slot1: Int,
    val slot2: Int,
    val slot3: Int,
    val slot4: Int
)

@Serializable
data class ExportPackage(
    val version: Int = 1,
    val babyProfile: BabyProfileExport? = null,
    val slotColors: SlotColorsExport? = null,
    val logs: List<BabyLog> = emptyList()
)
