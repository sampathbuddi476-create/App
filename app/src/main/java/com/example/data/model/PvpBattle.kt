package com.example.data.model

data class PvpBattle(
    val battleId: String = "",
    val exerciseName: String = "PUSHUPS", // Enforced identical exercise for both players
    val targetReps: Int = 25,
    val playerReps: Int = 0,
    val opponentName: String = "Rival Hunter Baek",
    val opponentRank: String = "B",
    val opponentReps: Int = 0,
    val isFinished: Boolean = false,
    val isPlayerWinner: Boolean = false,
    val ratingPointsChange: Int = 0
)
