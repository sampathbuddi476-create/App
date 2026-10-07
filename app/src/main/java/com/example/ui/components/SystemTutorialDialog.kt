package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
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

private data class InteractiveTutorialStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color,
    val explanation: String,
    val interactivePrompt: String,
    val simulationType: String
)

@Composable
fun SystemTutorialDialog(
    onDismiss: () -> Unit
) {
    val steps = remember {
        listOf(
            InteractiveTutorialStep(
                stepNumber = 1,
                title = "1. ATHLETE AWAKENING",
                subtitle = "Rank Progression & Stat Allocation",
                icon = Icons.Default.MilitaryTech,
                accentColor = SystemCyan,
                explanation = "You start as an E-Rank athlete. Complete training sessions to gain EXP, level up, and unlock stat points (STR, AGI, VIT, PER, STA).",
                interactivePrompt = "TEST SIMULATION: Tap the stat point node below to try allocating strength!",
                simulationType = "STAT_ALLOC"
            ),
            InteractiveTutorialStep(
                stepNumber = 2,
                title = "2. DAILY COMMISSIONS",
                subtitle = "Push-ups, Squats, Sit-ups & Cardio",
                icon = Icons.Default.Assignment,
                accentColor = SystemGold,
                explanation = "Four daily fitness commissions refresh every 24 hours. Tap 'START QUEST' to track movements or complete them for massive gold and artifact loot.",
                interactivePrompt = "TEST SIMULATION: Tap 'SIMULATE QUEST LAUNCH' to see quest activation!",
                simulationType = "QUEST_LAUNCH"
            ),
            InteractiveTutorialStep(
                stepNumber = 3,
                title = "3. AI CAMERA TRACKER",
                subtitle = "100% Hands-Free Pose Analysis",
                icon = Icons.Default.CameraAlt,
                accentColor = SystemGreen,
                explanation = "Set your phone down facing your full body. Computer vision automatically detects movement phases (Descending, Bottom Hold, Ascending) and counts reps with zero touch.",
                interactivePrompt = "TEST SIMULATION: Tap 'PERFORM SIMULATED REP' to see live depth analysis!",
                simulationType = "REP_TRACK"
            ),
            InteractiveTutorialStep(
                stepNumber = 4,
                title = "4. 1v1 PVP ARENA",
                subtitle = "Fair Synchronized Duels",
                icon = Icons.Default.SportsMartialArts,
                accentColor = SystemPurple,
                explanation = "Challenge random fitness rivals. STRICT RULE: Both players must perform the exact same chosen exercise. The first to reach the target wins RP and rank glory.",
                interactivePrompt = "TEST SIMULATION: Tap 'TEST MATCHMAKING' to simulate a rival challenge!",
                simulationType = "PVP_SIM"
            ),
            InteractiveTutorialStep(
                stepNumber = 5,
                title = "5. PROFILE & SETTINGS",
                subtitle = "Dossier Access & Cloud Backup",
                icon = Icons.Default.Security,
                accentColor = SystemAmber,
                explanation = "Access Settings directly via the top-left gear icon anytime, or tap the bottom-right 'PROFILE' tab to view your hunter dossier, change codename, or trigger voice briefings.",
                interactivePrompt = "TEST SIMULATION: Tap 'TEST VOICE BRIEFING' to hear system audio!",
                simulationType = "PROFILE_TEST"
            )
        )
    }

    var currentStepIndex by remember { mutableIntStateOf(0) }
    val step = steps[currentStepIndex]
    val isLast = currentStepIndex == steps.size - 1

    // Interactive simulation states
    var simStrength by remember { mutableIntStateOf(10) }
    var simPoints by remember { mutableIntStateOf(3) }
    var simQuestStarted by remember { mutableStateOf(false) }
    var simRepCount by remember { mutableIntStateOf(0) }
    var simPhase by remember { mutableStateOf("READY") }
    var simPvpMatched by remember { mutableStateOf(false) }
    var simVoiceHeard by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SystemBackground.copy(alpha = 0.95f))
                .padding(16.dp)
                .testTag("system_tutorial_dialog"),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, step.accentColor, CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SystemSurface)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(step.accentColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .border(1.dp, step.accentColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "INTERACTIVE TUTORIAL • ${step.stepNumber} / ${steps.size}",
                                color = step.accentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tutorial",
                                tint = TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Title & Subtitle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(step.accentColor.copy(alpha = 0.15f))
                                .border(1.dp, step.accentColor, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = step.icon,
                                contentDescription = step.title,
                                tint = step.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = step.title,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = step.subtitle,
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = step.explanation,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Sandbox Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(SystemSurfaceVariant)
                            .border(1.dp, step.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.TouchApp,
                                    contentDescription = null,
                                    tint = step.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = step.interactivePrompt,
                                    color = step.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            when (step.simulationType) {
                                "STAT_ALLOC" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("STRENGTH (STR): $simStrength", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text("POINTS AVAILABLE: $simPoints", color = SystemGold, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        SystemButton(
                                            text = if (simPoints > 0) "+1 ALLOCATE" else "RESET",
                                            containerColor = step.accentColor,
                                            contentColor = SystemBackground,
                                            testTag = "sim_allocate_button",
                                            onClick = {
                                                if (simPoints > 0) {
                                                    simStrength++
                                                    simPoints--
                                                } else {
                                                    simStrength = 10
                                                    simPoints = 3
                                                }
                                            }
                                        )
                                    }
                                }
                                "QUEST_LAUNCH" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("QUEST: 100 PUSH-UPS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(if (simQuestStarted) "STATUS: ACTIVE • CAMERA ON" else "STATUS: PENDING", color = if (simQuestStarted) SystemGreen else TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        SystemButton(
                                            text = if (simQuestStarted) "COMPLETED ✓" else "START QUEST ▶",
                                            containerColor = if (simQuestStarted) SystemGreen else step.accentColor,
                                            contentColor = SystemBackground,
                                            testTag = "sim_quest_button",
                                            onClick = { simQuestStarted = !simQuestStarted }
                                        )
                                    }
                                }
                                "REP_TRACK" -> {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("AI DEPTH: $simPhase", color = SystemGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text("REPS: $simRepCount", color = SystemCyan, fontSize = 16.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        SystemButton(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = "PERFORM VIRTUAL REP 🏃",
                                            containerColor = step.accentColor,
                                            contentColor = SystemBackground,
                                            testTag = "sim_rep_button",
                                            onClick = {
                                                simRepCount++
                                                simPhase = if (simRepCount % 2 == 0) "REP COMPLETE" else "BOTTOM HOLD"
                                            }
                                        )
                                    }
                                }
                                "PVP_SIM" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(if (simPvpMatched) "MATCHED: ATHLETE STEELE" else "MATCHMAKING: IDLE", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text("RULE: 25 PUSH-UPS ONLY", color = SystemPurple, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        SystemButton(
                                            text = if (simPvpMatched) "DUEL READY!" else "FIND RIVAL ⚔️",
                                            containerColor = step.accentColor,
                                            contentColor = TextPrimary,
                                            testTag = "sim_pvp_button",
                                            onClick = { simPvpMatched = !simPvpMatched }
                                        )
                                    }
                                }
                                "PROFILE_TEST" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("AUDIO SYSTEM: READY", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                            Text(if (simVoiceHeard) "VOICE: STATUS BRIEFED ✓" else "VOICE: TAP TO TEST", color = if (simVoiceHeard) SystemGreen else TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                        }
                                        SystemButton(
                                            text = "TEST BRIEFING 🎙️",
                                            containerColor = step.accentColor,
                                            contentColor = SystemBackground,
                                            testTag = "sim_voice_button",
                                            onClick = { simVoiceHeard = true }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Dots Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        steps.forEachIndexed { index, _ ->
                            val isCurrent = index == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(width = if (isCurrent) 22.dp else 8.dp, height = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCurrent) step.accentColor else SystemBorderActive.copy(alpha = 0.4f))
                                    .clickable { currentStepIndex = index }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (currentStepIndex > 0) {
                            SystemButton(
                                modifier = Modifier.weight(1f),
                                text = "BACK",
                                containerColor = SystemSurfaceVariant,
                                contentColor = TextSecondary,
                                testTag = "tutorial_back_button",
                                onClick = { currentStepIndex-- }
                            )
                        } else {
                            SystemButton(
                                modifier = Modifier.weight(1f),
                                text = "SKIP GUIDE",
                                containerColor = SystemSurfaceVariant,
                                contentColor = TextMuted,
                                testTag = "tutorial_skip_button",
                                onClick = onDismiss
                            )
                        }

                        SystemButton(
                            modifier = Modifier.weight(1.3f),
                            text = if (isLast) "START TRAINING!" else "NEXT STEP",
                            containerColor = step.accentColor,
                            contentColor = SystemBackground,
                            testTag = "tutorial_next_button",
                            onClick = {
                                if (isLast) {
                                    onDismiss()
                                } else {
                                    currentStepIndex++
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
