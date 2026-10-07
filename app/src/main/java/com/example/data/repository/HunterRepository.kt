package com.example.data.repository

import com.example.data.db.HunterDao
import com.example.data.model.DailyQuest
import com.example.data.model.DungeonBoss
import com.example.data.model.ExerciseType
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.InventoryItem
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class HunterRepository(private val dao: HunterDao) {

    val hunterProfileFlow: Flow<HunterProfile?> = dao.getHunterProfileFlow()
    val allDungeonsFlow: Flow<List<DungeonBoss>> = dao.getAllDungeonsFlow()
    val allInventoryItemsFlow: Flow<List<InventoryItem>> = dao.getAllInventoryItemsFlow()
    val recentWorkoutLogsFlow: Flow<List<WorkoutLog>> = dao.getRecentWorkoutLogsFlow()

    fun getDailyQuestsFlow(): Flow<List<DailyQuest>> {
        val todayKey = getTodayKey()
        return dao.getDailyQuestsFlow(todayKey)
    }

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    suspend fun ensureDailyQuestsInitialized() {
        val today = getTodayKey()
        val existing = dao.getDailyQuests(today)
        if (existing.isEmpty()) {
            val defaultQuests = listOf(
                DailyQuest(exerciseType = "PUSHUPS", title = "Push-ups Training", targetReps = 100, currentReps = 0, dateKey = today),
                DailyQuest(exerciseType = "SITUPS", title = "Sit-ups Core Conditioning", targetReps = 100, currentReps = 0, dateKey = today),
                DailyQuest(exerciseType = "SQUATS", title = "Squats Leg Strengthening", targetReps = 100, currentReps = 0, dateKey = today),
                DailyQuest(exerciseType = "JUMPING_JACKS", title = "Cardio Burst / 10km Run", targetReps = 100, currentReps = 0, dateKey = today)
            )
            dao.insertQuests(defaultQuests)
        }
    }

    suspend fun allocateStatPoint(statName: String): Boolean {
        val profile = dao.getHunterProfile() ?: return false
        if (profile.statPoints <= 0) return false

        val updated = when (statName.uppercase()) {
            "STR" -> profile.copy(
                strength = profile.strength + 1,
                statPoints = profile.statPoints - 1
            )
            "AGI" -> profile.copy(
                agility = profile.agility + 1,
                statPoints = profile.statPoints - 1
            )
            "VIT" -> {
                val newVit = profile.vitality + 1
                val newMaxHp = 100 + (newVit - 10) * 15
                profile.copy(
                    vitality = newVit,
                    maxHp = newMaxHp,
                    currentHp = (profile.currentHp + 15).coerceAtMost(newMaxHp),
                    statPoints = profile.statPoints - 1
                )
            }
            "PER" -> profile.copy(
                perception = profile.perception + 1,
                statPoints = profile.statPoints - 1
            )
            "STA" -> {
                val newSta = profile.stamina + 1
                val newMaxMp = 50 + (newSta - 10) * 8
                profile.copy(
                    stamina = newSta,
                    maxMp = newMaxMp,
                    currentMp = (profile.currentMp + 8).coerceAtMost(newMaxMp),
                    statPoints = profile.statPoints - 1
                )
            }
            else -> profile
        }

        dao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun recordWorkoutReps(
        exercise: ExerciseType,
        reps: Int,
        durationSeconds: Long,
        formScoreAvg: Int,
        dungeonId: String? = null
    ): Pair<Int, Boolean> { // returns (xpGained, didLevelUp)
        if (reps <= 0) return Pair(0, false)

        val profile = dao.getHunterProfile() ?: HunterProfile()
        val today = getTodayKey()

        // Form bonus: formScore > 85 yields +20% bonus XP
        val formBonusMult = if (formScoreAvg >= 85) 1.2f else 1.0f
        val xpGained = (reps * exercise.xpPerRep * formBonusMult).roundToInt()
        val cals = reps * exercise.calPerRep

        // Update Daily Quest
        dao.addRepsToDailyQuest(exercise.name, today, reps)

        // Log workout
        dao.insertWorkoutLog(
            WorkoutLog(
                exerciseType = exercise.name,
                repsCount = reps,
                durationSeconds = durationSeconds,
                caloriesBurned = cals,
                averageFormScore = formScoreAvg,
                xpGained = xpGained,
                dungeonBossDefeated = dungeonId
            )
        )

        // Apply XP and Check Level Up
        var newXp = profile.currentXp + xpGained
        var level = profile.level
        var xpToNext = profile.xpToNextLevel
        var statPoints = profile.statPoints
        var didLevelUp = false

        while (newXp >= xpToNext) {
            newXp -= xpToNext
            level += 1
            statPoints += 3
            xpToNext = (xpToNext * 1.35).toLong()
            didLevelUp = true
        }

        val rank = HunterRank.fromLevel(level)
        val updatedTitle = if (level >= 100) "Shadow Monarch"
        else if (level >= 70) "S-Rank Sovereign"
        else if (level >= 50) "High Orc Slayer"
        else if (level >= 20) "Demon Hunter"
        else profile.title

        val updatedProfile = profile.copy(
            level = level,
            currentXp = newXp,
            xpToNextLevel = xpToNext,
            statPoints = statPoints,
            title = updatedTitle,
            totalReps = profile.totalReps + reps,
            caloriesBurned = profile.caloriesBurned + cals,
            gold = profile.gold + (reps * 2)
        )
        dao.insertOrUpdateProfile(updatedProfile)

        return Pair(xpGained, didLevelUp)
    }

    suspend fun claimQuestReward(quest: DailyQuest): Boolean {
        if (!quest.isCompleted || quest.isRewardClaimed) return false
        val profile = dao.getHunterProfile() ?: return false

        // Mark claimed
        dao.updateQuest(quest.copy(isRewardClaimed = true))

        // Give reward: +3 Stat points, full HP heal, gold, loot item
        val updatedProfile = profile.copy(
            statPoints = profile.statPoints + 3,
            currentHp = profile.maxHp,
            currentMp = profile.maxMp,
            gold = profile.gold + 500
        )
        dao.insertOrUpdateProfile(updatedProfile)

        // Random loot gift
        val rewardItem = InventoryItem(
            name = "Architect's Blessing Potion",
            category = "POTION",
            rarity = "EPIC",
            description = "Reward for diligent hunters who never miss daily quests. Completely restores stamina.",
            statBonusVit = 2,
            isEquipped = false,
            quantity = 1,
            goldValue = 400
        )
        dao.insertItem(rewardItem)
        return true
    }

    suspend fun completeDungeonBoss(boss: DungeonBoss, durationSec: Long) {
        val profile = dao.getHunterProfile() ?: return

        // Update Boss record
        val updatedBoss = boss.copy(
            isDefeated = true,
            timesDefeated = boss.timesDefeated + 1,
            bestClearTimeSec = if (boss.bestClearTimeSec == 0L) durationSec else minOf(boss.bestClearTimeSec, durationSec)
        )
        dao.updateDungeon(updatedBoss)

        // Unlock next dungeon
        val allDungeons = dao.getDungeonById(boss.id) // check list
        unlockNextDungeonAfter(boss.id)

        // Give Rewards
        var newXp = profile.currentXp + boss.xpReward
        var level = profile.level
        var xpToNext = profile.xpToNextLevel
        var statPoints = profile.statPoints

        while (newXp >= xpToNext) {
            newXp -= xpToNext
            level += 1
            statPoints += 3
            xpToNext = (xpToNext * 1.35).toLong()
        }

        val updatedProfile = profile.copy(
            level = level,
            currentXp = newXp,
            xpToNextLevel = xpToNext,
            statPoints = statPoints,
            gold = profile.gold + boss.goldReward,
            dungeonsCleared = profile.dungeonsCleared + 1
        )
        dao.insertOrUpdateProfile(updatedProfile)

        // Give boss weapon/artifact
        val rewardItem = InventoryItem(
            name = boss.itemRewardName,
            category = "WEAPON",
            rarity = if (boss.rank == "S") "MONARCH" else if (boss.rank == "A") "LEGENDARY" else "EPIC",
            description = "Forged from the remnants of ${boss.name}. Imbued with immense mana.",
            statBonusStr = when (boss.rank) { "S" -> 25; "A" -> 15; "B" -> 10; "C" -> 7; else -> 4 },
            statBonusAgi = when (boss.rank) { "S" -> 20; "A" -> 12; "B" -> 8; else -> 3 },
            isEquipped = false,
            quantity = 1,
            goldValue = boss.goldReward
        )
        dao.insertItem(rewardItem)
    }

    private suspend fun unlockNextDungeonAfter(currentBossId: String) {
        val progression = listOf("boss_goblin", "boss_statue", "boss_kasaka", "boss_cerberus", "boss_igris", "boss_beru")
        val currentIndex = progression.indexOf(currentBossId)
        if (currentIndex in 0 until progression.size - 1) {
            val nextId = progression[currentIndex + 1]
            val nextBoss = dao.getDungeonById(nextId)
            if (nextBoss != null && !nextBoss.isUnlocked) {
                dao.updateDungeon(nextBoss.copy(isUnlocked = true))
            }
        }
    }

    suspend fun usePotion(item: InventoryItem): Boolean {
        if (item.category != "POTION" || item.quantity <= 0) return false
        val profile = dao.getHunterProfile() ?: return false

        val healedHp = (profile.currentHp + 50).coerceAtMost(profile.maxHp)
        val healedMp = (profile.currentMp + 25).coerceAtMost(profile.maxMp)
        dao.insertOrUpdateProfile(profile.copy(currentHp = healedHp, currentMp = healedMp))

        if (item.quantity > 1) {
            dao.updateItem(item.copy(quantity = item.quantity - 1))
        } else {
            dao.deleteItem(item.id)
        }
        return true
    }

    suspend fun toggleEquipItem(item: InventoryItem) {
        dao.updateItem(item.copy(isEquipped = !item.isEquipped))
    }

    suspend fun updateProfile(profile: HunterProfile) {
        dao.insertOrUpdateProfile(profile)
    }
}
