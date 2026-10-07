package com.example.camera

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.example.data.model.ExerciseType
import kotlin.math.abs

class CameraMovementAnalyzer(
    var currentExercise: ExerciseType = ExerciseType.PUSHUPS,
    private val onTrackingUpdate: (TrackingFeedback) -> Unit,
    private val onRepCounted: (Int, FormRating) -> Unit
) : ImageAnalysis.Analyzer {

    private val gridCols = 32
    private val gridRows = 24
    private var previousGrid: FloatArray? = null

    private var repCounter = 0
    private var currentPhase = MovementPhase.IDLE

    // Motion & Trajectory tracking
    private var baselineCentroidY = 0.5f
    private var hasCalibratedBaseline = false
    private var maxDepthReached = 0f
    private var phaseStartTimeMs = 0L
    private var lastRepTimestampMs = 0L

    // Smoothing filter
    private var smoothedCentroidY = 0.5f
    private var smoothedEnergy = 0f

    fun resetRepCount() {
        repCounter = 0
        currentPhase = MovementPhase.IDLE
        hasCalibratedBaseline = false
        maxDepthReached = 0f
    }

    fun setManualRepCount(count: Int) {
        repCounter = count.coerceAtLeast(0)
    }

    override fun analyze(image: ImageProxy) {
        try {
            val planes = image.planes
            if (planes.isEmpty()) return

            val yBuffer = planes[0].buffer
            val width = image.width
            val height = image.height
            val rowStride = planes[0].rowStride
            val pixelStride = planes[0].pixelStride

            // Sample down to gridCols x gridRows
            val currentGrid = FloatArray(gridCols * gridRows)
            val colStep = width / gridCols
            val rowStep = height / gridRows

            var totalLuma = 0.0
            var weightedRowSum = 0.0
            var sampleCount = 0

            for (r in 0 until gridRows) {
                val rowOffset = (r * rowStep) * rowStride
                for (c in 0 until gridCols) {
                    val index = rowOffset + (c * colStep * pixelStride)
                    if (index < yBuffer.limit()) {
                        val luma = (yBuffer.get(index).toInt() and 0xFF).toFloat() / 255.0f
                        currentGrid[r * gridCols + c] = luma

                        // Center weighted body tracking
                        val weight = 1.0f - abs((c.toFloat() / gridCols) - 0.5f)
                        totalLuma += (luma * weight)
                        weightedRowSum += (luma * weight * r.toDouble())
                        sampleCount++
                    }
                }
            }

            if (sampleCount == 0) return

            // Compute vertical centroid (0.0 to 1.0)
            val instantaneousCentroidY = if (totalLuma > 0.001) {
                ((weightedRowSum / totalLuma) / gridRows.toDouble()).toFloat()
            } else 0.5f

            // Frame-to-frame motion energy
            var frameDiff = 0f
            val prev = previousGrid
            if (prev != null && prev.size == currentGrid.size) {
                var diffSum = 0f
                for (i in currentGrid.indices) {
                    diffSum += abs(currentGrid[i] - prev[i])
                }
                frameDiff = (diffSum / currentGrid.size) * 100f
            }
            previousGrid = currentGrid

            // Low-pass filter for smooth motion curve
            smoothedCentroidY = smoothedCentroidY * 0.7f + instantaneousCentroidY * 0.3f
            smoothedEnergy = smoothedEnergy * 0.65f + frameDiff * 0.35f

            processExerciseCycle(smoothedCentroidY, smoothedEnergy)

        } catch (_: Exception) {
            // Safe guard against hardware frame reading glitches
        } finally {
            image.close()
        }
    }

    private fun processExerciseCycle(centroidY: Float, energy: Float) {
        val now = System.currentTimeMillis()

        // Calibrate baseline when standing/starting still
        if (!hasCalibratedBaseline) {
            baselineCentroidY = centroidY
            hasCalibratedBaseline = true
            phaseStartTimeMs = now
            return
        }

        // Relative vertical displacement: positive = moving down, negative = moving up
        val displacement = centroidY - baselineCentroidY
        val isMoving = energy > 2.0f

        when (currentExercise) {
            ExerciseType.PUSHUPS, ExerciseType.SQUATS -> {
                handleVerticalCycle(displacement, energy, now)
            }
            ExerciseType.JUMPING_JACKS -> {
                handleCardioBounceCycle(energy, now)
            }
            ExerciseType.SITUPS -> {
                handleCoreFlexionCycle(displacement, energy, now)
            }
            ExerciseType.SHADOW_PUNCHES -> {
                handleStrikeVelocityCycle(energy, now)
            }
        }
    }

    private fun handleVerticalCycle(displacement: Float, energy: Float, now: Long) {
        val depthThreshold = if (currentExercise == ExerciseType.PUSHUPS) 0.045f else 0.055f

        when (currentPhase) {
            MovementPhase.IDLE -> {
                if (displacement > depthThreshold * 0.4f && energy > 2.5f) {
                    currentPhase = MovementPhase.DESCENDING
                    phaseStartTimeMs = now
                    maxDepthReached = displacement
                    emitFeedback("Dropping down...", FormRating.GOOD)
                } else {
                    emitFeedback("Begin descent", FormRating.GOOD)
                }
            }
            MovementPhase.DESCENDING -> {
                if (displacement > maxDepthReached) {
                    maxDepthReached = displacement
                }
                if (maxDepthReached >= depthThreshold && displacement < maxDepthReached - 0.012f) {
                    currentPhase = MovementPhase.BOTTOM_HOLD
                    emitFeedback("Bottom reached! Drive upward!", FormRating.GOOD)
                } else {
                    val progress = (displacement / depthThreshold).coerceIn(0f, 1f)
                    emitFeedback(
                        if (progress > 0.8f) "Good depth! Hold briefly" else "Keep lowering down...",
                        FormRating.GOOD
                    )
                }
            }
            MovementPhase.BOTTOM_HOLD -> {
                if (displacement < depthThreshold * 0.5f) {
                    currentPhase = MovementPhase.ASCENDING
                    emitFeedback("Pushing up!", FormRating.GOOD)
                }
            }
            MovementPhase.ASCENDING -> {
                // Return close to original baseline
                if (displacement <= depthThreshold * 0.25f) {
                    if (now - lastRepTimestampMs > 550) { // Cooldown refractory
                        val formRating = when {
                            maxDepthReached >= depthThreshold * 1.3f -> FormRating.PERFECT
                            maxDepthReached >= depthThreshold -> FormRating.GOOD
                            else -> FormRating.NEEDS_DEPTH
                        }
                        repCounter++
                        lastRepTimestampMs = now
                        onRepCounted(repCounter, formRating)

                        currentPhase = MovementPhase.REP_COMPLETE
                        emitFeedback(formRating.label, formRating)
                    } else {
                        currentPhase = MovementPhase.IDLE
                    }
                }
            }
            MovementPhase.REP_COMPLETE -> {
                if (now - lastRepTimestampMs > 400) {
                    currentPhase = MovementPhase.IDLE
                    maxDepthReached = 0f
                }
            }
        }
    }

    private fun handleCardioBounceCycle(energy: Float, now: Long) {
        val cadenceThreshold = 6.0f
        if (energy > cadenceThreshold && (now - lastRepTimestampMs > 600)) {
            repCounter++
            lastRepTimestampMs = now
            val rating = if (energy > 12f) FormRating.PERFECT else FormRating.GOOD
            onRepCounted(repCounter, rating)
            currentPhase = MovementPhase.REP_COMPLETE
            emitFeedback("Jump Rep +1!", rating)
        } else if (now - lastRepTimestampMs > 350) {
            currentPhase = MovementPhase.IDLE
            emitFeedback(if (energy > 2f) "Good rhythm! Keep jumping!" else "Jump and spread arms", FormRating.GOOD)
        }
    }

    private fun handleCoreFlexionCycle(displacement: Float, energy: Float, now: Long) {
        val curlThreshold = 0.05f
        when (currentPhase) {
            MovementPhase.IDLE -> {
                if (displacement < -curlThreshold * 0.4f && energy > 2.5f) {
                    currentPhase = MovementPhase.ASCENDING
                    emitFeedback("Curling up!", FormRating.GOOD)
                }
            }
            MovementPhase.ASCENDING -> {
                if (displacement < -curlThreshold) {
                    currentPhase = MovementPhase.BOTTOM_HOLD
                    emitFeedback("Peak contraction!", FormRating.PERFECT)
                }
            }
            MovementPhase.BOTTOM_HOLD, MovementPhase.DESCENDING -> {
                if (displacement > -curlThreshold * 0.3f && (now - lastRepTimestampMs > 650)) {
                    repCounter++
                    lastRepTimestampMs = now
                    onRepCounted(repCounter, FormRating.PERFECT)
                    currentPhase = MovementPhase.REP_COMPLETE
                    emitFeedback("Situp Complete!", FormRating.PERFECT)
                }
            }
            MovementPhase.REP_COMPLETE -> {
                if (now - lastRepTimestampMs > 400) {
                    currentPhase = MovementPhase.IDLE
                }
            }
        }
    }

    private fun handleStrikeVelocityCycle(energy: Float, now: Long) {
        val strikeThreshold = 8.5f
        if (energy > strikeThreshold && (now - lastRepTimestampMs > 400)) {
            repCounter++
            lastRepTimestampMs = now
            val rating = if (energy > 15f) FormRating.PERFECT else FormRating.GOOD
            onRepCounted(repCounter, rating)
            currentPhase = MovementPhase.REP_COMPLETE
            emitFeedback("Strike hit!", rating)
        } else if (now - lastRepTimestampMs > 300) {
            currentPhase = MovementPhase.IDLE
            emitFeedback("Throw sharp punches at screen", FormRating.GOOD)
        }
    }

    private fun emitFeedback(tip: String, rating: FormRating) {
        val feedback = TrackingFeedback(
            phase = currentPhase,
            repCount = repCounter,
            motionEnergy = smoothedEnergy,
            verticalDisplacement = (smoothedCentroidY - baselineCentroidY),
            formRating = rating,
            formTip = tip,
            isBodyDetected = smoothedEnergy > 0.8f,
            debugMetric = "E: %.1f | D: %.3f".format(smoothedEnergy, smoothedCentroidY - baselineCentroidY)
        )
        onTrackingUpdate(feedback)
    }
}
