package com.example.data.model

data class LeaderboardEntry(
    val userId: String = "",
    val hunterName: String = "",
    val hunterRank: String = "E",
    val level: Int = 1,
    val totalReps: Int = 0,
    val dungeonsCleared: Int = 0,
    val photoUrl: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
