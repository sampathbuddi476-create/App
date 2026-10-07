package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemRed
import com.example.ui.theme.TextMuted

enum class HunterRank(
    val code: String,
    val title: String,
    val minLevel: Int,
    val color: Color,
    val damageMultiplier: Float
) {
    E_RANK("E", "E-Rank Hunter", 1, TextMuted, 1.0f),
    D_RANK("D", "D-Rank Hunter", 10, SystemGreen, 1.25f),
    C_RANK("C", "C-Rank Hunter", 20, SystemCyan, 1.6f),
    B_RANK("B", "B-Rank Hunter", 35, SystemAmber, 2.0f),
    A_RANK("A", "A-Rank Hunter", 50, SystemRed, 2.6f),
    S_RANK("S", "S-Rank Hunter", 70, SystemGold, 3.5f),
    NATIONAL("NAT", "National Level Hunter", 90, SystemPurple, 5.0f),
    SHADOW_MONARCH("MONARCH", "The Shadow Monarch", 100, Color(0xFFC77DFF), 8.0f);

    companion object {
        fun fromLevel(level: Int): HunterRank {
            return entries.reversed().firstOrNull { level >= it.minLevel } ?: E_RANK
        }
    }
}
