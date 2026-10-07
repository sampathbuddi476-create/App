package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.GoogleAuthManager
import com.example.camera.CameraMovementAnalyzer
import com.example.camera.FormRating
import com.example.camera.MovementPhase
import com.example.camera.TrackingFeedback
import com.example.data.db.AppDatabase
import com.example.data.model.DailyQuest
import com.example.data.model.DungeonBoss
import com.example.data.model.ExerciseType
import com.example.data.model.FitnessQuest
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.InventoryItem
import com.example.data.model.LeaderboardEntry
import com.example.data.model.WorkoutLog
import com.example.data.repository.FitnessQuestRepository
import com.example.data.repository.HunterRepository
import com.example.data.repository.LeaderboardRepository
import com.example.sound.SystemSoundManager
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ActiveWorkoutState(
    val exercise: ExerciseType = ExerciseType.PUSHUPS,
    val repCount: Int = 0,
    val durationSeconds: Long = 0L,
    val caloriesBurned: Float = 0f,
    val currentPhase: MovementPhase = MovementPhase.IDLE,
    val motionEnergy: Float = 0f,
    val formRating: FormRating = FormRating.GOOD,
    val formTip: String = "Stand in frame to begin tracking",
    val perfectReps: Int = 0,
    val isFrontCamera: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isPaused: Boolean = false,
    val activeDungeonBoss: DungeonBoss? = null
)

data class DungeonBattleState(
    val activeBoss: DungeonBoss? = null,
    val bossCurrentHp: Int = 100,
    val bossMaxHp: Int = 100,
    val bossAttackCountdown: Int = 8,
    val hunterCurrentHp: Int = 100,
    val hunterMaxHp: Int = 100,
    val combatLog: List<String> = emptyList(),
    val isVictory: Boolean = false,
    val isDefeat: Boolean = false,
    val repsCompletedInRaid: Int = 0
)

data class LevelUpEvent(
    val newLevel: Int,
    val newRank: HunterRank,
    val previousRank: HunterRank = HunterRank.E_RANK,
    val statPointsGained: Int = 3,
    val isRankUp: Boolean = false,
    val questTitle: String? = null
)

class SoloLevelingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = HunterRepository(database.hunterDao())
    val fitnessQuestRepository = FitnessQuestRepository(database.fitnessQuestDao(), database.hunterDao())
    val authManager = GoogleAuthManager(application)
    val leaderboardRepository = LeaderboardRepository(application)
    val soundManager = SystemSoundManager(application)
    val voiceManager = com.example.sound.SystemVoiceManager(application)

    // UI States
    private val _profileState = MutableStateFlow<HunterProfile?>(null)
    val profileState: StateFlow<HunterProfile?> = _profileState.asStateFlow()

    private val _dailyQuestsState = MutableStateFlow<List<DailyQuest>>(emptyList())
    val dailyQuestsState: StateFlow<List<DailyQuest>> = _dailyQuestsState.asStateFlow()

    private val _fitnessQuestsState = MutableStateFlow<List<FitnessQuest>>(emptyList())
    val fitnessQuestsState: StateFlow<List<FitnessQuest>> = _fitnessQuestsState.asStateFlow()

    private val _leaderboardState = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboardState: StateFlow<List<LeaderboardEntry>> = _leaderboardState.asStateFlow()

    private val _dungeonsState = MutableStateFlow<List<DungeonBoss>>(emptyList())
    val dungeonsState: StateFlow<List<DungeonBoss>> = _dungeonsState.asStateFlow()

    private val _inventoryState = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryState: StateFlow<List<InventoryItem>> = _inventoryState.asStateFlow()

    private val _workoutLogsState = MutableStateFlow<List<WorkoutLog>>(emptyList())
    val workoutLogsState: StateFlow<List<WorkoutLog>> = _workoutLogsState.asStateFlow()

    private val _workoutState = MutableStateFlow(ActiveWorkoutState())
    val workoutState: StateFlow<ActiveWorkoutState> = _workoutState.asStateFlow()

    private val _dungeonBattleState = MutableStateFlow(DungeonBattleState())
    val dungeonBattleState: StateFlow<DungeonBattleState> = _dungeonBattleState.asStateFlow()

    private val _pvpBattleState = MutableStateFlow<com.example.data.model.PvpBattle?>(null)
    val pvpBattleState: StateFlow<com.example.data.model.PvpBattle?> = _pvpBattleState.asStateFlow()

    private val _levelUpEvent = MutableStateFlow<LevelUpEvent?>(null)
    val levelUpEvent: StateFlow<LevelUpEvent?> = _levelUpEvent.asStateFlow()

    private val _trackingFeedback = MutableStateFlow(TrackingFeedback())
    val trackingFeedback: StateFlow<TrackingFeedback> = _trackingFeedback.asStateFlow()

    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUser
    val authError: StateFlow<String?> = authManager.authError
    val isAuthLoading: StateFlow<Boolean> = authManager.isLoading

    private val prefs = application.getSharedPreferences("solo_leveling_prefs", android.content.Context.MODE_PRIVATE)
    private val _showTutorial = MutableStateFlow(!prefs.getBoolean("has_seen_tutorial", false))
    val showTutorial: StateFlow<Boolean> = _showTutorial.asStateFlow()

    val isVoiceEnabled = MutableStateFlow(prefs.getBoolean("voice_enabled", true))
    val isAutoSyncEnabled = MutableStateFlow(prefs.getBoolean("auto_sync_enabled", true))

    val trainingFocus = MutableStateFlow(prefs.getString("training_focus", "BALANCED_MONARCH") ?: "BALANCED_MONARCH")
    val hunterExperience = MutableStateFlow(prefs.getString("hunter_experience", "NOVICE") ?: "NOVICE")
    val dailyRepGoal = MutableStateFlow(prefs.getInt("daily_rep_goal", 100))

    init {
        voiceManager.isEnabled = isVoiceEnabled.value
    }

    fun toggleVoice() {
        val next = !isVoiceEnabled.value
        prefs.edit().putBoolean("voice_enabled", next).apply()
        isVoiceEnabled.value = next
        voiceManager.isEnabled = next
    }

    fun toggleAutoSync() {
        val next = !isAutoSyncEnabled.value
        prefs.edit().putBoolean("auto_sync_enabled", next).apply()
        isAutoSyncEnabled.value = next
    }

    fun speakStatusBriefing() {
        val profile = _profileState.value ?: return
        val rank = HunterRank.fromLevel(profile.level)
        val remaining = _fitnessQuestsState.value.count { !it.isCompleted }
        voiceManager.announceStatusBriefing(profile, rank, remaining)
    }

    fun saveHunterEnquiry(focus: String, experience: String, dailyGoal: Int) {
        prefs.edit()
            .putString("training_focus", focus)
            .putString("hunter_experience", experience)
            .putInt("daily_rep_goal", dailyGoal)
            .apply()
        trainingFocus.value = focus
        hunterExperience.value = experience
        dailyRepGoal.value = dailyGoal
    }

    fun deleteAccount(onComplete: () -> Unit) {
        viewModelScope.launch {
            val uid = authManager.currentUser.value?.uid
            if (uid != null) {
                leaderboardRepository.deleteHunterFromLeaderboard(uid)
            }
            authManager.signOut()
            database.hunterDao().clearProfile()
            database.hunterDao().clearDailyQuests()
            database.hunterDao().clearInventory()
            database.hunterDao().clearWorkoutLogs()
            database.fitnessQuestDao().clearFitnessQuests()

            prefs.edit().clear().apply()

            _profileState.value = null
            _fitnessQuestsState.value = emptyList()
            _dailyQuestsState.value = emptyList()
            _inventoryState.value = emptyList()
            _workoutLogsState.value = emptyList()

            // Repopulate fresh initial state
            repository.ensureDailyQuestsInitialized()
            fitnessQuestRepository.ensureDailyFitnessQuestsInitialized()

            onComplete()
        }
    }

    fun openTutorial() {
        _showTutorial.value = true
    }

    fun dismissTutorial() {
        prefs.edit().putBoolean("has_seen_tutorial", true).apply()
        _showTutorial.value = false
    }

    private var workoutTimerJob: Job? = null
    private var bossAttackJob: Job? = null
    private var pvpOpponentJob: Job? = null

    // Movement Analyzer instance
    val cameraAnalyzer = CameraMovementAnalyzer(
        currentExercise = ExerciseType.PUSHUPS,
        onTrackingUpdate = { feedback ->
            _trackingFeedback.value = feedback
            _workoutState.update { current ->
                current.copy(
                    currentPhase = feedback.phase,
                    motionEnergy = feedback.motionEnergy,
                    formTip = feedback.formTip
                )
            }
        },
        onRepCounted = { repNum, formRating ->
            handleRepCounted(formRating)
        }
    )

    init {
        viewModelScope.launch {
            repository.ensureDailyQuestsInitialized()
            fitnessQuestRepository.ensureDailyFitnessQuestsInitialized()
        }

        viewModelScope.launch {
            repository.hunterProfileFlow.collectLatest { profile ->
                _profileState.value = profile
                profile?.let { syncToLeaderboardIfSignedIn(it) }
            }
        }

        viewModelScope.launch {
            repository.getDailyQuestsFlow().collectLatest { quests ->
                _dailyQuestsState.value = quests
            }
        }

        viewModelScope.launch {
            fitnessQuestRepository.getDailyFitnessQuestsFlow().collectLatest { quests ->
                _fitnessQuestsState.value = quests
            }
        }

        viewModelScope.launch {
            authManager.currentUser.collectLatest { user ->
                if (user != null) {
                    val currentProfile = _profileState.value
                    if (currentProfile != null) {
                        syncToLeaderboardIfSignedIn(currentProfile)
                    }
                }
            }
        }

        viewModelScope.launch {
            leaderboardRepository.observeLeaderboard().collectLatest { entries ->
                _leaderboardState.value = entries
            }
        }

        viewModelScope.launch {
            repository.allDungeonsFlow.collectLatest { dungeons ->
                _dungeonsState.value = dungeons
            }
        }

        viewModelScope.launch {
            repository.allInventoryItemsFlow.collectLatest { items ->
                _inventoryState.value = items
            }
        }

        viewModelScope.launch {
            repository.recentWorkoutLogsFlow.collectLatest { logs ->
                _workoutLogsState.value = logs
            }
        }
    }

    private fun syncToLeaderboardIfSignedIn(profile: HunterProfile) {
        val user = authManager.currentUser.value ?: return
        viewModelScope.launch {
            leaderboardRepository.syncHunterProfile(profile, user)
        }
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(activity)
            if (result.isSuccess) {
                soundManager.playQuestCompleteSound()
                _profileState.value?.let { profile ->
                    syncToLeaderboardIfSignedIn(profile)
                }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
    }

    fun handleRepCounted(rating: FormRating) {
        val current = _workoutState.value
        val newRep = current.repCount + 1
        val isPerfect = rating == FormRating.PERFECT
        val cals = current.caloriesBurned + current.exercise.calPerRep

        _workoutState.update {
            it.copy(
                repCount = newRep,
                formRating = rating,
                perfectReps = if (isPerfect) it.perfectReps + 1 else it.perfectReps,
                caloriesBurned = cals
            )
        }

        if (current.isSoundEnabled) {
            soundManager.playRepSound()
        }

        // Also add progress to fitness quests
        viewModelScope.launch {
            fitnessQuestRepository.addProgressByCategory(current.exercise.name, 1)
        }

        // If in Dungeon Boss Battle, apply attack damage to boss!
        val bossBattle = _dungeonBattleState.value
        val activeBoss = bossBattle.activeBoss
        if (activeBoss != null && !bossBattle.isVictory && !bossBattle.isDefeat) {
            applyBossDamage(activeBoss, rating)
        }

        // If in 1v1 PvP Duel, record rep towards target
        val activePvp = _pvpBattleState.value
        if (activePvp != null && !activePvp.isFinished) {
            val newPlayerReps = activePvp.playerReps + 1
            val isWon = newPlayerReps >= activePvp.targetReps

            _pvpBattleState.update {
                it?.copy(
                    playerReps = newPlayerReps,
                    isFinished = isWon,
                    isPlayerWinner = isWon,
                    ratingPointsChange = if (isWon) 25 else 0
                )
            }

            if (isWon) {
                soundManager.playLevelUpSound()
                pvpOpponentJob?.cancel()
                viewModelScope.launch {
                    val profile = _profileState.value ?: return@launch
                    var newXp = profile.currentXp + 300
                    var level = profile.level
                    var xpToNext = profile.xpToNextLevel
                    var statPoints = profile.statPoints

                    while (newXp >= xpToNext) {
                        newXp -= xpToNext
                        level += 1
                        statPoints += 3
                        xpToNext = (xpToNext * 1.35).toLong()
                    }

                    val updated = profile.copy(
                        level = level,
                        currentXp = newXp,
                        xpToNextLevel = xpToNext,
                        statPoints = statPoints,
                        gold = profile.gold + 500,
                        totalReps = profile.totalReps + activePvp.targetReps
                    )
                    repository.updateProfile(updated)
                    syncToLeaderboardIfSignedIn(updated)
                }
            }
        }
    }

    private fun applyBossDamage(boss: DungeonBoss, rating: FormRating) {
        val profile = _profileState.value ?: return
        val equippedItems = _inventoryState.value.filter { it.isEquipped }
        val bonusStr = equippedItems.sumOf { it.statBonusStr }
        val totalStr = profile.strength + bonusStr

        val baseDamage = totalStr * 0.8f + 5f
        val rankMultiplier = HunterRank.fromLevel(profile.level).damageMultiplier
        val formMultiplier = rating.multiplier
        val finalDamage = (baseDamage * rankMultiplier * formMultiplier).toInt().coerceAtLeast(1)

        soundManager.playBossHitSound()

        _dungeonBattleState.update { current ->
            val remainingHp = (current.bossCurrentHp - finalDamage).coerceAtLeast(0)
            val isDead = remainingHp <= 0
            val newLog = ("Hit ${boss.name} for $finalDamage DMG! (${rating.label})" +
                    if (isDead) " [BOSS SLAIN!]" else "").let { log ->
                listOf(log) + current.combatLog.take(5)
            }

            current.copy(
                bossCurrentHp = remainingHp,
                isVictory = isDead,
                repsCompletedInRaid = current.repsCompletedInRaid + 1,
                combatLog = newLog
            )
        }

        if (_dungeonBattleState.value.isVictory) {
            soundManager.playLevelUpSound()
            bossAttackJob?.cancel()
            viewModelScope.launch {
                repository.completeDungeonBoss(boss, _workoutState.value.durationSeconds)
            }
        }
    }

    fun startWorkout(exercise: ExerciseType, dungeonBoss: DungeonBoss? = null) {
        cameraAnalyzer.currentExercise = exercise
        cameraAnalyzer.resetRepCount()

        _workoutState.value = ActiveWorkoutState(
            exercise = exercise,
            activeDungeonBoss = dungeonBoss
        )

        if (dungeonBoss != null) {
            val profile = _profileState.value
            _dungeonBattleState.value = DungeonBattleState(
                activeBoss = dungeonBoss,
                bossCurrentHp = dungeonBoss.maxHp,
                bossMaxHp = dungeonBoss.maxHp,
                bossAttackCountdown = dungeonBoss.attackIntervalSeconds,
                hunterCurrentHp = profile?.currentHp ?: 100,
                hunterMaxHp = profile?.maxHp ?: 100,
                combatLog = listOf("Entering ${dungeonBoss.name}'s Dungeon Chamber...")
            )
            startBossAttackLoop(dungeonBoss)
        }

        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (!_workoutState.value.isPaused) {
                    _workoutState.update { it.copy(durationSeconds = it.durationSeconds + 1) }
                    // If cardio exercise, add minutes progress
                    if (exercise == ExerciseType.JUMPING_JACKS && _workoutState.value.durationSeconds % 60L == 0L) {
                        fitnessQuestRepository.addProgressByCategory("CARDIO", 1)
                    }
                }
            }
        }
    }

    private fun startBossAttackLoop(boss: DungeonBoss) {
        bossAttackJob?.cancel()
        bossAttackJob = viewModelScope.launch {
            var counter = boss.attackIntervalSeconds
            while (_dungeonBattleState.value.activeBoss != null &&
                !_dungeonBattleState.value.isVictory &&
                !_dungeonBattleState.value.isDefeat
            ) {
                delay(1000)
                counter--
                if (counter <= 0) {
                    val damage = boss.bossAttackDamage
                    soundManager.playSystemAlertSound()

                    _dungeonBattleState.update { current ->
                        val newHp = (current.hunterCurrentHp - damage).coerceAtLeast(0)
                        val isDefeat = newHp <= 0
                        val log = "${boss.name} dealt $damage DMG to Hunter!" +
                                if (isDefeat) " [CRITICAL FAILURE: HUNTER DOWN]" else ""
                        current.copy(
                            hunterCurrentHp = newHp,
                            isDefeat = isDefeat,
                            bossAttackCountdown = boss.attackIntervalSeconds,
                            combatLog = listOf(log) + current.combatLog.take(5)
                        )
                    }
                    counter = boss.attackIntervalSeconds
                } else {
                    _dungeonBattleState.update { it.copy(bossAttackCountdown = counter) }
                }
            }
        }
    }

    fun finishWorkout(onFinished: () -> Unit) {
        val current = _workoutState.value
        workoutTimerJob?.cancel()
        bossAttackJob?.cancel()

        if (current.repCount > 0) {
            viewModelScope.launch {
                val totalReps = current.repCount
                val avgForm = if (totalReps > 0) {
                    ((current.perfectReps.toFloat() / totalReps) * 30 + 70).toInt().coerceIn(70, 100)
                } else 80

                val (xpGained, didLevelUp) = repository.recordWorkoutReps(
                    exercise = current.exercise,
                    reps = totalReps,
                    durationSeconds = current.durationSeconds,
                    formScoreAvg = avgForm,
                    dungeonId = current.activeDungeonBoss?.id
                )

                if (didLevelUp) {
                    val updated = _profileState.value
                    if (updated != null) {
                        soundManager.playLevelUpSound()
                        val newRank = HunterRank.fromLevel(updated.level)
                        _levelUpEvent.value = LevelUpEvent(
                            newLevel = updated.level,
                            newRank = newRank
                        )
                        voiceManager.announceRankAdvancement(updated.hunterName, newRank)
                    }
                } else {
                    soundManager.playQuestCompleteSound()
                    val cals = current.exercise.calPerRep * totalReps
                    voiceManager.announceWorkoutSummary(
                        exerciseName = current.exercise.displayName,
                        reps = totalReps,
                        calories = cals,
                        durationSec = current.durationSeconds
                    )
                }

                _profileState.value?.let { syncToLeaderboardIfSignedIn(it) }
                onFinished()
            }
        } else {
            onFinished()
        }
    }

    fun manualAddRep() {
        handleRepCounted(FormRating.GOOD)
        cameraAnalyzer.setManualRepCount(_workoutState.value.repCount)
    }

    fun manualSubRep() {
        val current = _workoutState.value
        if (current.repCount > 0) {
            _workoutState.update { it.copy(repCount = it.repCount - 1) }
            cameraAnalyzer.setManualRepCount(_workoutState.value.repCount)
        }
    }

    fun switchExerciseInWorkout(newExercise: ExerciseType) {
        cameraAnalyzer.currentExercise = newExercise
        cameraAnalyzer.resetRepCount()
        _workoutState.update { it.copy(exercise = newExercise, repCount = 0) }
    }

    fun toggleCameraLens() {
        _workoutState.update { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun toggleSound() {
        _workoutState.update { it.copy(isSoundEnabled = !it.isSoundEnabled) }
    }

    fun allocateStat(statKey: String) {
        viewModelScope.launch {
            val success = repository.allocateStatPoint(statKey)
            if (success) {
                soundManager.playRepSound()
            }
        }
    }

    fun claimDailyQuest(quest: DailyQuest) {
        viewModelScope.launch {
            val success = repository.claimQuestReward(quest)
            if (success) {
                soundManager.playQuestCompleteSound()
            }
        }
    }

    fun claimFitnessQuest(questId: Long) {
        viewModelScope.launch {
            val result = fitnessQuestRepository.claimQuestRewardResult(questId)
            if (result.success) {
                if (result.didLevelUp) {
                    soundManager.playLevelUpSound()
                    _levelUpEvent.value = LevelUpEvent(
                        newLevel = result.newLevel,
                        newRank = result.newRank,
                        previousRank = result.previousRank,
                        statPointsGained = result.statPointsGained,
                        isRankUp = result.didRankUp,
                        questTitle = result.questTitle
                    )
                    val hunterName = _profileState.value?.hunterName ?: "Hunter"
                    if (result.didRankUp) {
                        voiceManager.announceRankAdvancement(hunterName, result.newRank)
                    } else {
                        voiceManager.announceMilestone("Level ${result.newLevel} Reached", "Physical ascension confirmed for $hunterName")
                    }
                } else {
                    soundManager.playQuestCompleteSound()
                    voiceManager.announceMilestone("Fitness Commission Accomplished", "${result.questTitle} successfully logged")
                }
                _profileState.value?.let { syncToLeaderboardIfSignedIn(it) }
            }
        }
    }

    suspend fun checkUsernameAvailability(name: String): Boolean {
        val currentUid = authManager.currentUser.value?.uid
        return !leaderboardRepository.isUsernameTaken(name, currentUid)
    }

    fun getSuggestedUsernames(seed: String? = null): List<String> {
        return leaderboardRepository.generateSuggestedUsernames(seed ?: _profileState.value?.hunterName)
    }

    fun updateHunterUsername(newName: String, onDone: ((Boolean, String?) -> Unit)? = null) {
        val trimmed = newName.trim()
        if (trimmed.length < 3) {
            onDone?.invoke(false, "Codename must be at least 3 characters")
            return
        }
        if (trimmed.length > 20) {
            onDone?.invoke(false, "Codename cannot exceed 20 characters")
            return
        }
        viewModelScope.launch {
            val currentUid = authManager.currentUser.value?.uid
            val isTaken = leaderboardRepository.isUsernameTaken(trimmed, currentUid)
            if (isTaken) {
                onDone?.invoke(false, "Codename already registered to another hunter")
                return@launch
            }
            val current = _profileState.value ?: return@launch
            val updated = current.copy(hunterName = trimmed)
            repository.updateProfile(updated)
            syncToLeaderboardIfSignedIn(updated)
            soundManager.playRepSound()
            onDone?.invoke(true, null)
        }
    }

    fun usePotion(item: InventoryItem) {
        viewModelScope.launch {
            val success = repository.usePotion(item)
            if (success) {
                soundManager.playQuestCompleteSound()
            }
        }
    }

    fun toggleEquip(item: InventoryItem) {
        viewModelScope.launch {
            repository.toggleEquipItem(item)
            soundManager.playRepSound()
        }
    }

    fun startPvpBattle(
        exercise: ExerciseType,
        targetReps: Int,
        opponentName: String,
        opponentRank: String
    ) {
        // Enforce identical exercise for both players
        cameraAnalyzer.currentExercise = exercise
        cameraAnalyzer.resetRepCount()

        _workoutState.value = ActiveWorkoutState(
            exercise = exercise,
            repCount = 0
        )

        _pvpBattleState.value = com.example.data.model.PvpBattle(
            battleId = "pvp_${System.currentTimeMillis()}",
            exerciseName = exercise.name,
            targetReps = targetReps,
            playerReps = 0,
            opponentName = opponentName,
            opponentRank = opponentRank,
            opponentReps = 0,
            isFinished = false
        )

        // Opponent simulation pace: 1.8 to 2.4s per rep
        pvpOpponentJob?.cancel()
        pvpOpponentJob = viewModelScope.launch {
            val paceDelay = when (opponentRank) {
                "S" -> 1600L
                "A" -> 1900L
                "B" -> 2200L
                else -> 2500L
            }

            while (_pvpBattleState.value != null && !_pvpBattleState.value!!.isFinished) {
                delay(paceDelay)
                val current = _pvpBattleState.value ?: break
                if (current.isFinished) break

                val newOpponentReps = current.opponentReps + 1
                val isOpponentWon = newOpponentReps >= current.targetReps

                _pvpBattleState.update {
                    it?.copy(
                        opponentReps = newOpponentReps,
                        isFinished = isOpponentWon,
                        isPlayerWinner = false
                    )
                }

                if (isOpponentWon) {
                    soundManager.playSystemAlertSound()
                    break
                }
            }
        }
    }

    fun manualAddPvpRep() {
        handleRepCounted(FormRating.GOOD)
    }

    fun leavePvpBattle() {
        pvpOpponentJob?.cancel()
        _pvpBattleState.value = null
    }

    fun manualSyncLeaderboard() {
        val profile = _profileState.value ?: return
        syncToLeaderboardIfSignedIn(profile)
        soundManager.playQuestCompleteSound()
    }

    fun dismissLevelUpDialog() {
        _levelUpEvent.value = null
    }

    override fun onCleared() {
        super.onCleared()
        workoutTimerJob?.cancel()
        bossAttackJob?.cancel()
        pvpOpponentJob?.cancel()
        soundManager.release()
    }
}
