package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fitness_quests")
data class FitnessQuest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val category: String, // SQUATS, CARDIO, PUSHUPS, SITUPS, SHADOW_PUNCHES
    val targetValue: Int,
    val targetUnit: String, // "reps", "minutes", "km"
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false,
    val xpReward: Int = 150,
    val statPointsReward: Int = 1,
    val goldReward: Int = 250,
    val itemRewardName: String? = null,
    val dateKey: String
)
