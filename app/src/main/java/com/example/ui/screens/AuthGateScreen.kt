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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AuthGateScreen(
    isLoading: Boolean,
    authError: String?,
    onSignInWithGoogle: (Activity) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("auth_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // System Monolith Header
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = "Monarch Insignia",
                tint = SystemCyan,
                modifier = Modifier.size(54.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "[ SYSTEM INITIALIZATION • STEP 1 ]",
                color = SystemCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "SOLO LEVELING",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
            Text(
                text = "FITNESS SYSTEM • ARISE",
                color = SystemGold,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Information Window
            SystemWindow(
                title = "GATE ACCESS PROTOCOL",
                systemTag = "AUTHENTICATION"
            ) {
                Text(
                    text = "Welcome Hunter. Choose your sign-in method to enter the System. You will select and register your unique Hunter codename immediately after.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Feature benefits
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BenefitRow(
                        icon = Icons.Default.CloudDone,
                        title = "Google Cloud Save",
                        desc = "Persist hunter rank, dungeon clears, and stats across devices.",
                        tint = SystemGreen
                    )
                    BenefitRow(
                        icon = Icons.Default.MilitaryTech,
                        title = "Hunter Association Leaderboard",
                        desc = "Compete with global hunters with your exclusive registered codename.",
                        tint = SystemGold
                    )
                    BenefitRow(
                        icon = Icons.Default.SportsMartialArts,
                        title = "1v1 PvP Colosseum",
                        desc = "Real-time automated randomized duels & exercise matches.",
                        tint = SystemPurple
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (authError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SystemRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .border(1.dp, SystemRed.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = authError,
                        color = SystemRed,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isLoading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = SystemCyan,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "CONNECTING WITH GOOGLE...",
                        color = TextCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                // Primary Google Sign-In Button
                SystemButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "SIGN IN WITH GOOGLE",
                    containerColor = SystemCyan,
                    contentColor = SystemBackground,
                    testTag = "gate_google_sign_in_button",
                    onClick = {
                        if (activity != null) {
                            onSignInWithGoogle(activity)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Guest option
                SystemButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "CONTINUE AS GUEST HUNTER",
                    containerColor = SystemSurfaceVariant,
                    contentColor = TextPrimary,
                    testTag = "gate_continue_guest_button",
                    onClick = onContinueAsGuest
                )
            }
        }
    }
}

@Composable
private fun BenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = desc,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}
