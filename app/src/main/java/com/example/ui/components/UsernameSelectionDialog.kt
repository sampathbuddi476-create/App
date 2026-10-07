package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun UsernameSelectionDialog(
    currentUsername: String,
    suggestedNames: List<String>,
    onRerollSuggestions: () -> List<String>,
    onCheckAvailability: suspend (String) -> Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var usernameInput by remember { mutableStateOf(currentUsername) }
    var suggestions by remember { mutableStateOf(suggestedNames) }
    var isChecking by remember { mutableStateOf(false) }
    var isAvailable by remember { mutableStateOf<Boolean?>(null) }
    var validationMessage by remember { mutableStateOf<String?>(null) }

    // Live debounced availability check
    LaunchedEffect(usernameInput) {
        val trimmed = usernameInput.trim()
        if (trimmed.isBlank()) {
            isAvailable = null
            validationMessage = "Codename cannot be blank"
            return@LaunchedEffect
        }
        if (trimmed.length < 3) {
            isAvailable = false
            validationMessage = "Minimum 3 characters required"
            return@LaunchedEffect
        }
        if (trimmed.length > 20) {
            isAvailable = false
            validationMessage = "Maximum 20 characters allowed"
            return@LaunchedEffect
        }
        if (trimmed == currentUsername) {
            isAvailable = true
            validationMessage = "Current active codename"
            return@LaunchedEffect
        }

        isChecking = true
        validationMessage = "Verifying with Hunter Registry..."
        delay(400) // Debounce

        val available = onCheckAvailability(trimmed)
        isChecking = false
        isAvailable = available
        validationMessage = if (available) {
            "✓ Unique codename available!"
        } else {
            "✗ Already taken by another hunter"
        }
    }

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
                .padding(20.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, SystemCyan, CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SystemSurface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = SystemCyan,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HUNTER CODENAME",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Pick or write your unique Hunter codename. Your codename must not conflict with other registered hunters.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input TextField
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = {
                            if (it.length <= 20) usernameInput = it
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("username_input_field"),
                        singleLine = true,
                        placeholder = { Text("e.g. Shadow_Monarch", color = TextMuted, fontSize = 14.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SystemCyan,
                            unfocusedBorderColor = SystemBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = SystemCyan,
                            focusedContainerColor = SystemSurfaceVariant,
                            unfocusedContainerColor = SystemSurfaceVariant
                        ),
                        trailingIcon = {
                            if (isChecking) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = SystemCyan,
                                    strokeWidth = 2.dp
                                )
                            } else if (isAvailable == true) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Available",
                                    tint = SystemGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isAvailable == false) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = "Taken",
                                    tint = SystemRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Status line
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = validationMessage ?: "",
                            color = when {
                                isChecking -> TextCyan
                                isAvailable == true -> SystemGreen
                                isAvailable == false -> SystemRed
                                else -> TextMuted
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${usernameInput.length}/20",
                            color = TextMuted,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Suggested Codenames
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SUGGESTED CODENAMES:",
                            color = TextCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    suggestions = onRerollSuggestions()
                                }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Casino,
                                contentDescription = "Reroll",
                                tint = SystemGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "REROLL",
                                color = SystemGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Chips grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        suggestions.chunked(2).forEach { rowList ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowList.forEach { name ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(SystemSurfaceVariant)
                                            .border(1.dp, SystemBorderActive.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .clickable {
                                                usernameInput = name
                                            }
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = name,
                                            color = SystemCyan,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SystemButton(
                            modifier = Modifier.weight(1f),
                            text = "CANCEL",
                            containerColor = SystemSurfaceVariant,
                            contentColor = TextSecondary,
                            testTag = "username_cancel_button",
                            onClick = onDismiss
                        )

                        SystemButton(
                            modifier = Modifier.weight(1.3f),
                            text = "CONFIRM",
                            enabled = isAvailable == true && !isChecking && usernameInput.isNotBlank(),
                            containerColor = SystemCyan,
                            contentColor = SystemBackground,
                            testTag = "username_confirm_button",
                            onClick = {
                                if (isAvailable == true) {
                                    onConfirm(usernameInput.trim())
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
