package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.DailyQuest
import com.example.data.model.DungeonBoss
import com.example.data.model.FitnessQuest
import com.example.data.model.HunterProfile
import com.example.data.model.InventoryItem
import com.example.data.model.WorkoutLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Database(
    entities = [
        HunterProfile::class,
        DailyQuest::class,
        FitnessQuest::class,
        InventoryItem::class,
        WorkoutLog::class,
        DungeonBoss::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun hunterDao(): HunterDao
    abstract fun fitnessQuestDao(): FitnessQuestDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "solo_leveling_fit.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Populate default data on first creation
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).hunterDao()
                                prepopulateDatabase(dao)
                            }
                        }

                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).hunterDao()
                                prepopulateDatabase(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(dao: HunterDao) {
            // 1. Initial Hunter Profile
            dao.insertOrUpdateProfile(
                HunterProfile(
                    id = 1,
                    hunterName = "Apex_Athlete",
                    title = "The Novice Trainee",
                    level = 1,
                    currentXp = 0L,
                    xpToNextLevel = 100L,
                    statPoints = 5,
                    strength = 10,
                    agility = 10,
                    vitality = 10,
                    perception = 10,
                    stamina = 10,
                    currentHp = 100,
                    maxHp = 100,
                    currentMp = 50,
                    maxMp = 50,
                    gold = 1000,
                    totalReps = 0,
                    caloriesBurned = 0f,
                    dungeonsCleared = 0
                )
            )

            // 2. Initial Daily Quests
            val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            dao.insertQuests(
                listOf(
                    DailyQuest(
                        exerciseType = "PUSHUPS",
                        title = "Push-ups Training",
                        targetReps = 100,
                        currentReps = 0,
                        dateKey = todayKey
                    ),
                    DailyQuest(
                        exerciseType = "SITUPS",
                        title = "Sit-ups Core Conditioning",
                        targetReps = 100,
                        currentReps = 0,
                        dateKey = todayKey
                    ),
                    DailyQuest(
                        exerciseType = "SQUATS",
                        title = "Squats Leg Strengthening",
                        targetReps = 100,
                        currentReps = 0,
                        dateKey = todayKey
                    ),
                    DailyQuest(
                        exerciseType = "JUMPING_JACKS",
                        title = "Cardio Burst / Running",
                        targetReps = 100,
                        currentReps = 0,
                        dateKey = todayKey
                    )
                )
            )

            // 3. Dungeon Bosses
            dao.insertDungeons(
                listOf(
                    DungeonBoss(
                        id = "boss_goblin",
                        name = "Goblin Chieftain",
                        rank = "E",
                        subtitle = "Cave of Low-Tier Beasts",
                        maxHp = 30,
                        attackIntervalSeconds = 12,
                        bossAttackDamage = 15,
                        targetExercise = "PUSHUPS",
                        xpReward = 150,
                        goldReward = 300,
                        itemRewardName = "Rusty Goblin Dagger",
                        isUnlocked = true,
                        isDefeated = false
                    ),
                    DungeonBoss(
                        id = "boss_statue",
                        name = "God Statue Guardian",
                        rank = "D",
                        subtitle = "Double Dungeon Altar",
                        maxHp = 60,
                        attackIntervalSeconds = 10,
                        bossAttackDamage = 20,
                        targetExercise = "SQUATS",
                        xpReward = 350,
                        goldReward = 800,
                        itemRewardName = "Courage Ring",
                        isUnlocked = false,
                        isDefeated = false
                    ),
                    DungeonBoss(
                        id = "boss_kasaka",
                        name = "Blue Venom Kasaka",
                        rank = "C",
                        subtitle = "Hapjeong Subway Lair",
                        maxHp = 100,
                        attackIntervalSeconds = 9,
                        bossAttackDamage = 25,
                        targetExercise = "PUSHUPS",
                        xpReward = 750,
                        goldReward = 1800,
                        itemRewardName = "Kasaka's Venom Fang",
                        isUnlocked = false,
                        isDefeated = false
                    ),
                    DungeonBoss(
                        id = "boss_cerberus",
                        name = "Gatekeeper Cerberus",
                        rank = "B",
                        subtitle = "Demon Castle Entrance",
                        maxHp = 180,
                        attackIntervalSeconds = 8,
                        bossAttackDamage = 35,
                        targetExercise = "SQUATS",
                        xpReward = 1500,
                        goldReward = 4000,
                        itemRewardName = "Hellfire Hound Fang",
                        isUnlocked = false,
                        isDefeated = false
                    ),
                    DungeonBoss(
                        id = "boss_igris",
                        name = "Blood-Red Commander Igris",
                        rank = "A",
                        subtitle = "Throne Room of the Red Knight",
                        maxHp = 300,
                        attackIntervalSeconds = 7,
                        bossAttackDamage = 45,
                        targetExercise = "SHADOW_PUNCHES",
                        xpReward = 3500,
                        goldReward = 10000,
                        itemRewardName = "Knight Commander Longsword",
                        isUnlocked = false,
                        isDefeated = false
                    ),
                    DungeonBoss(
                        id = "boss_beru",
                        name = "Ant King Beru",
                        rank = "S",
                        subtitle = "Jeju Island Queen's Chamber",
                        maxHp = 500,
                        attackIntervalSeconds = 6,
                        bossAttackDamage = 60,
                        targetExercise = "JUMPING_JACKS",
                        xpReward = 8000,
                        goldReward = 25000,
                        itemRewardName = "Ant King's Sovereign Core",
                        isUnlocked = false,
                        isDefeated = false
                    )
                )
            )

            // 4. Initial Starter Inventory
            dao.insertItems(
                listOf(
                    InventoryItem(
                        name = "Steel Hunter Dagger",
                        category = "WEAPON",
                        rarity = "COMMON",
                        description = "Standard issue daggers for lowest rank hunters. Light and swift.",
                        statBonusStr = 3,
                        statBonusAgi = 2,
                        isEquipped = true,
                        quantity = 1,
                        goldValue = 150
                    ),
                    InventoryItem(
                        name = "System Recovery Potion",
                        category = "POTION",
                        rarity = "RARE",
                        description = "Instantly heals 50 HP and cures exhaustion. A gift from the System.",
                        statBonusVit = 0,
                        isEquipped = false,
                        quantity = 3,
                        goldValue = 200
                    ),
                    InventoryItem(
                        name = "Daily Quest Blessed Box",
                        category = "ACCESSORY",
                        rarity = "EPIC",
                        description = "Unopened mystery reward container from the Architect.",
                        statBonusVit = 2,
                        isEquipped = false,
                        quantity = 1,
                        goldValue = 500
                    )
                )
            )
        }
    }
}
