package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.ExerciseType
import com.example.ui.components.LevelUpDialog
import com.example.ui.screens.AuthGateScreen
import com.example.ui.screens.CameraWorkoutScreen
import com.example.ui.screens.DailyQuestScreen
import com.example.ui.screens.DungeonRaidScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.PvpBattlegroundScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SystemStatusScreen
import com.example.ui.screens.WorkoutHistoryScreen
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemCyanDark
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.viewmodel.SoloLevelingViewModel

enum class SoloScreen(val title: String, val icon: ImageVector) {
    STATUS("STATUS", Icons.Default.MilitaryTech),
    DAILY_QUEST("QUESTS", Icons.Default.Assignment),
    PVP("PVP 1v1", Icons.Default.SportsMartialArts),
    CAMERA_TRACK("TRAIN", Icons.Default.CameraAlt),
    DUNGEON("DUNGEON", Icons.Default.Security),
    LEADERBOARD("RANKS", Icons.Default.EmojiEvents),
    PROFILE("PROFILE", Icons.Default.Person),
    SETTINGS("SETTINGS", Icons.Default.Settings),
    INVENTORY("ITEMS", Icons.Default.Inventory2)
}

@Composable
fun SoloLevelingApp(viewModel: SoloLevelingViewModel = viewModel()) {
    var hasPassedAuthGate by remember { mutableStateOf(false) }
    var currentScreen by remember { mutableStateOf(SoloScreen.STATUS) }
    var onboardingStage by remember { mutableStateOf<String>("SIGN_IN") } // "SIGN_IN", "USERNAME", "ENQUIRY", "DONE"
    var tempChosenUsername by remember { mutableStateOf("Shadow_Hunter") }
    var isGuestSession by remember { mutableStateOf(false) }
    var showRecalibrateEnquiry by remember { mutableStateOf(false) }

    val profile by viewModel.profileState.collectAsState()
    val fitnessQuests by viewModel.fitnessQuestsState.collectAsState()
    val leaderboardEntries by viewModel.leaderboardState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val authError by viewModel.authError.collectAsState()
    val dungeons by viewModel.dungeonsState.collectAsState()
    val inventory by viewModel.inventoryState.collectAsState()
    val workoutLogs by viewModel.workoutLogsState.collectAsState()
    val workoutState by viewModel.workoutState.collectAsState()
    val trackingFeedback by viewModel.trackingFeedback.collectAsState()
    val dungeonBattleState by viewModel.dungeonBattleState.collectAsState()
    val pvpBattleState by viewModel.pvpBattleState.collectAsState()
    val levelUpEvent by viewModel.levelUpEvent.collectAsState()
    val showTutorial by viewModel.showTutorial.collectAsState()
    val isVoiceEnabled by viewModel.isVoiceEnabled.collectAsState()
    val isAutoSyncEnabled by viewModel.isAutoSyncEnabled.collectAsState()
    val trainingFocus by viewModel.trainingFocus.collectAsState()
    val hunterExperience by viewModel.hunterExperience.collectAsState()
    val dailyRepGoal by viewModel.dailyRepGoal.collectAsState()
    var showUsernameDialogOnStatus by remember { mutableStateOf(false) }

    // Onboarding flow: Screen 1 (Sign In) -> Screen 2 (Username Selection) -> Screen 3 (User Enquiry)
    if (!hasPassedAuthGate) {
        when (onboardingStage) {
            "SIGN_IN" -> {
                // If user is already signed in with Google from previous session, advance to USERNAME or DONE
                if (currentUser != null) {
                    tempChosenUsername = currentUser?.displayName?.take(18) ?: profile?.hunterName ?: "Shadow_Hunter"
                    isGuestSession = false
                    onboardingStage = "USERNAME"
                } else {
                    AuthGateScreen(
                        isLoading = isAuthLoading,
                        authError = authError,
                        onSignInWithGoogle = { activity ->
                            viewModel.signInWithGoogle(activity)
                        },
                        onContinueAsGuest = {
                            isGuestSession = true
                            tempChosenUsername = profile?.hunterName ?: "Guest_Hunter"
                            onboardingStage = "USERNAME"
                        }
                    )
                    return
                }
            }
            "USERNAME" -> {
                com.example.ui.components.UsernameSelectionScreen(
                    initialUsername = tempChosenUsername,
                    suggestedNames = viewModel.getSuggestedUsernames(),
                    onRerollSuggestions = { viewModel.getSuggestedUsernames() },
                    onCheckAvailability = { name -> viewModel.checkUsernameAvailability(name) },
                    onConfirmUsername = { chosenName ->
                        tempChosenUsername = chosenName
                        viewModel.updateHunterUsername(chosenName)
                        onboardingStage = "ENQUIRY"
                    }
                )
                return
            }
            "ENQUIRY" -> {
                com.example.ui.components.HunterEnquiryScreen(
                    hunterName = tempChosenUsername,
                    isGuest = isGuestSession,
                    initialFocus = trainingFocus,
                    initialExperience = hunterExperience,
                    initialDailyGoal = dailyRepGoal,
                    onCompleteEnquiry = { focus, exp, goal ->
                        viewModel.saveHunterEnquiry(focus, exp, goal)
                        hasPassedAuthGate = true
                        onboardingStage = "DONE"
                    }
                )
                return
            }
        }
    }

    // Modal Recalibration of Enquiry Survey from Settings
    if (showRecalibrateEnquiry) {
        com.example.ui.components.HunterEnquiryScreen(
            hunterName = profile?.hunterName ?: "Hunter",
            isGuest = currentUser == null,
            initialFocus = trainingFocus,
            initialExperience = hunterExperience,
            initialDailyGoal = dailyRepGoal,
            onCompleteEnquiry = { focus, exp, goal ->
                viewModel.saveHunterEnquiry(focus, exp, goal)
                showRecalibrateEnquiry = false
            }
        )
        return
    }

    // Handle Back Press when on sub-screens
    BackHandler(enabled = currentScreen != SoloScreen.STATUS) {
        currentScreen = SoloScreen.STATUS
    }

    // Visual Celebratory Animation Overlay when leveling up or ranking up
    levelUpEvent?.let { event ->
        com.example.ui.components.CelebratoryRankUpOverlay(
            event = event,
            onDismiss = { viewModel.dismissLevelUpDialog() }
        )
    }

    // Interactive System Tutorial Guide for New Hunters
    if (showTutorial) {
        com.example.ui.components.SystemTutorialDialog(
            onDismiss = { viewModel.dismissTutorial() }
        )
    }

    // Status Screen Quick Username Picker / Writer Dialog
    if (showUsernameDialogOnStatus && profile != null) {
        com.example.ui.components.UsernameSelectionDialog(
            currentUsername = profile!!.hunterName,
            suggestedNames = viewModel.getSuggestedUsernames(),
            onRerollSuggestions = { viewModel.getSuggestedUsernames() },
            onCheckAvailability = { name -> viewModel.checkUsernameAvailability(name) },
            onConfirm = { newName ->
                viewModel.updateHunterUsername(newName)
                showUsernameDialogOnStatus = false
            },
            onDismiss = { showUsernameDialogOnStatus = false }
        )
    }

    val isCameraMode = currentScreen == SoloScreen.CAMERA_TRACK

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SystemBackground,
        bottomBar = {
            if (!isCameraMode) {
                NavigationBar(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .border(1.dp, SystemBorder, CutCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    containerColor = SystemSurface,
                    contentColor = TextPrimary
                ) {
                    val tabs = listOf(
                        SoloScreen.STATUS,
                        SoloScreen.DAILY_QUEST,
                        SoloScreen.PVP,
                        SoloScreen.LEADERBOARD,
                        SoloScreen.PROFILE
                    )

                    tabs.forEach { screen ->
                        val selected = currentScreen == screen
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (screen == SoloScreen.CAMERA_TRACK) {
                                    viewModel.startWorkout(workoutState.exercise)
                                }
                                currentScreen = screen
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (selected) SystemCyan else TextMuted
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontSize = 9.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (selected) SystemCyan else TextMuted
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = SystemCyan.copy(alpha = 0.15f),
                                selectedIconColor = SystemCyan,
                                unselectedIconColor = TextMuted
                            ),
                            modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isCameraMode) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            when (currentScreen) {
                SoloScreen.STATUS -> {
                    SystemStatusScreen(
                        profile = profile,
                        equippedItems = inventory.filter { it.isEquipped },
                        onAllocateStat = { stat -> viewModel.allocateStat(stat) },
                        onStartWorkoutClick = {
                            viewModel.startWorkout(ExerciseType.PUSHUPS)
                            currentScreen = SoloScreen.CAMERA_TRACK
                        },
                        onOpenDungeonsClick = {
                            currentScreen = SoloScreen.DUNGEON
                        },
                        onOpenInventoryClick = {
                            currentScreen = SoloScreen.INVENTORY
                        },
                        onChangeUsernameClick = {
                            showUsernameDialogOnStatus = true
                        },
                        onOpenTutorial = {
                            viewModel.openTutorial()
                        }
                    )
                }

                SoloScreen.DAILY_QUEST -> {
                    DailyQuestScreen(
                        fitnessQuests = fitnessQuests,
                        onStartWorkout = { exercise ->
                            viewModel.startWorkout(exercise)
                            currentScreen = SoloScreen.CAMERA_TRACK
                        },
                        onClaimFitnessReward = { questId ->
                            viewModel.claimFitnessQuest(questId)
                        }
                    )
                }

                SoloScreen.PVP -> {
                    PvpBattlegroundScreen(
                        profile = profile,
                        activeBattle = pvpBattleState,
                        trackingFeedback = trackingFeedback,
                        analyzer = viewModel.cameraAnalyzer,
                        isFrontCamera = workoutState.isFrontCamera,
                        onToggleLens = { viewModel.toggleCameraLens() },
                        onStartBattle = { exercise, targetReps, oppName, oppRank ->
                            viewModel.startPvpBattle(exercise, targetReps, oppName, oppRank)
                        },
                        onLeaveBattle = { viewModel.leavePvpBattle() }
                    )
                }

                SoloScreen.CAMERA_TRACK -> {
                    CameraWorkoutScreen(
                        workoutState = workoutState,
                        trackingFeedback = trackingFeedback,
                        analyzer = viewModel.cameraAnalyzer,
                        onExerciseChanged = { newEx -> viewModel.switchExerciseInWorkout(newEx) },
                        onToggleLens = { viewModel.toggleCameraLens() },
                        onToggleSound = { viewModel.toggleSound() },
                        onFinishWorkout = {
                            viewModel.finishWorkout {
                                currentScreen = if (dungeonBattleState.activeBoss != null) SoloScreen.DUNGEON else SoloScreen.STATUS
                            }
                        },
                        onBack = {
                            viewModel.finishWorkout {
                                currentScreen = SoloScreen.STATUS
                            }
                        }
                    )
                }

                SoloScreen.DUNGEON -> {
                    DungeonRaidScreen(
                        dungeons = dungeons,
                        profile = profile,
                        battleState = dungeonBattleState,
                        onEnterDungeon = { boss ->
                            val targetEx = ExerciseType.fromName(boss.targetExercise)
                            viewModel.startWorkout(targetEx, boss)
                        },
                        onLaunchCameraInDungeon = {
                            currentScreen = SoloScreen.CAMERA_TRACK
                        },
                        onAttackBossDirect = {
                            viewModel.handleRepCounted(com.example.camera.FormRating.PERFECT)
                        },
                        onLeaveDungeon = {
                            viewModel.finishWorkout {
                                currentScreen = SoloScreen.DUNGEON
                            }
                        }
                    )
                }

                SoloScreen.LEADERBOARD -> {
                    LeaderboardScreen(
                        entries = leaderboardEntries,
                        profile = profile,
                        currentUser = currentUser,
                        isAuthLoading = isAuthLoading,
                        authError = authError,
                        onSignInWithGoogle = { activity -> viewModel.signInWithGoogle(activity) },
                        onSignOut = { viewModel.signOut() }
                    )
                }

               SoloScreen.SETTINGS -> {
                   SettingsScreen(
                        profile = profile,
                        currentUser = currentUser,
                        isSoundEnabled = workoutState.isSoundEnabled,
                        isFrontCameraDefault = workoutState.isFrontCamera,
                        isVoiceEnabled = isVoiceEnabled,
                        isAutoSyncEnabled = isAutoSyncEnabled,
                        trainingFocus = trainingFocus,
                        hunterExperience = hunterExperience,
                        dailyRepGoal = dailyRepGoal,
                        onToggleSound = { viewModel.toggleSound() },
                        onToggleCameraLens = { viewModel.toggleCameraLens() },
                        onToggleVoice = { viewModel.toggleVoice() },
                        onToggleAutoSync = { viewModel.toggleAutoSync() },
                        onSpeakSummary = { viewModel.speakStatusBriefing() },
                        onOpenEnquiry = { showRecalibrateEnquiry = true },
                        onDeleteAccount = {
                            viewModel.deleteAccount {
                                hasPassedAuthGate = false
                                onboardingStage = "SIGN_IN"
                                currentScreen = SoloScreen.STATUS
                            }
                        },
                        onSignInWithGoogle = { activity -> viewModel.signInWithGoogle(activity) },
                        onSignOut = { viewModel.signOut() },
                        onManualSync = {}
                    )
                }

                else -> {}
            }
        }
    }
}
