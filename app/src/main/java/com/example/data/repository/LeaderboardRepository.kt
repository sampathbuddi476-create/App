package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.HunterProfile
import com.example.data.model.HunterRank
import com.example.data.model.LeaderboardEntry
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val TAG = "LeaderboardRepo"

class LeaderboardRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    suspend fun syncHunterProfile(profile: HunterProfile, user: FirebaseUser) {
        try {
            val rank = HunterRank.fromLevel(profile.level).code
            val displayName = user.displayName?.takeIf { it.isNotBlank() } ?: profile.hunterName

            val entry = mapOf(
                "userId" to user.uid,
                "hunterName" to displayName,
                "hunterRank" to rank,
                "level" to profile.level,
                "totalReps" to profile.totalReps,
                "dungeonsCleared" to profile.dungeonsCleared,
                "photoUrl" to (user.photoUrl?.toString() ?: ""),
                "updatedAt" to System.currentTimeMillis()
            )

            db.collection("leaderboard")
                .document(user.uid)
                .set(entry)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync hunter to leaderboard", e)
        }
    }

    suspend fun deleteHunterFromLeaderboard(userId: String) {
        try {
            db.collection("leaderboard")
                .document(userId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete hunter from leaderboard", e)
        }
    }

    suspend fun isUsernameTaken(username: String, excludeUserId: String? = null): Boolean {
        val clean = username.trim()
        if (clean.isBlank()) return true

        // Reserved / Protected athlete names
        val protectedNames = listOf(
            "Apex_Alpha", "Iron_Vanguard", "Thunder_Titan", "Steel_Gladiator",
            "Vortex_Runner", "Cyber_Spartan", "Storm_Breaker", "Hyper_Striker"
        )
        if (protectedNames.any { it.equals(clean, ignoreCase = true) }) {
            return true
        }

        return try {
            val snapshot = db.collection("leaderboard")
                .whereEqualTo("hunterName", clean)
                .get()
                .await()

            val conflict = snapshot.documents.firstOrNull { doc ->
                val uid = doc.getString("userId") ?: doc.id
                excludeUserId == null || uid != excludeUserId
            }
            conflict != null
        } catch (e: Exception) {
            Log.w(TAG, "Username check error: ${e.message}")
            false
        }
    }

    fun generateSuggestedUsernames(baseSeed: String? = null): List<String> {
        val prefixes = listOf(
            "Iron", "Apex", "Titan", "Vortex", "Steel",
            "Thunder", "Cyber", "Velocity", "Phoenix", "Blaze",
            "Zenith", "Quantum", "Hyper", "Vanguard", "Echo", "Atlas"
        )
        val suffixes = listOf(
            "Athlete", "Runner", "Striker", "Lifter", "Gladiator",
            "Warrior", "Spartan", "Force", "Challenger", "Champion",
            "Pulse", "Engine", "Beast", "Master", "Dominator"
        )
        val num1 = (10..99).random()
        val num2 = (100..999).random()

        val list = mutableListOf<String>()
        list.add("${prefixes.random()}_${suffixes.random()}")
        list.add("${prefixes.random()}${suffixes.random()}_$num1")
        if (!baseSeed.isNullOrBlank() && !baseSeed.contains("Jin-woo", ignoreCase = true)) {
            val cleanSeed = baseSeed.trim().replace(" ", "_")
            list.add("${cleanSeed}_$num1")
        } else {
            list.add("Athlete_$num2")
        }
        list.add("${prefixes.random()}_Pro_$num1")
        return list.distinct()
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> = callbackFlow {
        val query = db.collection("leaderboard")
            .orderBy("level", Query.Direction.DESCENDING)
            .limit(50)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Error observing leaderboard: ${error.message}")
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    val uid = doc.getString("userId") ?: doc.id
                    val name = doc.getString("hunterName") ?: "Unknown Hunter"
                    val rank = doc.getString("hunterRank") ?: "E"
                    val level = (doc.getLong("level") ?: 1L).toInt()
                    val reps = (doc.getLong("totalReps") ?: 0L).toInt()
                    val dungeons = (doc.getLong("dungeonsCleared") ?: 0L).toInt()
                    val photo = doc.getString("photoUrl") ?: ""
                    val updated = doc.getLong("updatedAt") ?: 0L

                    LeaderboardEntry(
                        userId = uid,
                        hunterName = name,
                        hunterRank = rank,
                        level = level,
                        totalReps = reps,
                        dungeonsCleared = dungeons,
                        photoUrl = photo,
                        updatedAt = updated
                    )
                }
                trySend(list)
            }
        }

        awaitClose { listener.remove() }
    }
}
