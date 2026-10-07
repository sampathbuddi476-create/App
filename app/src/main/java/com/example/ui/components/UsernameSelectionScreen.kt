package com.example.ui.components

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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun UsernameSelectionScreen(
    initialUsername: String = "Shadow_Hunter",
    suggestedNames: List<String> = emptyList(),
    onRerollSuggestions: () -> List<String> = { emptyList() },
    onCheckAvailability: suspend (String) -> Boolean = { true },
    onConfirmUsername: (String) -> Unit
) {
    var hunterCodename by remember { mutableStateOf(initialUsername) }
    var currentSuggestions by remember { mutableStateOf(suggestedNames) }
    var isCheckingName by remember { mutableStateOf(false) }
    var isNameAvailable by remember { mutableStateOf<Boolean?>(null) }
    var nameValidationMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(hunterCodename) {
        val trimmed = hunterCodename.trim()
        if (trimmed.length < 3) {
            isNameAvailable = false
            nameValidationMessage = "Codename must be at least 3 characters"
            return@LaunchedEffect
        }
        if (trimmed.length > 20) {
            isNameAvailable = false
            nameValidationMessage = "Codename maximum 20 characters"
            return@LaunchedEffect
        }

        isCheckingName = true
        delay(350)
        val available = onCheckAvailability(trimmed)
        isCheckingName = false
        isNameAvailable = available
        nameValidationMessage = if (available) "✓ Codename available for registration" else "✗ Already claimed by another Hunter"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
            .testTag("username_selection_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = SystemCyan,
                modifier = Modifier.size(52.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "[ SYSTEM STEP 2 • HUNTER CODENAME ]",
                color = SystemCyan,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "CLAIM YOUR IDENTITY",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Write your unique Hunter codename or choose one of the recommended titles below. Every hunter on the leaderboard must hold a distinct moniker.",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            SystemWindow(
                title = "HUNTER CODENAME REGISTRY",
                systemTag = "EXCLUSIVE ID"
            ) {
                OutlinedTextField(
                    value = hunterCodename,
                    onValueChange = { if (it.length <= 20) hunterCodename = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step2_username_input"),
                    placeholder = { Text("Choose codename (e.g. Shadow_Monarch)", color = TextMuted, fontSize = 12.sp) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = if (isNameAvailable == true) SystemGreen else SystemCyan,
                        unfocusedBorderColor = SystemBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = SystemCyan,
                        focusedContainerColor = SystemSurfaceVariant,
                        unfocusedContainerColor = SystemSurfaceVariant
                    ),
                    trailingIcon = {
                        if (isCheckingName) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = SystemCyan, strokeWidth = 2.dp)
                        } else if (isNameAvailable == true) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Available", tint = SystemGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = nameValidationMessage ?: "",
                    color = if (isNameAvailable == true) SystemGreen else if (isNameAvailable == false) SystemRed else TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECOMMENDED TITLES:",
                        color = TextCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "REROLL 🎲",
                        color = SystemGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .clickable {
                                currentSuggestions = onRerollSuggestions()
                            }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    for (name in currentSuggestions.take(4)) {
                        val isSelected = hunterCodename.trim().equals(name, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) SystemCyan.copy(alpha = 0.2f) else SystemSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) SystemCyan else SystemBorderActive.copy(alpha = 0.4f),
                                    RoundedCornerShape(6.dp)
                                )
                                .clickable { hunterCodename = name }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) SystemCyan else TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SystemCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            SystemButton(
                modifier = Modifier.fillMaxWidth(),
                text = "CONTINUE TO HUNTER ENQUIRY",
                containerColor = SystemCyan,
                contentColor = SystemBackground,
                testTag = "step2_confirm_username_button",
                enabled = isNameAvailable == true,
                onClick = {
                    val name = hunterCodename.trim()
                    if (name.isNotEmpty()) {
                        onConfirmUsername(name)
                    }
                }
            )
        }
    }
}
