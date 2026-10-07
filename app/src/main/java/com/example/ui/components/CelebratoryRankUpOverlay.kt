package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.SystemAmber
import com.example.ui.theme.SystemBackground
import com.example.ui.theme.SystemBorderActive
import com.example.ui.theme.SystemCyan
import com.example.ui.theme.SystemCyanGlow
import com.example.ui.theme.SystemGold
import com.example.ui.theme.SystemGreen
import com.example.ui.theme.SystemPurple
import com.example.ui.theme.SystemSurface
import com.example.ui.theme.SystemSurfaceVariant
import com.example.ui.theme.TextCyan
import com.example.ui.theme.TextGold
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.LevelUpEvent
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val initialX: Float,
    val initialY: Float,
    val speed: Float,
    val angle: Double,
    val size: Float,
    val color: Color
)

@Composable
fun CelebratoryRankUpOverlay(
    event: LevelUpEvent,
    onDismiss: () -> Unit
) {
    // 1. Spring scale animation for the badge
    val badgeScale = remember { Animatable(0.2f) }
    LaunchedEffect(Unit) {
        badgeScale.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    // 2. Infinite rotation and pulse for background celestial rays and aura
    val infiniteTransition = rememberInfiniteTransition(label = "celebration_fx")
    val rotationAngle = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rays_rotation"
    )

    val shockwaveExpansion = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shockwave"
    )

    val particleDrift = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particles"
    )

    // Generate confetti particles
    val particles = remember {
        val colors = listOf(SystemCyan, SystemGold, SystemPurple, SystemGreen, Color.White, SystemAmber)
        List(40) {
            Particle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                speed = 40f + Random.nextFloat() * 100f,
                angle = Random.nextDouble(0.0, Math.PI * 2),
                size = 4f + Random.nextFloat() * 6f,
                color = colors.random()
            )
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.88f))
                .testTag("celebratory_rank_up_overlay"),
            contentAlignment = Alignment.Center
        ) {
            // Fullscreen Dynamic Particle & Energy Shockwave Canvas
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Expanding Shockwave Ring
                val shockRadius = 140.dp.toPx() * shockwaveExpansion.value
                val shockAlpha = (1f - (shockwaveExpansion.value - 0.2f) / 1.2f).coerceIn(0f, 0.7f)
                drawCircle(
                    color = if (event.isRankUp) SystemGold.copy(alpha = shockAlpha) else SystemCyan.copy(alpha = shockAlpha),
                    radius = shockRadius,
                    center = Offset(cx, cy - 80f),
                    style = Stroke(width = 4.dp.toPx())
                )

                // Secondary Shockwave
                val secRadius = 80.dp.toPx() * shockwaveExpansion.value
                drawCircle(
                    color = SystemPurple.copy(alpha = shockAlpha * 0.6f),
                    radius = secRadius,
                    center = Offset(cx, cy - 80f),
                    style = Stroke(width = 2.dp.toPx())
                )

                // Rotating Celestial Rays behind badge
                val numRays = 16
                val rayLength = 260.dp.toPx()
                for (i in 0 until numRays) {
                    val rad = Math.toRadians((rotationAngle.value + (i * (360f / numRays))).toDouble())
                    val endX = cx + (cos(rad) * rayLength).toFloat()
                    val endY = (cy - 80f) + (sin(rad) * rayLength).toFloat()
                    drawLine(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                if (event.isRankUp) SystemGold.copy(alpha = 0.45f) else SystemCyan.copy(alpha = 0.45f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy - 80f),
                            radius = rayLength
                        ),
                        start = Offset(cx, cy - 80f),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx()
                    )
                }

                // Floating Confetti / Embers
                particles.forEach { p ->
                    val progress = (particleDrift.value + p.initialY) % 1f
                    val px = (p.initialX * size.width + cos(p.angle) * p.speed * progress).toFloat()
                    val py = (p.initialY * size.height - progress * size.height * 0.4f + sin(p.angle) * 30f).toFloat()
                    val alpha = (1f - progress).coerceIn(0.2f, 1f)
                    drawCircle(
                        color = p.color.copy(alpha = alpha),
                        radius = p.size,
                        center = Offset(px % size.width, py % size.height)
                    )
                }
            }

            // Main Holographic Celebration Card
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .scale(badgeScale.value)
                    .background(SystemSurface.copy(alpha = 0.95f), CutCornerShape(16.dp))
                    .border(
                        2.dp,
                        if (event.isRankUp) SystemGold else SystemBorderActive,
                        CutCornerShape(16.dp)
                    )
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top System Status Pill
                    Box(
                        modifier = Modifier
                            .background(
                                if (event.isRankUp) SystemGold.copy(alpha = 0.2f) else SystemCyan.copy(alpha = 0.2f),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (event.isRankUp) SystemGold else SystemCyan,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = if (event.isRankUp) SystemGold else SystemCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(
                                text = if (event.isRankUp) "HUNTER RANK PROMOTION!" else "QUEST LEVEL UP!",
                                color = if (event.isRankUp) TextGold else TextCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title
                    Text(
                        text = if (event.isRankUp) "ARISE • NEW RANK!" else "SYSTEM LEVEL UP!",
                        color = TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )

                    if (!event.questTitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Commission: \"${event.questTitle}\"",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Promoted Rank Badge
                    HunterRankBadge(
                        rank = event.newRank,
                        modifier = Modifier.scale(1.25f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "HUNTER LEVEL ${event.newLevel}",
                        color = if (event.isRankUp) SystemGold else SystemCyanGlow,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rewards Summary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SystemSurfaceVariant, RoundedCornerShape(8.dp))
                            .border(1.dp, SystemBorderActive.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "STAT POINTS GAINED:",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "+${event.statPointsGained} POINTS",
                                    color = SystemGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "RECOVERY STATUS:",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = "HP & MP RESTORED",
                                    color = SystemGreen,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Dismiss Action Button
                    SystemButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "ACCEPT & ARISE",
                        containerColor = if (event.isRankUp) SystemGold else SystemCyan,
                        contentColor = SystemBackground,
                        testTag = "celebratory_accept_button",
                        onClick = onDismiss
                    )
                }
            }
        }
    }
}
