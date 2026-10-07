package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.FitnessQuest
import kotlinx.coroutines.flow.Flow

@Dao
interface FitnessQuestDao {

    @Query("SELECT * FROM fitness_quests WHERE dateKey = :dateKey ORDER BY id ASC")
    fun getQuestsForDateFlow(dateKey: String): Flow<List<FitnessQuest>>

    @Query("SELECT * FROM fitness_quests WHERE dateKey = :dateKey")
    suspend fun getQuestsForDate(dateKey: String): List<FitnessQuest>

    @Query("SELECT * FROM fitness_quests WHERE id = :id LIMIT 1")
    suspend fun getQuestById(id: Long): FitnessQuest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<FitnessQuest>)

    @Update
    suspend fun updateQuest(quest: FitnessQuest)

    @Query("""
        UPDATE fitness_quests 
        SET currentProgress = MIN(targetValue, currentProgress + :progress),
            isCompleted = (currentProgress + :progress >= targetValue)
        WHERE category = :category AND dateKey = :dateKey
    """)
    suspend fun addProgressByCategory(category: String, dateKey: String, progress: Int)

    @Query("UPDATE fitness_quests SET isRewardClaimed = 1 WHERE id = :id")
    suspend fun markRewardClaimed(id: Long)

    @Query("DELETE FROM fitness_quests")
    suspend fun clearFitnessQuests()
}
