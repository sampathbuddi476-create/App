package com.example.ui.screens

import android.app.Activity
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.LeaderboardEntry
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
fun LeaderboardScreen(
    entries: List<LeaderboardEntry>,
    profile: HunterProfile?,
    currentUser: FirebaseUser?,
    isAuthLoading: Boolean,
    authError: String?,
    onSignInWithGoogle: (Activity) -> Unit,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .padding(16.dp)
            .testTag("leaderboard_screen")
    ) {
        // Top Header: System Cloud Connection & Google Auth Status
        SystemWindow(
            title = "GLOBAL HUNTER LEADERBOARD",
            systemTag = "CLOUD FIRESTORE"
        ) {
            if (currentUser != null) {
                // Logged in with Google
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Google Account",
                            tint = SystemCyan,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = currentUser.displayName ?: profile?.hunterName ?: "Hunter",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "GOOGLE CONNECTED • CLOUD SYNC ACTIVE",
                                color = SystemGreen,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Logout,
                            contentDescription = "Sign Out",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Not logged in: Show Google Sign-in Call to Action
                Column {
                    Text(
                        text = "Sign in with your Google account to record your Hunter progression on the global hall of fame and sync stats across devices.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (authError != null) {
                        Text(
                            text = authError,
                            color = SystemRed,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (isAuthLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = SystemCyan,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONNECTING GOOGLE ACCOUNT...", color = TextCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        SystemButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = "SIGN IN WITH GOOGLE",
                            containerColor = SystemCyan,
                            contentColor = SystemBackground,
                            testTag = "google_sign_in_button",
                            onClick = {
                                if (activity != null) {
                                    onSignInWithGoogle(activity)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Leaderboard List Title
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WORLDWIDE HUNTER RANKINGS",
                color = SystemCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
            Text(
                text = "${entries.size} HUNTERS",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Display Leaderboard Entries
        if (entries.isEmpty()) {
            // Include local player if list is loading or fresh
            val fallbackEntry = LeaderboardEntry(
                userId = currentUser?.uid ?: "local",
                hunterName = currentUser?.displayName ?: profile?.hunterName ?: "Apex_Athlete",
                hunterRank = HunterRank.fromLevel(profile?.level ?: 1).code,
                level = profile?.level ?: 1,
                totalReps = profile?.totalReps ?: 0,
                dungeonsCleared = profile?.dungeonsCleared ?: 0
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    LeaderboardCard(rankPosition = 1, entry = fallbackEntry, isCurrentUser = true)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(entries) { index, entry ->
                    val isCurrent = entry.userId == currentUser?.uid
                    LeaderboardCard(
                        rankPosition = index + 1,
                        entry = entry,
                        isCurrentUser = isCurrent
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardCard(
    rankPosition: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean
) {
    val borderColor = when (rankPosition) {
        1 -> SystemGold
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> if (isCurrentUser) SystemCyan else SystemBorder
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                if (rankPosition <= 3 || isCurrentUser) 1.5.dp else 1.dp,
                borderColor,
                CutCornerShape(8.dp)
            ),
        shape = CutCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) SystemSurfaceVariant else SystemSurface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Position number badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (rankPosition <= 3) borderColor.copy(alpha = 0.2f) else SystemSurfaceVariant,
                            CircleShape
                        )
                        .border(1.dp, borderColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#$rankPosition",
                        color = if (rankPosition <= 3) borderColor else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.hunterName,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(YOU)",
                                color = SystemCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Text(
                        text = "${entry.totalReps} Reps • ${entry.dungeonsCleared} Dungeons Cleared",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Right side: Rank code and Level
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "LV. ${entry.level}",
                    color = SystemGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace
                )
                Box(
                    modifier = Modifier
                        .background(SystemSurfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${entry.hunterRank}-RANK",
                        color = TextCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
