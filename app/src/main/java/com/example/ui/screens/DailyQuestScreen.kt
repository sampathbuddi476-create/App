package com.example.ui.screens

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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExerciseType
import com.example.data.model.FitnessQuest
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
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
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun DailyQuestScreen(
    fitnessQuests: List<FitnessQuest>,
    onStartWorkout: (ExerciseType) -> Unit,
    onClaimFitnessReward: (Long) -> Unit
) {
    var countdownString by remember { mutableStateOf("00:00:00") }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Calendar.getInstance()
            val midnight = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }
            val diffMs = (midnight.timeInMillis - now.timeInMillis).coerceAtLeast(0)
            val hours = diffMs / (1000 * 60 * 60)
            val minutes = (diffMs / (1000 * 60)) % 60
            val seconds = (diffMs / 1000) % 60
            countdownString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("daily_quest_screen")
    ) {
        // Quest System Header
        SystemWindow(
            title = "DAILY FITNESS QUESTS",
            systemTag = "ARCHITECT'S SYSTEM"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Complete your daily fitness commissions to level up stats, earn gold, and unlock hunter artifacts.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .background(SystemSurfaceVariant, RoundedCornerShape(6.dp))
                        .border(1.dp, SystemAmber.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Timer",
                            tint = SystemAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = countdownString,
                            color = SystemAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quests List
        Text(
            text = "ACTIVE QUEST COMMISSIONS (${fitnessQuests.size})",
            color = SystemCyan,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        fitnessQuests.forEach { quest ->
            FitnessQuestCard(
                quest = quest,
                onStartWorkout = {
                    val ex = when (quest.category.uppercase()) {
                        "SQUATS" -> ExerciseType.SQUATS
                        "PUSHUPS" -> ExerciseType.PUSHUPS
                        "SITUPS" -> ExerciseType.SITUPS
                        "CARDIO" -> ExerciseType.JUMPING_JACKS
                        "SHADOW_PUNCHES" -> ExerciseType.SHADOW_PUNCHES
                        else -> ExerciseType.PUSHUPS
                    }
                    onStartWorkout(ex)
                },
                onClaimReward = { onClaimFitnessReward(quest.id) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun FitnessQuestCard(
    quest: FitnessQuest,
    onStartWorkout: () -> Unit,
    onClaimReward: () -> Unit
) {
    val progress = (quest.currentProgress.toFloat() / quest.targetValue).coerceIn(0f, 1f)
    val isComplete = quest.isCompleted

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isComplete) SystemGreen.copy(alpha = 0.8f) else SystemBorderActive.copy(alpha = 0.5f),
                CutCornerShape(8.dp)
            ),
        shape = CutCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SystemSurface.copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Title & Progress Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isComplete) Icons.Default.CheckCircle else Icons.Default.FitnessCenter,
                        contentDescription = "Task Status",
                        tint = if (isComplete) SystemGreen else SystemCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = quest.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .background(
                            if (isComplete) SystemGreen.copy(alpha = 0.15f) else SystemSurfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isComplete) SystemGreen.copy(alpha = 0.5f) else SystemBorder,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "${quest.currentProgress} / ${quest.targetValue} ${quest.targetUnit}",
                        color = if (isComplete) SystemGreen else TextCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = quest.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (isComplete) SystemGreen else SystemCyan,
                trackColor = SystemSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Rewards Information Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SystemSurfaceVariant.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Rewards",
                        tint = SystemGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REWARD: +${quest.xpReward} EXP | +${quest.statPointsReward} STAT | +${quest.goldReward} G" +
                                if (!quest.itemRewardName.isNullOrBlank()) " | ${quest.itemRewardName}" else "",
                        color = TextGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dedicated Action Row with prominent Start Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isComplete && !quest.isRewardClaimed) {
                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "CLAIM REWARD (+${quest.xpReward} EXP)",
                        containerColor = SystemGold,
                        contentColor = SystemBackground,
                        testTag = "claim_fitness_quest_${quest.id}",
                        onClick = onClaimReward
                    )
                } else if (isComplete && quest.isRewardClaimed) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SystemGreen.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .border(1.dp, SystemGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓ QUEST COMPLETED & REWARD CLAIMED",
                            color = SystemGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                } else {
                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "START QUEST",
                        containerColor = SystemCyan,
                        contentColor = SystemBackground,
                        testTag = "start_fitness_quest_${quest.id}",
                        onClick = onStartWorkout
                    )
                }
            }
        }
    }
}
