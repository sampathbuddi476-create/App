package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.DungeonBoss
import com.example.data.model.ExerciseType
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.ui.components.HunterRankBadge
import com.example.ui.components.ResourceBar
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
import com.example.ui.viewmodel.DungeonBattleState

@Composable
fun DungeonRaidScreen(
    dungeons: List<DungeonBoss>,
    profile: HunterProfile?,
    battleState: DungeonBattleState,
    onEnterDungeon: (DungeonBoss) -> Unit,
    onLaunchCameraInDungeon: () -> Unit,
    onAttackBossDirect: () -> Unit,
    onLeaveDungeon: () -> Unit
) {
    val activeBoss = battleState.activeBoss

    if (activeBoss != null) {
        // Active Boss Combat Arena Screen
        ActiveBossCombatView(
            boss = activeBoss,
            battleState = battleState,
            profile = profile,
            onLaunchCamera = onLaunchCameraInDungeon,
            onAttackDirect = onAttackBossDirect,
            onLeaveDungeon = onLeaveDungeon
        )
    } else {
        // Dungeons Gate Catalog
        DungeonGateListView(
            dungeons = dungeons,
            profile = profile,
            onEnterDungeon = onEnterDungeon
        )
    }
}

@Composable
private fun ActiveBossCombatView(
    boss: DungeonBoss,
    battleState: DungeonBattleState,
    profile: HunterProfile?,
    onLaunchCamera: () -> Unit,
    onAttackDirect: () -> Unit,
    onLeaveDungeon: () -> Unit
) {
    val bossHpProgress = (battleState.bossCurrentHp.toFloat() / battleState.bossMaxHp).coerceIn(0f, 1f)
    val hunterHpProgress = if (battleState.hunterMaxHp > 0) {
        (battleState.hunterCurrentHp.toFloat() / battleState.hunterMaxHp).coerceIn(0f, 1f)
    } else 1f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dungeon_combat_arena")
    ) {
        // Boss Header Window
        SystemWindow(
            title = "[DUNGEON BOSS CHAMBER]",
            systemTag = "RANK ${boss.rank} GATE",
            borderColor = if (boss.rank == "S") SystemPurple else SystemRed
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = boss.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = boss.subtitle,
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Boss Health Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "BOSS HP",
                        color = SystemRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${battleState.bossCurrentHp} / ${battleState.bossMaxHp}",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { bossHpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = SystemRed,
                    trackColor = SystemSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Boss Attack Countdown Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (battleState.bossAttackCountdown <= 2) Color(0x33FF2A55) else SystemSurfaceVariant,
                    RoundedCornerShape(8.dp)
                )
                .border(
                    1.dp,
                    if (battleState.bossAttackCountdown <= 2) SystemRed else SystemBorder,
                    RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Alert",
                        tint = if (battleState.bossAttackCountdown <= 2) SystemRed else SystemAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "BOSS ATTACK TIMER",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "${battleState.bossAttackCountdown}s",
                    color = if (battleState.bossAttackCountdown <= 2) SystemRed else SystemAmber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hunter Health & Target Exercise
        SystemWindow(
            title = "HUNTER CONDITION",
            systemTag = "PLAYER VITALITY",
            borderColor = SystemCyan
        ) {
            ResourceBar(
                title = "HUNTER HP",
                current = battleState.hunterCurrentHp,
                max = battleState.hunterMaxHp,
                barColor = SystemGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            val targetEx = ExerciseType.fromName(boss.targetExercise)
            Text(
                text = "WEAKNESS REGIMEN: ${targetEx.displayName.uppercase()}",
                color = SystemCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Each tracked rep delivers heavy physical strikes scaled by your STR stat!",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Combat Logs
        SystemWindow(
            title = "COMBAT LOG",
            systemTag = "TELEMETRY"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (battleState.combatLog.isEmpty()) {
                    Text(
                        text = "Awaiting first strike...",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    battleState.combatLog.take(4).forEach { log ->
                        Text(
                            text = "• $log",
                            color = if (log.contains("SLAIN") || log.contains("VICTORY")) SystemGold
                            else if (log.contains("dealt")) SystemRed
                            else TextCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Battle Status / Actions
        if (battleState.isVictory) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SystemGold.copy(alpha = 0.2f), CutCornerShape(8.dp))
                    .border(1.5.dp, SystemGold, CutCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "DUNGEON CONQUERED!",
                        color = SystemGold,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Claimed: ${boss.xpReward} EXP, ${boss.goldReward} Gold, ${boss.itemRewardName}",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SystemButton(
                        text = "RETURN TO GATES",
                        containerColor = SystemGold,
                        contentColor = SystemBackground,
                        testTag = "leave_dungeon_victory",
                        onClick = onLeaveDungeon
                    )
                }
            }
        } else if (battleState.isDefeat) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SystemRed.copy(alpha = 0.2f), CutCornerShape(8.dp))
                    .border(1.5.dp, SystemRed, CutCornerShape(8.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "HUNTER COLLAPSED",
                        color = SystemRed,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You were overwhelmed by ${boss.name}. Use recovery potions or train further.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SystemButton(
                        text = "EMERGENCY RETREAT",
                        containerColor = SystemRed,
                        contentColor = TextPrimary,
                        testTag = "leave_dungeon_defeat",
                        onClick = onLeaveDungeon
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SystemButton(
                    modifier = Modifier.weight(1f),
                    text = "CAMERA STRIKE",
                    containerColor = SystemCyan,
                    contentColor = SystemBackground,
                    testTag = "launch_camera_in_dungeon",
                    onClick = onLaunchCamera
                )
                SystemButton(
                    modifier = Modifier.weight(1f),
                    text = "ATTACK (+1 REP)",
                    containerColor = SystemPurple,
                    contentColor = TextPrimary,
                    testTag = "attack_boss_direct",
                    onClick = onAttackDirect
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "RETREAT FROM DUNGEON",
                containerColor = SystemSurfaceVariant,
                contentColor = TextMuted,
                testTag = "retreat_dungeon_button",
                onClick = onLeaveDungeon
            )
        }
    }
}

@Composable
private fun DungeonGateListView(
    dungeons: List<DungeonBoss>,
    profile: HunterProfile?,
    onEnterDungeon: (DungeonBoss) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dungeon_list_view")
    ) {
        SystemWindow(
            title = "INSTANT DUNGEONS & GATES",
            systemTag = "RAID ARCHIVES"
        ) {
            Text(
                text = "Dimensional rifts filled with mythical beasts. Enter gates and conquer dungeon bosses through real physical exercise reps to earn legendary artifacts and massive EXP.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        dungeons.forEach { boss ->
            DungeonCard(boss = boss, onEnter = { onEnterDungeon(boss) })
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun DungeonCard(
    boss: DungeonBoss,
    onEnter: () -> Unit
) {
    val rankColor = when (boss.rank) {
        "S" -> SystemGold
        "A" -> SystemRed
        "B" -> SystemPurple
        "C" -> SystemCyan
        "D" -> SystemGreen
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (boss.isUnlocked) rankColor.copy(alpha = 0.7f) else SystemBorder,
                CutCornerShape(8.dp)
            ),
        shape = CutCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SystemSurface.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(rankColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, rankColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${boss.rank}-RANK GATE",
                            color = rankColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = boss.name,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (!boss.isUnlocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                } else if (boss.isDefeated) {
                    Text(
                        text = "CONQUERED x${boss.timesDefeated}",
                        color = SystemGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${boss.subtitle} • Weakness: ${boss.targetExercise} (${boss.maxHp} reps)",
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "REWARDS: +${boss.xpReward} EXP | +${boss.goldReward} G | ${boss.itemRewardName}",
                    color = TextCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (boss.isUnlocked) {
                    SystemButton(
                        text = "ENTER DUNGEON GATE",
                        containerColor = rankColor,
                        contentColor = SystemBackground,
                        testTag = "enter_gate_${boss.id}",
                        onClick = onEnter
                    )
                } else {
                    Text(
                        text = "[CLEAR PREVIOUS GATE TO UNLOCK]",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
