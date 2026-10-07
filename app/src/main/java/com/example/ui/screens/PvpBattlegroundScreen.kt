package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camera.CameraMovementAnalyzer
import com.example.camera.CameraPreviewWithOverlay
import com.example.camera.TrackingFeedback
import com.example.data.model.ExerciseType
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.PvpBattle
import com.example.ui.components.HunterRankBadge
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemCyanGlow
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun PvpBattlegroundScreen(
    profile: HunterProfile?,
    activeBattle: PvpBattle?,
    trackingFeedback: TrackingFeedback,
    analyzer: CameraMovementAnalyzer,
    isFrontCamera: Boolean,
    onToggleLens: () -> Unit,
    onStartBattle: (ExerciseType, Int, String, String) -> Unit,
    onLeaveBattle: () -> Unit
) {
    if (activeBattle != null) {
        // Active 1v1 Duel Arena View
        ActivePvpArenaView(
            profile = profile,
            battle = activeBattle,
            trackingFeedback = trackingFeedback,
            analyzer = analyzer,
            isFrontCamera = isFrontCamera,
            onToggleLens = onToggleLens,
            onLeaveBattle = onLeaveBattle
        )
    } else {
        // Matchmaking Lobby View
        PvpLobbyView(
            profile = profile,
            onStartBattle = onStartBattle
        )
    }
}

