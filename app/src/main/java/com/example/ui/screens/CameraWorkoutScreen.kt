package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.camera.CameraMovementAnalyzer
import com.example.camera.CameraPreviewWithOverlay
import com.example.camera.FormRating
import com.example.camera.MovementPhase
import com.example.camera.TrackingFeedback
import com.example.data.model.ExerciseType
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemCyanGlow
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ActiveWorkoutState

@Composable
fun CameraWorkoutScreen(
    workoutState: ActiveWorkoutState,
    trackingFeedback: TrackingFeedback,
    analyzer: CameraMovementAnalyzer,
    onExerciseChanged: (ExerciseType) -> Unit,
    onToggleLens: () -> Unit,
    onToggleSound: () -> Unit,
    onFinishWorkout: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SystemBackground)
            .testTag("camera_workout_screen")
    ) {
        if (hasCameraPermission) {
            // Live Camera View with Holographic Reticle Overlay
            CameraPreviewWithOverlay(
                modifier = Modifier.fillMaxSize(),
                useFrontCamera = workoutState.isFrontCamera,
                analyzer = analyzer,
                trackingFeedback = trackingFeedback
            )
        } else {
            // Permission fallback message
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                SystemWindow(
                    title = "CAMERA ACCESS REQUIRED",
                    systemTag = "SYSTEM SECURITY",
                    borderColor = SystemAmber
                ) {
                    Text(
                        text = "The Solo Leveling System requires live camera vision to track body biomechanics, rep depth, and movement cadence.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "GRANT CAMERA PERMISSION",
                        testTag = "grant_camera_permission_button",
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    )
                }
            }
        }

        // Top Navigation & Controls Overlay
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CutCornerShape(8.dp))
                        .background(SystemSurface.copy(alpha = 0.85f))
                        .border(1.dp, SystemBorder, CutCornerShape(8.dp))
                        .clickable { onBack() }
                        .testTag("workout_back_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SystemCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Title Pill
                Box(
                    modifier = Modifier
                        .background(SystemSurface.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .border(1.dp, SystemCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "HUNTER TRAINING • ${workoutState.exercise.displayName.uppercase()}",
                        color = TextCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Controls: Camera Flip & Sound
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onToggleLens,
                        modifier = Modifier
                            .size(42.dp)
                            .background(SystemSurface.copy(alpha = 0.85f), CutCornerShape(8.dp))
                            .border(1.dp, SystemBorder, CutCornerShape(8.dp))
                            .testTag("toggle_camera_lens")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cameraswitch,
                            contentDescription = "Flip Camera",
                            tint = SystemCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleSound,
                        modifier = Modifier
                            .size(42.dp)
                            .background(SystemSurface.copy(alpha = 0.85f), CutCornerShape(8.dp))
                            .border(1.dp, SystemBorder, CutCornerShape(8.dp))
                            .testTag("toggle_workout_sound")
                    ) {
                        Icon(
                            imageVector = if (workoutState.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Sound",
                            tint = if (workoutState.isSoundEnabled) SystemGreen else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Exercise Switcher Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ExerciseType.entries.forEach { ex ->
                    val isSelected = ex == workoutState.exercise
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) SystemCyan else SystemSurface.copy(alpha = 0.85f))
                            .border(
                                1.dp,
                                if (isSelected) SystemCyanGlow else SystemBorder,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onExerciseChanged(ex) }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("chip_${ex.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ex.displayName,
                            color = if (isSelected) SystemBackground else TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Bottom Workout HUD
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Movement Phase and Coaching Pill
            Box(
                modifier = Modifier
                    .background(
                        when (trackingFeedback.phase) {
                            MovementPhase.REP_COMPLETE -> SystemGreen.copy(alpha = 0.9f)
                            MovementPhase.BOTTOM_HOLD -> SystemPurple.copy(alpha = 0.9f)
                            MovementPhase.DESCENDING, MovementPhase.ASCENDING -> SystemCyan.copy(alpha = 0.9f)
                            MovementPhase.IDLE -> SystemSurface.copy(alpha = 0.85f)
                        },
                        RoundedCornerShape(20.dp)
                    )
                    .border(1.dp, SystemBorderActive, RoundedCornerShape(20.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${trackingFeedback.phase.label} • ${trackingFeedback.formTip.uppercase()}",
                    color = if (trackingFeedback.phase == MovementPhase.IDLE) TextCyan else SystemBackground,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Holographic Rep Counter Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, SystemCyan.copy(alpha = 0.7f), CutCornerShape(12.dp)),
                shape = CutCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = SystemSurface.copy(alpha = 0.92f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Rep Counter Display driven purely by live camera movement tracking
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${workoutState.repCount}",
                            color = SystemCyan,
                            fontSize = 54.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            modifier = Modifier.testTag("camera_tracked_rep_count")
                        )
                        Text(
                            text = "${workoutState.exercise.unit.uppercase()} TRACKED BY CAMERA",
                            color = SystemGold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Secondary workout metrics: Time, Calories, Form Score
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        MetricPill(
                            title = "TIME",
                            value = formatTime(workoutState.durationSeconds)
                        )
                        MetricPill(
                            title = "BURNED",
                            value = "%.1f kcal".format(workoutState.caloriesBurned)
                        )
                        MetricPill(
                            title = "PERFECT REPS",
                            value = "${workoutState.perfectReps}"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Motion Energy Bar
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "MOTION TRACKING ENERGY",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${trackingFeedback.motionEnergy.toInt()}%",
                                color = SystemCyan,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        LinearProgressIndicator(
                            progress = { (trackingFeedback.motionEnergy / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = SystemCyan,
                            trackColor = SystemSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Complete & Log Workout Button
                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "COMPLETE TRAINING & CLAIM EXP",
                        testTag = "finish_workout_button",
                        containerColor = SystemGreen,
                        contentColor = SystemBackground,
                        onClick = onFinishWorkout
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = TextMuted, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}

private fun formatTime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
