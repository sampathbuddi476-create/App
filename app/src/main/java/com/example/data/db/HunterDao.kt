package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DailyQuest
import com.example.data.model.DungeonBoss
import com.example.data.model.HunterProfile
import com.example.data.model.InventoryItem
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.flow.Flow

@Dao
interface HunterDao {

    // --- Profile ---
    @Query("SELECT * FROM hunter_profile WHERE id = 1 LIMIT 1")
    fun getHunterProfileFlow(): Flow<HunterProfile?>

    @Query("SELECT * FROM hunter_profile WHERE id = 1 LIMIT 1")
    suspend fun getHunterProfile(): HunterProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: HunterProfile)

    // --- Daily Quests ---
    @Query("SELECT * FROM daily_quests WHERE dateKey = :dateKey ORDER BY id ASC")
    fun getDailyQuestsFlow(dateKey: String): Flow<List<DailyQuest>>

    @Query("SELECT * FROM daily_quests WHERE dateKey = :dateKey")
    suspend fun getDailyQuests(dateKey: String): List<DailyQuest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<DailyQuest>)

    @Update
    suspend fun updateQuest(quest: DailyQuest)

    @Query("UPDATE daily_quests SET currentReps = MIN(targetReps, currentReps + :addedReps), isCompleted = (currentReps + :addedReps >= targetReps) WHERE exerciseType = :exerciseType AND dateKey = :dateKey")
    suspend fun addRepsToDailyQuest(exerciseType: String, dateKey: String, addedReps: Int)

    // --- Dungeons ---
    @Query("SELECT * FROM dungeon_bosses ORDER BY maxHp ASC")
    fun getAllDungeonsFlow(): Flow<List<DungeonBoss>>

    @Query("SELECT * FROM dungeon_bosses WHERE id = :id LIMIT 1")
    suspend fun getDungeonById(id: String): DungeonBoss?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDungeons(dungeons: List<DungeonBoss>)

    @Update
    suspend fun updateDungeon(boss: DungeonBoss)

    // --- Inventory ---
    @Query("SELECT * FROM inventory_items ORDER BY isEquipped DESC, rarity DESC, id ASC")
    fun getAllInventoryItemsFlow(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItem>)

    @Update
    suspend fun updateItem(item: InventoryItem)

    @Query("DELETE FROM inventory_items WHERE id = :id")
    suspend fun deleteItem(id: Int)

    // --- Workout Logs ---
    @Query("SELECT * FROM workout_logs ORDER BY timestamp DESC LIMIT 50")
    fun getRecentWorkoutLogsFlow(): Flow<List<WorkoutLog>>

    @Insert
    suspend fun insertWorkoutLog(log: WorkoutLog): Long

    @Query("DELETE FROM hunter_profile")
    suspend fun clearProfile()

    @Query("DELETE FROM daily_quests")
    suspend fun clearDailyQuests()

    @Query("DELETE FROM inventory_items")
    suspend fun clearInventory()

    @Query("DELETE FROM workout_logs")
    suspend fun clearWorkoutLogs()
}
