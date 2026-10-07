package com.example.camera

data class TrackingFeedback(
    val phase: MovementPhase = MovementPhase.IDLE,
    val repCount: Int = 0,
    val motionEnergy: Float = 0f, // 0 - 100
    val verticalDisplacement: Float = 0f, // -1.0 to 1.0 relative to baseline
    val formRating: FormRating = FormRating.GOOD,
    val formTip: String = "Position yourself in camera view",
    val isBodyDetected: Boolean = false,
    val debugMetric: String = ""
)

enum class MovementPhase(val label: String) {
    IDLE("READY"),
    DESCENDING("DESCENDING"),
    BOTTOM_HOLD("BOTTOM HOLD"),
    ASCENDING("ASCENDING"),
    REP_COMPLETE("REP COUNTED!")
}

enum class FormRating(val label: String, val multiplier: Float) {
    PERFECT("PERFECT FORM", 1.25f),
    GOOD("GOOD REP", 1.0f),
    NEEDS_DEPTH("NEEDS MORE DEPTH", 0.8f)
}
