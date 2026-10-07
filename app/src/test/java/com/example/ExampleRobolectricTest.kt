package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.model.ExerciseType
import com.example.data.model.FitnessQuest
import com.example.data.model.HunterRank
import com.example.data.repository.FitnessQuestRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: FitnessQuestRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FitnessQuestRepository(db.fitnessQuestDao(), db.hunterDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Solo Leveling", appName)
    }

    @Test
    fun `fitness quest repository initializes collection of daily fitness quests`() = runBlocking {
        val quests = repository.ensureDailyFitnessQuestsInitialized()
        assertTrue(quests.isNotEmpty())

        val squatsQuest = quests.find { it.title.contains("squats", ignoreCase = true) }
        assertNotNull("Should contain task like 'Complete 20 squats'", squatsQuest)
        assertEquals(20, squatsQuest?.targetValue)

        val runQuest = quests.find { it.title.contains("Run", ignoreCase = true) }
        assertNotNull("Should contain task like 'Run for 15 minutes'", runQuest)
        assertEquals(15, runQuest?.targetValue)
        assertEquals("minutes", runQuest?.targetUnit)
    }

    @Test
    fun `fitness quest progress and reward claiming`() = runBlocking {
        repository.ensureDailyFitnessQuestsInitialized()
        val initialQuests = repository.getDailyFitnessQuestsFlow().first()
        val squatsQuest = initialQuests.first { it.category == "SQUATS" }

        // Add progress
        repository.addProgressByCategory("SQUATS", 20)

        val updatedQuests = repository.getDailyFitnessQuestsFlow().first()
        val completedSquatsQuest = updatedQuests.first { it.id == squatsQuest.id }
        assertTrue(completedSquatsQuest.isCompleted)

        // Claim reward result
        val claimResult = repository.claimQuestRewardResult(completedSquatsQuest.id)
        assertTrue(claimResult.success)
        assertTrue(claimResult.xpGained > 0)

        val claimedQuests = repository.getDailyFitnessQuestsFlow().first()
        val finalSquatsQuest = claimedQuests.first { it.id == squatsQuest.id }
        assertTrue(finalSquatsQuest.isRewardClaimed)
    }

    @Test
    fun `hunter rank progression calculation`() {
        assertEquals(HunterRank.E_RANK, HunterRank.fromLevel(1))
        assertEquals(HunterRank.D_RANK, HunterRank.fromLevel(10))
        assertEquals(HunterRank.C_RANK, HunterRank.fromLevel(25))
        assertEquals(HunterRank.B_RANK, HunterRank.fromLevel(35))
        assertEquals(HunterRank.A_RANK, HunterRank.fromLevel(55))
        assertEquals(HunterRank.S_RANK, HunterRank.fromLevel(75))
        assertEquals(HunterRank.SHADOW_MONARCH, HunterRank.fromLevel(100))
    }

    @Test
    fun `exercise types provide xp and calories`() {
        assertTrue(ExerciseType.PUSHUPS.xpPerRep > 0)
        assertTrue(ExerciseType.SQUATS.calPerRep > 0)
        assertEquals("Push-ups", ExerciseType.PUSHUPS.displayName)
    }

    @Test
    fun `pvp battle enforces identical exercise for both players`() {
        val battle = com.example.data.model.PvpBattle(
            battleId = "test_pvp",
            exerciseName = ExerciseType.SQUATS.name,
            targetReps = 25,
            playerReps = 0,
            opponentName = "Rival Hunter Baek",
            opponentRank = "B",
            opponentReps = 0
        )
        assertEquals("SQUATS", battle.exerciseName)
        assertEquals(25, battle.targetReps)
        assertEquals(false, battle.isFinished)
    }
}
