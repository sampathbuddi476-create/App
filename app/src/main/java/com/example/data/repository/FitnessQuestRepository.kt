package com.example.data.repository

import com.example.data.db.FitnessQuestDao
import com.example.data.db.HunterDao
import com.example.data.model.FitnessQuest
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.InventoryItem
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class QuestClaimResult(
    val success: Boolean,
    val previousLevel: Int = 1,
    val newLevel: Int = 1,
    val previousRank: HunterRank = HunterRank.E_RANK,
    val newRank: HunterRank = HunterRank.E_RANK,
    val statPointsGained: Int = 0,
    val xpGained: Int = 0,
    val didLevelUp: Boolean = false,
    val didRankUp: Boolean = false,
    val questTitle: String = ""
)

class FitnessQuestRepository(
    private val questDao: FitnessQuestDao,
    private val hunterDao: HunterDao
) {

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    }

    fun getDailyFitnessQuestsFlow(): Flow<List<FitnessQuest>> {
        val today = getTodayKey()
        return questDao.getQuestsForDateFlow(today)
    }

    suspend fun ensureDailyFitnessQuestsInitialized(): List<FitnessQuest> {
        val today = getTodayKey()
        val existing = questDao.getQuestsForDate(today)
        if (existing.isNotEmpty()) {
            return existing
        }

        val defaultQuests = listOf(
            FitnessQuest(
                title = "Complete 20 squats",
                description = "Build raw leg strength and stability. Proper depth is required by the System.",
                category = "SQUATS",
                targetValue = 20,
                targetUnit = "reps",
                xpReward = 150,
                statPointsReward = 1,
                goldReward = 200,
                itemRewardName = "Low-Grade Strength Elixir",
                dateKey = today
            ),
            FitnessQuest(
                title = "Run for 15 minutes",
                description = "Enhance cardio stamina and oxygen capacity through continuous endurance running.",
                category = "CARDIO",
                targetValue = 15,
                targetUnit = "minutes",
                xpReward = 200,
                statPointsReward = 2,
                goldReward = 350,
                itemRewardName = "Agility Boost Rune",
                dateKey = today
            ),
            FitnessQuest(
                title = "Complete 30 push-ups",
                description = "Upper body pectoral and tricep conditioning to increase physical impact force.",
                category = "PUSHUPS",
                targetValue = 30,
                targetUnit = "reps",
                xpReward = 180,
                statPointsReward = 1,
                goldReward = 250,
                itemRewardName = "Hunter Iron Bracer",
                dateKey = today
            ),
            FitnessQuest(
                title = "Perform 40 sit-ups",
                description = "Core flexion regimen to strengthen abdominal defense and combat balance.",
                category = "SITUPS",
                targetValue = 40,
                targetUnit = "reps",
                xpReward = 180,
                statPointsReward = 1,
                goldReward = 250,
                itemRewardName = null,
                dateKey = today
            ),
            FitnessQuest(
                title = "Execute 50 shadow punches",
                description = "Rapid strike training to hone kinetic speed and reflex sharpness.",
                category = "SHADOW_PUNCHES",
                targetValue = 50,
                targetUnit = "strikes",
                xpReward = 120,
                statPointsReward = 1,
                goldReward = 200,
                itemRewardName = null,
                dateKey = today
            )
        )

        questDao.insertQuests(defaultQuests)
        return defaultQuests
    }

    suspend fun addProgressByCategory(category: String, progress: Int) {
        val today = getTodayKey()
        questDao.addProgressByCategory(category.uppercase(), today, progress)
    }

    suspend fun claimQuestRewardResult(questId: Long): QuestClaimResult {
        val quest = questDao.getQuestById(questId) ?: return QuestClaimResult(success = false)
        if (!quest.isCompleted || quest.isRewardClaimed) return QuestClaimResult(success = false)

        val profile = hunterDao.getHunterProfile() ?: HunterProfile()
        val prevLevel = profile.level
        val prevRank = HunterRank.fromLevel(prevLevel)

        // 1. Mark claimed
        questDao.markRewardClaimed(questId)

        // 2. Award XP and check Level Up
        var newXp = profile.currentXp + quest.xpReward
        var level = profile.level
        var xpToNext = profile.xpToNextLevel
        var statPoints = profile.statPoints + quest.statPointsReward

        while (newXp >= xpToNext) {
            newXp -= xpToNext
            level += 1
            statPoints += 3
            xpToNext = (xpToNext * 1.35).toLong()
        }

        val newRank = HunterRank.fromLevel(level)
        val didLevelUp = level > prevLevel
        val didRankUp = newRank != prevRank

        val updatedProfile = profile.copy(
            level = level,
            currentXp = newXp,
            xpToNextLevel = xpToNext,
            statPoints = statPoints,
            gold = profile.gold + quest.goldReward,
            currentHp = profile.maxHp,
            currentMp = profile.maxMp
        )
        hunterDao.insertOrUpdateProfile(updatedProfile)

        // 3. Award item if specified
        if (!quest.itemRewardName.isNullOrBlank()) {
            val rewardItem = InventoryItem(
                name = quest.itemRewardName,
                category = "ACCESSORY",
                rarity = "RARE",
                description = "Earned by completing '${quest.title}'.",
                statBonusStr = 2,
                statBonusAgi = 2,
                isEquipped = false,
                quantity = 1,
                goldValue = quest.goldReward
            )
            hunterDao.insertItem(rewardItem)
        }

        return QuestClaimResult(
            success = true,
            previousLevel = prevLevel,
            newLevel = level,
            previousRank = prevRank,
            newRank = newRank,
            statPointsGained = statPoints - profile.statPoints,
            xpGained = quest.xpReward,
            didLevelUp = didLevelUp,
            didRankUp = didRankUp,
            questTitle = quest.title
        )
    }

    suspend fun claimQuestReward(questId: Long): Boolean {
        return claimQuestRewardResult(questId).success
    }
}
