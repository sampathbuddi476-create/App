package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.InventoryItem
import com.example.ui.components.HunterRankBadge
import com.example.ui.components.ResourceBar
import com.example.ui.components.StatRow
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemCyanGlow
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SystemStatusScreen(
    profile: HunterProfile?,
    equippedItems: List<InventoryItem>,
    onAllocateStat: (String) -> Unit,
    onStartWorkoutClick: () -> Unit,
    onOpenDungeonsClick: () -> Unit = {},
    onOpenInventoryClick: () -> Unit = {},
    onChangeUsernameClick: () -> Unit = {},
    onOpenTutorial: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    if (profile == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(SystemBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("LOADING HUNTER STATUS...", color = SystemCyan, fontFamily = FontFamily.Monospace)
        }
        return
    }

    val rank = HunterRank.fromLevel(profile.level)
    val strBonus = equippedItems.sumOf { it.statBonusStr }
    val agiBonus = equippedItems.sumOf { it.statBonusAgi }
    val vitBonus = equippedItems.sumOf { it.statBonusVit }
    val xpProgress = (profile.currentXp.toFloat() / profile.xpToNextLevel).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("status_screen")
    ) {
        // Top Quick Bar: Settings in Top Left Corner + Title + Tutorial in Top Right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top Left Settings Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SystemSurfaceVariant)
                    .border(1.dp, SystemCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable { onOpenSettings() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("top_left_settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "System Settings",
                    tint = SystemCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "SETTINGS",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Top Right Tutorial & Guide Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(SystemCyan.copy(alpha = 0.15f))
                    .border(1.dp, SystemCyan.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .clickable { onOpenTutorial() }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("top_tutorial_button")
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = "System Guide",
                    tint = SystemCyan,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "TUTORIAL",
                    color = SystemCyan,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Top System Window: Hunter Identification
        SystemWindow(
            title = "STATUS WINDOW",
            systemTag = "PLAYER: ${profile.hunterName.uppercase()}"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onChangeUsernameClick() }
                    ) {
                        Text(
                            text = profile.hunterName,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit username",
                            tint = SystemCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "TITLE: ${profile.title}",
                        color = TextCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "LEVEL: ${profile.level}",
                        color = SystemGold,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                HunterRankBadge(rank = rank)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // XP Bar
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "EXP PROGRESS",
                        color = SystemCyan,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${profile.currentXp} / ${profile.xpToNextLevel} XP (${(xpProgress * 100).toInt()}%)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { xpProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = SystemCyan,
                    trackColor = SystemSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HP and MP Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ResourceBar(
                    title = "HP",
                    current = profile.currentHp,
                    max = profile.maxHp,
                    barColor = SystemRed,
                    modifier = Modifier.weight(1f)
                )
                ResourceBar(
                    title = "MP",
                    current = profile.currentMp,
                    max = profile.maxMp,
                    barColor = SystemPurple,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Gold and Currency
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GOLD: ",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "${profile.gold} G",
                    color = TextGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stat Distribution Window
        SystemWindow(
            title = "ABILITY PARAMETERS",
            systemTag = if (profile.statPoints > 0) "POINTS: ${profile.statPoints}" else "STAT ALLOCATION",
            borderColor = if (profile.statPoints > 0) SystemAmber else SystemCyan
        ) {
            if (profile.statPoints > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .background(SystemAmber.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, SystemAmber, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "• YOU HAVE ${profile.statPoints} UNALLOCATED STAT POINTS! TAP [+] TO EMPOWER.",
                        color = SystemAmber,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            val canAlloc = profile.statPoints > 0

            StatRow(
                statName = "STR",
                statFullName = "STRENGTH (Damage / Power)",
                value = profile.strength,
                bonus = strBonus,
                canAllocate = canAlloc,
                onAllocate = { onAllocateStat("STR") }
            )
            HorizontalDivider(color = SystemBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            StatRow(
                statName = "AGI",
                statFullName = "AGILITY (Speed / Evasion)",
                value = profile.agility,
                bonus = agiBonus,
                canAllocate = canAlloc,
                onAllocate = { onAllocateStat("AGI") }
            )
            HorizontalDivider(color = SystemBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            StatRow(
                statName = "VIT",
                statFullName = "VITALITY (Health / Armor)",
                value = profile.vitality,
                bonus = vitBonus,
                canAllocate = canAlloc,
                onAllocate = { onAllocateStat("VIT") }
            )
            HorizontalDivider(color = SystemBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            StatRow(
                statName = "PER",
                statFullName = "PERCEPTION (Precision / Crits)",
                value = profile.perception,
                canAllocate = canAlloc,
                onAllocate = { onAllocateStat("PER") }
            )
            HorizontalDivider(color = SystemBorder.copy(alpha = 0.5f), thickness = 0.5.dp)

            StatRow(
                statName = "STA",
                statFullName = "STAMINA (Endurance / Mana)",
                value = profile.stamina,
                canAllocate = canAlloc,
                onAllocate = { onAllocateStat("STA") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Cumulative Fitness Milestones
        SystemWindow(
            title = "HUNTER MILESTONES & RECORDS",
            systemTag = "ACHIEVEMENTS"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MilestoneBox(
                    title = "TOTAL REPS",
                    value = "${profile.totalReps}",
                    subtitle = "Repetitions logged",
                    color = SystemCyan
                )
                MilestoneBox(
                    title = "CALORIES",
                    value = "${profile.caloriesBurned.toInt()}",
                    subtitle = "kCal burned",
                    color = SystemGreen
                )
                MilestoneBox(
                    title = "DUNGEONS",
                    value = "${profile.dungeonsCleared}",
                    subtitle = "Bosses slain",
                    color = SystemPurple
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SystemButton(
                modifier = Modifier.weight(1f),
                text = "DUNGEON RAIDS",
                containerColor = SystemPurple,
                contentColor = TextPrimary,
                testTag = "status_enter_dungeon_button",
                onClick = onOpenDungeonsClick
            )
            SystemButton(
                modifier = Modifier.weight(1f),
                text = "INVENTORY",
                containerColor = SystemSurfaceVariant,
                contentColor = SystemCyan,
                testTag = "status_open_inventory_button",
                onClick = onOpenInventoryClick
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        SystemButton(
            modifier = Modifier.fillMaxWidth(),
            text = "ENTER CAMERA TRAINING ROOM",
            testTag = "start_camera_workout_button",
            onClick = onStartWorkoutClick
        )
    }
}

@Composable
private fun MilestoneBox(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Box(
        modifier = Modifier
            .background(SystemSurfaceVariant, RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                color = color,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 9.sp
            )
        }
    }
}
