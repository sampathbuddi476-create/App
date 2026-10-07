package com.example.camera

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.components.SystemButton
import com.example.ui.components.SystemWindow
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorder
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemRed
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val TAG = "CameraPreviewView"

@Composable
fun CameraPreviewWithOverlay(
    modifier: Modifier = Modifier,
    useFrontCamera: Boolean = true,
    analyzer: CameraMovementAnalyzer,
    trackingFeedback: TrackingFeedback
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraError by remember { mutableStateOf<String?>(null) }
    var isBindingCamera by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }
    var boundCamera by remember { mutableStateOf<Camera?>(null) }

    // Persistent PreviewView with COMPATIBLE TextureView to prevent surface abandonment
    val previewView = remember(context) {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    // Controlled single-thread executor that cleans up safely on disposal
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    // Bind camera with defensive error handling and automatic recovery
    LaunchedEffect(useFrontCamera, lifecycleOwner, retryCount) {
        isBindingCamera = true
        cameraError = null

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                // Completely release prior bindings
                cameraProvider.unbindAll()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also { analysis ->
                        if (!cameraExecutor.isShutdown) {
                            analysis.setAnalyzer(cameraExecutor, analyzer)
                        }
                    }

                // Verify if requested lens facing exists, fall back to opposite or any available lens
                val primarySelector = if (useFrontCamera) {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } else {
                    CameraSelector.DEFAULT_BACK_CAMERA
                }

                val selectorToUse = if (cameraProvider.hasCamera(primarySelector)) {
                    primarySelector
                } else {
                    val fallbackSelector = if (useFrontCamera) {
                        CameraSelector.DEFAULT_BACK_CAMERA
                    } else {
                        CameraSelector.DEFAULT_FRONT_CAMERA
                    }
                    if (cameraProvider.hasCamera(fallbackSelector)) {
                        fallbackSelector
                    } else {
                        null
                    }
                }

                if (selectorToUse == null) {
                    cameraError = "No camera hardware detected on device or emulator."
                    isBindingCamera = false
                    return@addListener
                }

                boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selectorToUse,
                    preview,
                    imageAnalysis
                )

                cameraError = null
                isBindingCamera = false
            } catch (e: Exception) {
                Log.e(TAG, "Camera initialization error: ${e.message}", e)
                cameraError = "Camera device currently unavailable. Telemetry simulation active."
                isBindingCamera = false
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // Lifecycle clean up on screen exit or unmount
    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error unbinding camera on dispose", e)
            }
            try {
                cameraExecutor.shutdown()
            } catch (_: Exception) {}
        }
    }

    Box(modifier = modifier) {
        // Camera Viewport Surface
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        // Holographic Cyber HUD Canvas
        HolographicTrackingCanvas(
            modifier = Modifier.fillMaxSize(),
            feedback = trackingFeedback
        )

        // Fallback / Recovery Card if camera is locked or emulator virtual camera is reopening
        if (cameraError != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SystemBackground.copy(alpha = 0.88f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                SystemWindow(
                    title = "VISUAL SENSOR STANDBY",
                    systemTag = "TELEMETRY OVERRIDE",
                    borderColor = SystemAmber
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = null,
                            tint = SystemAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "CAMERA SENSOR UNRESPONSIVE",
                            color = SystemAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "The hardware or virtual camera feed was interrupted. Manual repetition logging, haptic feedback, and voice telemetry continue to operate normally.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SystemButton(
                            modifier = Modifier.weight(1f),
                            text = "RETRY SENSOR",
                            containerColor = SystemCyan,
                            contentColor = SystemBackground,
                            testTag = "retry_camera_button",
                            onClick = {
                                retryCount++
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HolographicTrackingCanvas(
    modifier: Modifier = Modifier,
    feedback: TrackingFeedback
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val activeColor = when (feedback.phase) {
            MovementPhase.REP_COMPLETE -> SystemGreen
            MovementPhase.BOTTOM_HOLD -> SystemPurple
            MovementPhase.DESCENDING, MovementPhase.ASCENDING -> SystemCyan
            MovementPhase.IDLE -> Color(0x8800E5FF)
        }

        // 1. Cyber Reticle Corners
        val cornerLen = 40f
        val strokeWidth = 4f
        val margin = 36f

        // Top-Left Corner
        drawLine(activeColor, Offset(margin, margin), Offset(margin + cornerLen, margin), strokeWidth)
        drawLine(activeColor, Offset(margin, margin), Offset(margin, margin + cornerLen), strokeWidth)

        // Top-Right Corner
        drawLine(activeColor, Offset(w - margin, margin), Offset(w - margin - cornerLen, margin), strokeWidth)
        drawLine(activeColor, Offset(w - margin, margin), Offset(w - margin, margin + cornerLen), strokeWidth)

        // Bottom-Left Corner
        drawLine(activeColor, Offset(margin, h - margin), Offset(margin + cornerLen, h - margin), strokeWidth)
        drawLine(activeColor, Offset(margin, h - margin), Offset(margin, h - margin - cornerLen), strokeWidth)

        // Bottom-Right Corner
        drawLine(activeColor, Offset(w - margin, h - margin), Offset(w - margin - cornerLen, h - margin), strokeWidth)
        drawLine(activeColor, Offset(w - margin, h - margin), Offset(w - margin, h - margin - cornerLen), strokeWidth)

        // 2. Vertical Motion Gauge (Right side bar)
        val barRight = w - 24f
        val barTop = h * 0.25f
        val barHeight = h * 0.5f
        val barWidth = 6f

        drawRect(
            color = Color(0x33FFFFFF),
            topLeft = Offset(barRight, barTop),
            size = Size(barWidth, barHeight)
        )

        // Indicator bead representing vertical displacement
        val displacementClamped = (feedback.verticalDisplacement * 5f).coerceIn(-1f, 1f)
        val beadY = barTop + (barHeight * 0.5f) + (displacementClamped * barHeight * 0.45f)
        drawCircle(
            color = activeColor,
            radius = 10f,
            center = Offset(barRight + (barWidth / 2f), beadY)
        )

        // 3. Central Energy Pulse Ring
        val ringRadius = (50f + (feedback.motionEnergy * 1.8f)).coerceIn(50f, 130f)
        drawCircle(
            color = activeColor.copy(alpha = (feedback.motionEnergy / 60f).coerceIn(0.15f, 0.6f)),
            radius = ringRadius,
            center = Offset(w * 0.5f, h * 0.45f),
            style = Stroke(
                width = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
            )
        )

        // Center crosshair
        val cx = w * 0.5f
        val cy = h * 0.45f
        drawLine(activeColor.copy(alpha = 0.5f), Offset(cx - 16f, cy), Offset(cx + 16f, cy), 2f)
        drawLine(activeColor.copy(alpha = 0.5f), Offset(cx, cy - 16f), Offset(cx, cy + 16f), 2f)
    }
}
