package com.wongchoi500.babylog.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "baby_logs")
@Serializable
data class BabyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String, // MILK, DIAPER, SOLIDS, SLEEP
    val startTime: Long,
    val endTime: Long? = null,
    val amountMl: Int? = null,
    val hasPee: Boolean? = null,
    val peeAmount: String? = null, // Small, Medium, Large
    val hasPoop: Boolean? = null,
    val poopDetails: String? = null,
    val foodContent: String? = null,
    val foodAmount: String? = null,
    val isNightWake: Boolean? = null
)
