package com.example.ui.components

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class HunterEnquiryData(
    val focus: String,
    val experience: String,
    val dailyGoalReps: Int
)

@Composable
fun HunterEnquiryScreen(
    hunterName: String,
    isGuest: Boolean,
    initialFocus: String = "BALANCED_MONARCH",
    initialExperience: String = "NOVICE",
    initialDailyGoal: Int = 100,
    onCompleteEnquiry: (focus: String, experience: String, dailyGoal: Int) -> Unit
) {
    var selectedFocus by remember { mutableStateOf(initialFocus) }
    var selectedExperience by remember { mutableStateOf(initialExperience) }
    var goalSlider by remember { mutableFloatStateOf(initialDailyGoal.toFloat()) }

    val focusOptions = listOf(
        Triple("BALANCED_MONARCH", "Balanced Monarch", "Full-body strength, endurance, agility & mobility."),
        Triple("SHADOW_STRENGTH", "Shadow Strength", "High-intensity hypertrophy & maximum power lifting."),
        Triple("AGILITY_ASSASSIN", "Agility Assassin", "Speed, high-cadence reps & fast metabolic burn."),
        Triple("TITAN_ENDURANCE", "Titan Endurance", "Extreme rep stamina & continuous stamina growth.")
    )

    val experienceOptions = listOf(
        Pair("BEGINNER", "Beginner Hunter (E-Rank novice beginning physical conditioning)"),
        Pair("INTERMEDIATE", "Experienced Hunter (C-D Rank regular daily trainee)"),
        Pair("ADVANCED", "Elite S-Rank Hunter (High volume seasoned athlete)")
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("hunter_enquiry_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SystemCyan,
                modifier = Modifier.size(44.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "[ SYSTEM ENQUIRY • PROTOCOL 01 ]",
                color = SystemCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "CALIBRATE HUNTER POTENTIAL",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (isGuest) "Guest Hunter: $hunterName" else "Hunter: $hunterName",
                color = SystemGold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "The System requires initial calibration to calibrate daily quest targets, AI form tracking strictness, and telemetry models for your physical ascent.",
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Section 1: Training Focus
            SystemWindow(
                title = "1. TRAINING DISCIPLINE",
                systemTag = "FOCUS"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    focusOptions.forEach { (id, title, desc) ->
                        val isSelected = selectedFocus == id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(if (isSelected) SystemCyan.copy(alpha = 0.15f) else SystemSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) SystemCyan else SystemBorder,
                                    CutCornerShape(6.dp)
                                )
                                .clickable { selectedFocus = id }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.AssignmentTurnedIn else Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = if (isSelected) SystemCyan else TextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = title.uppercase(),
                                        color = if (isSelected) SystemCyan else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = desc,
                                        color = TextSecondary,
                                        fontSize = 10.sp,
                                        lineHeight = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 2: Hunter Experience Level
            SystemWindow(
                title = "2. CURRENT CONDITIONING",
                systemTag = "EXPERIENCE"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    experienceOptions.forEach { (id, label) ->
                        val isSelected = selectedExperience == id
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(CutCornerShape(6.dp))
                                .background(if (isSelected) SystemGold.copy(alpha = 0.15f) else SystemSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) SystemGold else SystemBorder,
                                    CutCornerShape(6.dp)
                                )
                                .clickable { selectedExperience = id }
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = null,
                                    tint = if (isSelected) SystemGold else TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) SystemGold else TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Section 3: Daily Target Reps
            val currentGoal = goalSlider.toInt()
            SystemWindow(
                title = "3. DAILY REP TARGET",
                systemTag = "$currentGoal REPS"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TARGET REPS / DAY",
                            color = TextCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "$currentGoal REPETITIONS",
                            color = SystemGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = goalSlider,
                        onValueChange = { goalSlider = (it / 10).toInt() * 10f },
                        valueRange = 30f..300f,
                        steps = 26,
                        colors = SliderDefaults.colors(
                            thumbColor = SystemCyan,
                            activeTrackColor = SystemCyan,
                            inactiveTrackColor = SystemSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("30 (Casual)", color = TextMuted, fontSize = 9.sp)
                        Text("100 (Standard)", color = TextMuted, fontSize = 9.sp)
                        Text("300 (Monarch)", color = TextMuted, fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm & Enter System Button
            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "COMMENCE SYSTEM AWAKENING",
                containerColor = SystemCyan,
                contentColor = SystemBackground,
                testTag = "submit_enquiry_button",
                onClick = {
                    onCompleteEnquiry(
                        selectedFocus,
                        selectedExperience,
                        currentGoal
                    )
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Settings can be recalibrated anytime in the Settings terminal.",
                color = TextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
