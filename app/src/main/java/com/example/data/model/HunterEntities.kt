package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hunter_profile")
data class HunterProfile(
    @PrimaryKey val id: Int = 1,
    val hunterName: String = "Apex_Athlete",
    val title: String = "The Novice Trainee",
    val level: Int = 1,
    val currentXp: Long = 0L,
    val xpToNextLevel: Long = 100L,
    val statPoints: Int = 5,
    val strength: Int = 10,
    val agility: Int = 10,
    val vitality: Int = 10,
    val perception: Int = 10,
    val stamina: Int = 10,
    val currentHp: Int = 100,
    val maxHp: Int = 100,
    val currentMp: Int = 50,
    val maxMp: Int = 50,
    val gold: Int = 500,
    val totalReps: Int = 0,
    val caloriesBurned: Float = 0f,
    val dungeonsCleared: Int = 0,
    val consecutiveDays: Int = 1,
    val penaltyStrikes: Int = 0
)

@Entity(tableName = "daily_quests")
data class DailyQuest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val exerciseType: String,
    val title: String,
    val targetReps: Int,
    val currentReps: Int = 0,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false,
    val dateKey: String
)

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val category: String, // WEAPON, ARMOR, ACCESSORY, POTION, TITLE
    val rarity: String,   // COMMON, RARE, EPIC, LEGENDARY, MONARCH
    val description: String,
    val statBonusStr: Int = 0,
    val statBonusAgi: Int = 0,
    val statBonusVit: Int = 0,
    val isEquipped: Boolean = false,
    val quantity: Int = 1,
    val goldValue: Int = 100
)

@Entity(tableName = "workout_logs")
data class WorkoutLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val exerciseType: String,
    val repsCount: Int,
    val durationSeconds: Long,
    val caloriesBurned: Float,
    val timestamp: Long = System.currentTimeMillis(),
    val averageFormScore: Int = 90, // 0 - 100
    val xpGained: Int,
    val dungeonBossDefeated: String? = null
)

@Entity(tableName = "dungeon_bosses")
data class DungeonBoss(
    @PrimaryKey val id: String,
    val name: String,
    val rank: String, // E, D, C, B, A, S
    val subtitle: String,
    val maxHp: Int,
    val attackIntervalSeconds: Int,
    val bossAttackDamage: Int,
    val targetExercise: String, // PUSHUPS, SQUATS, etc.
    val xpReward: Int,
    val goldReward: Int,
    val itemRewardName: String,
    val isUnlocked: Boolean = false,
    val isDefeated: Boolean = false,
    val timesDefeated: Int = 0,
    val bestClearTimeSec: Long = 0L
)
