package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.ui.components.HunterRankBadge
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
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
import com.google.firebase.auth.FirebaseUser

@Composable
fun SettingsScreen(
    profile: HunterProfile?,
    currentUser: FirebaseUser?,
    isSoundEnabled: Boolean,
    isFrontCameraDefault: Boolean,
    isVoiceEnabled: Boolean = true,
    isAutoSyncEnabled: Boolean = true,
    trainingFocus: String = "BALANCED_MONARCH",
    hunterExperience: String = "NOVICE",
    dailyRepGoal: Int = 100,
    onToggleSound: () -> Unit,
    onToggleCameraLens: () -> Unit,
    onToggleVoice: () -> Unit = {},
    onToggleAutoSync: () -> Unit = {},
    onSpeakSummary: () -> Unit = {},
    onOpenEnquiry: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onSignInWithGoogle: (Activity) -> Unit,
    onSignOut: () -> Unit,
    onManualSync: () -> Unit,
    onOpenTutorial: () -> Unit = {},
    onUpdateUsername: (String) -> Unit = {},
    onCheckUsernameAvailability: suspend (String) -> Boolean = { true },
    onGetSuggestedUsernames: () -> List<String> = { emptyList() }
) {
    val context = LocalContext.current
    val activity = context as? Activity
    var hapticFeedbackEnabled by remember { mutableStateOf(true) }
    var showUsernameDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirmDialog by remember { mutableStateOf(false) }

    if (showUsernameDialog && profile != null) {
        com.example.ui.components.UsernameSelectionDialog(
            currentUsername = profile.hunterName,
            suggestedNames = onGetSuggestedUsernames(),
            onRerollSuggestions = onGetSuggestedUsernames,
            onCheckAvailability = onCheckUsernameAvailability,
            onConfirm = { newName ->
                onUpdateUsername(newName)
                showUsernameDialog = false
            },
            onDismiss = { showUsernameDialog = false }
        )
    }

    if (showDeleteAccountConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirmDialog = false },
            containerColor = SystemBackground,
            modifier = Modifier.border(1.dp, SystemRed, RoundedCornerShape(8.dp)),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = SystemRed,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PURGE HUNTER PROFILE?",
                        color = SystemRed,
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Text(
                    text = "CRITICAL WARNING: This action will permanently wipe your Hunter profile, Level, Rank, Dungeon clears, Inventory artifacts, and remove your records from the global Hunter Association Leaderboard.\n\nAre you sure you want to arise anew as a fresh recruit?",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            },
            confirmButton = {
                SystemButton(
                    text = "CONFIRM PURGE",
                    containerColor = SystemRed,
                    contentColor = TextPrimary,
                    testTag = "confirm_delete_account_button",
                    onClick = {
                        showDeleteAccountConfirmDialog = false
                        onDeleteAccount()
                    }
                )
            },
            dismissButton = {
                SystemButton(
                    text = "ABORT",
                    containerColor = SystemSurfaceVariant,
                    contentColor = TextPrimary,
                    testTag = "cancel_delete_account_button",
                    onClick = { showDeleteAccountConfirmDialog = false }
                )
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        SystemWindow(
            title = "SYSTEM TERMINAL & CONFIGURATION",
            systemTag = "SETTINGS"
        ) {
            Text(
                text = "Control hunter profile telemetry, voice narration, audio effects, sync rules, and training calibration.",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. Hunter Profile & Identity Card
        SystemWindow(
            title = "HUNTER DOSSIER & PROFILE",
            systemTag = "PROFILE CARD",
            borderColor = SystemCyan
        ) {
            if (profile != null) {
                val rank = HunterRank.fromLevel(profile.level)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = SystemCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = profile.hunterName,
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                text = "TITLE: ${profile.title}",
                                color = TextCyan,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "LEVEL ${profile.level} • ${profile.totalReps} REPS LOGGED",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    HunterRankBadge(rank = rank)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SystemButton(
                        modifier = Modifier.weight(1f),
                        text = "CHANGE CODENAME",
                        containerColor = SystemSurfaceVariant,
                        contentColor = SystemCyan,
                        testTag = "settings_change_username_button",
                        onClick = { showUsernameDialog = true }
                    )
                    SystemButton(
                        modifier = Modifier.weight(1f),
                        text = "VOICE BRIEFING 🎙️",
                        containerColor = SystemCyan,
                        contentColor = SystemBackground,
                        testTag = "settings_voice_briefing_button",
                        onClick = onSpeakSummary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Google Account & Cloud Sync Section
        SystemWindow(
            title = "GOOGLE ACCOUNT & CLOUD STORAGE",
            systemTag = if (currentUser != null) "SYNCED" else "GUEST / LOCAL",
            borderColor = if (currentUser != null) SystemGreen else SystemAmber
        ) {
            if (currentUser != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User avatar",
                            tint = SystemCyan,
                            modifier = Modifier.size(34.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser.displayName ?: profile?.hunterName ?: "Hunter",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentUser.email ?: "Google Account Linked",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    SystemButton(
                        text = "SIGN OUT",
                        containerColor = SystemSurfaceVariant,
                        contentColor = SystemRed,
                        testTag = "settings_sign_out_button",
                        onClick = onSignOut
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                SystemButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "SYNC WITH LEADERBOARD NOW",
                    containerColor = SystemCyan,
                    contentColor = SystemBackground,
                    testTag = "settings_manual_sync_button",
                    onClick = onManualSync
                )
            } else {
                Column {
                    Text(
                        text = "You are currently playing as Guest Hunter. Sign in with Google to back up your rank and stats permanently.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "SIGN IN WITH GOOGLE",
                        containerColor = SystemCyan,
                        contentColor = SystemBackground,
                        testTag = "settings_sign_in_button",
                        onClick = {
                            if (activity != null) {
                                onSignInWithGoogle(activity)
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Audio, Voice & Telemetry Toggles
        SystemWindow(
            title = "SYSTEM TOGGLES & TELEMETRY",
            systemTag = "PREFERENCES"
        ) {
            SettingToggleRow(
                icon = Icons.Default.RecordVoiceOver,
                title = "Voice Summaries & Milestones (TTS)",
                subtitle = "Monarch voice announces hunter rank-ups and fitness goals",
                checked = isVoiceEnabled,
                onCheckedChange = { onToggleVoice() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                icon = Icons.Default.VolumeUp,
                title = "Audio Sound Effects",
                subtitle = "Rep count beeps, boss hit impacts, level-up fanfares",
                checked = isSoundEnabled,
                onCheckedChange = { onToggleSound() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                icon = Icons.Default.Vibration,
                title = "Haptic Tactile Feedback",
                subtitle = "Physical vibration pulses upon valid rep completion",
                checked = hapticFeedbackEnabled,
                onCheckedChange = { hapticFeedbackEnabled = it }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                icon = Icons.Default.Cameraswitch,
                title = "Default to Front Camera",
                subtitle = if (isFrontCameraDefault) "Selfie lens active by default" else "Rear lens active by default",
                checked = isFrontCameraDefault,
                onCheckedChange = { onToggleCameraLens() }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleRow(
                icon = Icons.Default.Sync,
                title = "Auto Cloud Sync",
                subtitle = "Automatically update global leaderboard on workout completion",
                checked = isAutoSyncEnabled,
                onCheckedChange = { onToggleAutoSync() }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. Hunter Enquiry & Training Calibration
        SystemWindow(
            title = "HUNTER ENQUIRY & CALIBRATION",
            systemTag = "TRAINING SPECS"
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Focus: ${trainingFocus.replace("_", " ")}", color = TextCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    Text("Goal: $dailyRepGoal Reps/day", color = SystemGold, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Conditioning Level: $hunterExperience",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                SystemButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "RECALIBRATE ENQUIRY SURVEY",
                    containerColor = SystemSurfaceVariant,
                    contentColor = TextPrimary,
                    testTag = "settings_recalibrate_enquiry_button",
                    onClick = onOpenEnquiry
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. System Tutorial & Guide
        SystemWindow(
            title = "TRAINING INSTRUCTION",
            systemTag = "MANUAL"
        ) {
            Text(
                text = "Review the step-by-step interactive manual covering quests, AI camera tracking, 1v1 PvP, and dungeon boss raids.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "OPEN SYSTEM TUTORIAL & GUIDE",
                containerColor = SystemCyan,
                contentColor = SystemBackground,
                testTag = "settings_open_tutorial_button",
                onClick = onOpenTutorial
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 6. Danger Zone: Delete Account
        SystemWindow(
            title = "DANGER ZONE: ACCOUNT DELETION",
            systemTag = "PERMANENT PURGE",
            borderColor = SystemRed
        ) {
            Text(
                text = "Delete your Hunter account and clear all local and leaderboard records.",
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "DELETE ACCOUNT & RESET PROGRESS",
                containerColor = SystemRed.copy(alpha = 0.2f),
                contentColor = SystemRed,
                testTag = "settings_delete_account_button",
                onClick = { showDeleteAccountConfirmDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // App Information Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SOLO LEVELING FITNESS SYSTEM v1.4.0\nOPTIMIZED • COMPOSE & CAMERAX",
                color = TextMuted,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                imageVector = icon,
                contentDescription = null,
                tint = SystemCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 10.sp
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SystemCyan,
                checkedTrackColor = SystemCyan.copy(alpha = 0.3f),
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = SystemSurfaceVariant
            )
        )
    }
}
