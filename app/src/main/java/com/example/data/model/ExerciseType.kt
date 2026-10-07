package com.example.data.model

enum class ExerciseType(
    val displayName: String,
    val unit: String,
    val xpPerRep: Int,
    val calPerRep: Float,
    val defaultTarget: Int,
    val instruction: String,
    val focusMuscles: String
) {
    PUSHUPS(
        displayName = "Push-ups",
        unit = "reps",
        xpPerRep = 5,
        calPerRep = 0.5f,
        defaultTarget = 100,
        instruction = "Position camera at side or front. Lower chest down and press fully up.",
        focusMuscles = "Chest, Shoulders, Triceps"
    ),
    SQUATS(
        displayName = "Squats",
        unit = "reps",
        xpPerRep = 5,
        calPerRep = 0.6f,
        defaultTarget = 100,
        instruction = "Stand back so full body is visible. Drop hips below knees and stand erect.",
        focusMuscles = "Quads, Glutes, Hamstrings"
    ),
    JUMPING_JACKS(
        displayName = "Jumping Jacks",
        unit = "jumps",
        xpPerRep = 3,
        calPerRep = 0.3f,
        defaultTarget = 100,
        instruction = "Step back. Jump spreading legs and raising arms overhead in rhythm.",
        focusMuscles = "Full Body Cardio, Calves"
    ),
    SITUPS(
        displayName = "Sit-ups",
        unit = "reps",
        xpPerRep = 5,
        calPerRep = 0.4f,
        defaultTarget = 100,
        instruction = "Place phone facing side profile. Engage core to curl torso upward.",
        focusMuscles = "Abdominals, Core"
    ),
    SHADOW_PUNCHES(
        displayName = "Shadow Punches",
        unit = "strikes",
        xpPerRep = 2,
        calPerRep = 0.25f,
        defaultTarget = 200,
        instruction = "Stand in guard stance. Throw sharp direct punches toward the screen.",
        focusMuscles = "Shoulders, Back, Speed"
    );

    companion object {
        fun fromName(name: String): ExerciseType {
            return entries.firstOrNull { it.name.equals(name, ignoreCase = true) } ?: PUSHUPS
        }
    }
}
