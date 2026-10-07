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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Healing
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.example.data.model.InventoryItem
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

@Composable
fun InventoryScreen(
    items: List<InventoryItem>,
    profile: HunterProfile?,
    onUsePotion: (InventoryItem) -> Unit,
    onToggleEquip: (InventoryItem) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("inventory_screen")
    ) {
        SystemWindow(
            title = "HUNTER INVENTORY & ARTIFACTS",
            systemTag = "DIMENSIONAL STORAGE"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Stored Artifacts & Runes (${items.size} Items)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Text(
                    text = "GOLD: ${profile?.gold ?: 0} G",
                    color = TextGold,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO ITEMS IN STORAGE",
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace
                )
            }
        } else {
            items.forEach { item ->
                InventoryItemCard(
                    item = item,
                    onUsePotion = { onUsePotion(item) },
                    onToggleEquip = { onToggleEquip(item) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun InventoryItemCard(
    item: InventoryItem,
    onUsePotion: () -> Unit,
    onToggleEquip: () -> Unit
) {
    val rarityColor = when (item.rarity) {
        "MONARCH" -> Color(0xFFC77DFF)
        "LEGENDARY" -> SystemGold
        "EPIC" -> SystemPurple
        "RARE" -> SystemCyan
        else -> TextMuted
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (item.isEquipped) SystemGreen else rarityColor.copy(alpha = 0.5f),
                CutCornerShape(8.dp)
            ),
        shape = CutCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = SystemSurface.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(rarityColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .border(1.dp, rarityColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.rarity,
                            color = rarityColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.name,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (item.isEquipped) {
                    Text(
                        text = "[EQUIPPED]",
                        color = SystemGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                } else if (item.quantity > 1) {
                    Text(
                        text = "x${item.quantity}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Stat Bonuses
            if (item.statBonusStr > 0 || item.statBonusAgi > 0 || item.statBonusVit > 0) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (item.statBonusStr > 0) Text("+${item.statBonusStr} STR", color = SystemRed, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    if (item.statBonusAgi > 0) Text("+${item.statBonusAgi} AGI", color = SystemCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    if (item.statBonusVit > 0) Text("+${item.statBonusVit} VIT", color = SystemGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (item.category == "POTION") {
                    SystemButton(
                        text = "USE POTION (+50 HP)",
                        containerColor = SystemGreen,
                        contentColor = SystemBackground,
                        testTag = "use_potion_${item.id}",
                        onClick = onUsePotion
                    )
                } else if (item.category == "WEAPON" || item.category == "ACCESSORY" || item.category == "ARMOR") {
                    SystemButton(
                        text = if (item.isEquipped) "UNEQUIP" else "EQUIP ARTIFACT",
                        containerColor = if (item.isEquipped) SystemSurfaceVariant else SystemCyan,
                        contentColor = if (item.isEquipped) TextPrimary else SystemBackground,
                        testTag = "equip_item_${item.id}",
                        onClick = onToggleEquip
                    )
                }
            }
        }
    }
}