@Composable
private fun PvpLobbyView(
    profile: HunterProfile?,
    onStartBattle: (ExerciseType, Int, String, String) -> Unit
) {
    var selectedExercise by remember { mutableStateOf(ExerciseType.PUSHUPS) }
    var selectedTargetReps by remember { mutableIntStateOf(25) }
    var selectedOpponentIndex by remember { mutableIntStateOf(0) }

    val opponents = listOf(
        Triple("Athlete Marcus Steele", "B", "Heavyweight Calisthenics Specialist"),
        Triple("Athlete Elena Vance", "S", "Endurance Marathon Champion"),
        Triple("Athlete Victor Cross", "A", "Cross-Training Veteran"),
        Triple("Athlete Maya Sterling", "S", "Functional Kinetic Master")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("pvp_lobby_view")
    ) {
        SystemWindow(
            title = "HUNTER PVP BATTLEGROUND",
            systemTag = "1v1 ARENA",
            borderColor = SystemPurple
        ) {
            Text(
                text = "Enter the Hunter Colosseum for real-time 1v1 physical duels. Both contestants must perform the exact same chosen exercise. The first to reach the target repetitions claims victory.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Enforced Same-Exercise Rule Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SystemPurple.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .border(1.dp, SystemPurple, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Rule",
                        tint = SystemPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STRICT DUEL RULE: Both players can ONLY perform the designated exercise. Different exercises are prohibited and will not register.",
                        color = TextCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Choose Enforced Exercise (Push-ups, Squats, or Jumping Jacks)
        Text(
            text = "STEP 1: CHOOSE ENFORCED EXERCISE",
            color = SystemCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val allowedExercises = listOf(ExerciseType.PUSHUPS, ExerciseType.SQUATS, ExerciseType.JUMPING_JACKS)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allowedExercises.forEach { ex ->
                val isSelected = ex == selectedExercise
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CutCornerShape(8.dp))
                        .background(if (isSelected) SystemCyan else SystemSurface)
                        .border(1.dp, if (isSelected) SystemCyanGlow else SystemBorder, CutCornerShape(8.dp))
                        .clickable { selectedExercise = ex }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = ex.displayName.uppercase(),
                            color = if (isSelected) SystemBackground else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Select Duel Distance / Target Reps
        Text(
            text = "STEP 2: TARGET REPETITIONS",
            color = SystemCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        val repOptions = listOf(20, 35, 50)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repOptions.forEach { reps ->
                val isSelected = reps == selectedTargetReps
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CutCornerShape(8.dp))
                        .background(if (isSelected) SystemGold else SystemSurface)
                        .border(1.dp, if (isSelected) SystemGold else SystemBorder, CutCornerShape(8.dp))
                        .clickable { selectedTargetReps = reps }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$reps REPS",
                        color = if (isSelected) SystemBackground else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Random Matchmaking Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SystemPurple.copy(alpha = 0.6f), CutCornerShape(8.dp)),
            shape = CutCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SystemSurfaceVariant.copy(alpha = 0.7f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsMartialArts,
                        contentDescription = "Matchmaking",
                        tint = SystemPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RANDOM RIVAL MATCHMAKING",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "The Colosseum will automatically match you against a random active Hunter from the Association roster. Both contestants are strictly bound to perform ${selectedExercise.displayName.uppercase()}.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Initiate Match Button
        SystemButton(
            modifier = Modifier.fillMaxWidth(),
            text = "MATCH RANDOM OPPONENT & ARISE",
            containerColor = SystemPurple,
            contentColor = TextPrimary,
            testTag = "enter_pvp_battle_button",
            onClick = {
                val randomOpponent = opponents.random()
                onStartBattle(
                    selectedExercise,
                    selectedTargetReps,
                    randomOpponent.first,
                    randomOpponent.second
                )
            }
        )
    }
}

@Composable
private fun ActivePvpArenaView(
    profile: HunterProfile?,
    battle: PvpBattle,
    trackingFeedback: TrackingFeedback,
    analyzer: CameraMovementAnalyzer,
    isFrontCamera: Boolean,
    onToggleLens: () -> Unit,
    onLeaveBattle: () -> Unit
) {
    val playerProgress = (battle.playerReps.toFloat() / battle.targetReps).coerceIn(0f, 1f)
    val opponentProgress = (battle.opponentReps.toFloat() / battle.targetReps).coerceIn(0f, 1f)
    val exercise = ExerciseType.fromName(battle.exerciseName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(14.dp)
            .testTag("active_pvp_arena")
    ) {
        // Enforced Exercise Header
        SystemWindow(
            title = "1v1 BATTLEGROUND",
            systemTag = "FIRST TO ${battle.targetReps} REPS",
            borderColor = SystemPurple
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "LOCKED DUEL EXERCISE",
                    color = TextMuted,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = exercise.displayName.uppercase(),
                    color = SystemCyan,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Both hunters are restricted exclusively to this movement.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Split Versus Progress Dashboard
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SystemBorder, CutCornerShape(8.dp)),
            shape = CutCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SystemSurface)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Player Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "YOU (${profile?.hunterName ?: "Hunter"})",
                        color = SystemCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${battle.playerReps} / ${battle.targetReps}",
                        color = SystemCyan,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { playerProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SystemCyan,
                    trackColor = SystemSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Opponent Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${battle.opponentName.uppercase()} [${battle.opponentRank}]",
                        color = SystemRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${battle.opponentReps} / ${battle.targetReps}",
                        color = SystemRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { opponentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SystemRed,
                    trackColor = SystemSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (!battle.isFinished) {
            // Live Camera View Box for tracking player's reps in real-time
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(CutCornerShape(10.dp))
                    .border(1.5.dp, SystemCyan, CutCornerShape(10.dp))
            ) {
                CameraPreviewWithOverlay(
                    modifier = Modifier.fillMaxSize(),
                    useFrontCamera = isFrontCamera,
                    analyzer = analyzer,
                    trackingFeedback = trackingFeedback
                )

                // Lens switch button on top right of camera preview
                IconButton(
                    onClick = onToggleLens,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(SystemSurface.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Cameraswitch,
                        contentDescription = "Flip camera",
                        tint = SystemCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Tracking phase indicator pill at bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(8.dp)
                        .background(SystemSurface.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LIVE MOTION: ${trackingFeedback.phase.label}",
                        color = SystemCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: reps are tracked strictly via camera movement; player can concede if necessary
            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "CONCEDE DUEL",
                containerColor = SystemSurfaceVariant,
                contentColor = SystemRed,
                testTag = "pvp_concede_button",
                onClick = onLeaveBattle
            )
        } else {
            // Duel Finished Outcome Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (battle.isPlayerWinner) SystemGold.copy(alpha = 0.15f) else SystemRed.copy(alpha = 0.15f),
                        CutCornerShape(10.dp)
                    )
                    .border(
                        2.dp,
                        if (battle.isPlayerWinner) SystemGold else SystemRed,
                        CutCornerShape(10.dp)
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (battle.isPlayerWinner) Icons.Default.EmojiEvents else Icons.Default.SportsMartialArts,
                        contentDescription = "Outcome",
                        tint = if (battle.isPlayerWinner) SystemGold else SystemRed,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (battle.isPlayerWinner) "VICTORY ACHIEVED!" else "DEFEAT IN THE ARENA",
                        color = if (battle.isPlayerWinner) SystemGold else SystemRed,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (battle.isPlayerWinner) {
                            "You outpaced ${battle.opponentName} in ${exercise.displayName}!\n+300 EXP • +25 PvP Rating (RP) • +500 Gold"
                        } else {
                            "${battle.opponentName} reached ${battle.targetReps} reps first.\nTrain harder and challenge again!"
                        },
                        color = TextPrimary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "RETURN TO LOBBY",
                        containerColor = if (battle.isPlayerWinner) SystemGold else SystemCyan,
                        contentColor = SystemBackground,
                        testTag = "pvp_return_lobby_button",
                        onClick = onLeaveBattle
                    )
                }
            }
        }
    }
}
