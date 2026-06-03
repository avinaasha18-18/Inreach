package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Recreating the pure CSS hand-loading animation in highly detailed Compose
@Composable
fun InReachSplashLayout(
    state: String,
    onFinished: () -> Unit
) {
    // Background color of the loading stage is faithful to Dribbble's pure blue #4492f4
    val loadingBgColor = Color(0xFF4492F4)
    // Background of the app logo popup is the premium deep cosmic navy of InReach
    val logoBgColor = Color(0xFF0B132B)

    // Smooth transition from loading blue to cosmic navy
    val backgroundColor by animateColorAsState(
        targetValue = if (state == "loading") loadingBgColor else logoBgColor,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "bg_color_transition"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = tween(800)) togetherWith fadeOut(animationSpec = tween(800))
            },
            label = "splash_stages"
        ) { currentStage ->
            when (currentStage) {
                "loading" -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        InReachHandLoadingAnimation(accentColor = loadingBgColor)
                        Spacer(modifier = Modifier.height(36.dp))
                        Text(
                            text = "Initializing Connections...",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Warm Introductions on the Horizon",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                "logo_popup" -> {
                    InReachLogoPopupAnimation()
                }
            }
        }
    }
}

@Composable
fun InReachHandLoadingAnimation(accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "finger_loading")
    val progress = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.wrapContentSize()
    ) {
        val calculatedProgress = progress.value

        // Finger 1 (Timing active from 20% to 41%)
        FingerItemComponent(
            progress = calculatedProgress,
            activeStart = 20f,
            activeEnd = 41f,
            peakStart = 29f,
            peakEnd = 35f,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Finger 2 (Timing active from 24% to 45%)
        FingerItemComponent(
            progress = calculatedProgress,
            activeStart = 24f,
            activeEnd = 45f,
            peakStart = 33f,
            peakEnd = 39f,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Finger 3 (Timing active from 28% to 49%)
        FingerItemComponent(
            progress = calculatedProgress,
            activeStart = 28f,
            activeEnd = 49f,
            peakStart = 37f,
            peakEnd = 43f,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Finger 4 (Timing active from 32% to 53%)
        FingerItemComponent(
            progress = calculatedProgress,
            activeStart = 32f,
            activeEnd = 53f,
            peakStart = 41f,
            peakEnd = 47f,
            accentColor = accentColor
        )

        Spacer(modifier = Modifier.width(2.dp))

        // Last Finger - Thumb (Timing active from 34% to 60%, with rotation)
        ThumbItemComponent(
            progress = calculatedProgress,
            activeStart = 34f,
            activeEnd = 60f,
            peakStart = 43f,
            peakEnd = 50f,
            accentColor = accentColor
        )
    }
}

@Composable
fun FingerItemComponent(
    progress: Float,
    activeStart: Float,
    activeEnd: Float,
    peakStart: Float,
    peakEnd: Float,
    accentColor: Color
) {
    // Interpolate finger padding and height movement based on progress
    val movementRatio = getRatio(progress, activeStart, activeEnd, peakStart, peakEnd)

    // Calculate vertical movement (CSS: padding-bottom changes, which compresses content up)
    val maxTravel = 16f
    val currentTravel = movementRatio * maxTravel

    // Joint lines top movement
    val linesOffset = movementRatio * (-7f)
    // Nail bottom movement
    val nailOffset = movementRatio * 5f

    Box(
        modifier = Modifier
            .size(width = 20.dp, height = 70.dp)
            .graphicsLayer {
                translationY = -currentTravel
            }
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 8.dp, bottomEnd = 8.dp))
            .background(Color.White)
    ) {
        // Double-line joint representation (CSS: span:before, span:after)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .graphicsLayer {
                    translationY = linesOffset
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accentColor)
                    .padding(horizontal = 4.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(accentColor)
                    .padding(horizontal = 4.dp)
            )
        }

        // Circular nail component (CSS: i tag)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 3.dp)
                .graphicsLayer {
                    translationY = -nailOffset
                }
                .size(14.dp)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 7.dp, bottomEnd = 7.dp))
                .background(accentColor)
        )
    }
}

@Composable
fun ThumbItemComponent(
    progress: Float,
    activeStart: Float,
    activeEnd: Float,
    peakStart: Float,
    peakEnd: Float,
    accentColor: Color
) {
    // Thumb rotation & translation
    val ratio = getRatio(progress, activeStart, activeEnd, peakStart, peakEnd)

    // Timings match CSS: top:32px normal -> top:20px/right:2px peak, with normal angle -> -12deg rotation
    val maxOffsetUp = 12f
    val currentOffsetUp = ratio * maxOffsetUp
    val currentOffsetRight = ratio * 2f
    val currentRotation = ratio * (-12f)

    Box(
        modifier = Modifier
            .size(width = 24.dp, height = 70.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Box(
            modifier = Modifier
                .offset(y = (32 - currentOffsetUp).dp, x = currentOffsetRight.dp)
                .size(width = 24.dp, height = 20.dp)
                .graphicsLayer {
                    rotationZ = currentRotation
                }
                .clip(RoundedCornerShape(topStart = 0.dp, topEnd = 5.dp, bottomStart = 0.dp, bottomEnd = 14.dp))
                .background(Color.White)
        ) {
            // Cutout visual representing the joint (CSS: last-finger-item i:after)
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(Color.White)
            ) {
                // Background overlapping curve to create hollow impression
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 15.dp, bottomEnd = 15.dp))
                        .background(accentColor)
                )
            }
        }
    }
}

// Function to interpolate smooth transitions mirroring keyframe intervals
private fun getRatio(progress: Float, activeStart: Float, activeEnd: Float, peakStart: Float, peakEnd: Float): Float {
    return when {
        progress < activeStart || progress > activeEnd -> 0f
        progress in peakStart..peakEnd -> 1f
        progress < peakStart -> {
            val range = peakStart - activeStart
            val delta = progress - activeStart
            // Sine-based easing in
            Math.sin((delta / range) * Math.PI / 2).toFloat()
        }
        else -> {
            val range = activeEnd - peakEnd
            val delta = activeEnd - progress
            // Sine-based easing out
            Math.sin((delta / range) * Math.PI / 2).toFloat()
        }
    }
}

// Re-creating the official InReach brand logo vectors dynamically inside Jetpack Canvas for unlimited crisp definition
@Composable
fun InReachLogoPopupAnimation() {
    var startAnim by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        startAnim = true
    }

    // Elastic scaling and fading entrance for the portal globe logo and brand typography
    val scaleIn by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.3f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "logo_scale"
    )

    val fadeInAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = LinearOutSlowInEasing),
        label = "logo_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scaleIn
                scaleY = scaleIn
                alpha = fadeInAlpha
            }
    ) {
        // High-fidelity brand globe + portal drawing on Canvas
        Box(
            modifier = Modifier.size(240.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f
                val globeRadius = w * 0.35f

                // 1. Draw glowing digital globe (the blue sphere behind)
                val globeGradient = Brush.radialGradient(
                    colors = listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8), Color(0xFF0F172A)),
                    center = Offset(cx, cy),
                    radius = globeRadius
                )
                drawCircle(
                    brush = globeGradient,
                    radius = globeRadius,
                    center = Offset(cx, cy)
                )

                // 2. Draw globe grid lines (latitudes and longitudes)
                val gridColor = Color(0xFF1E3A8A).copy(alpha = 0.5f)
                val strokeWidth = 3f

                // Draw horizontal line ( equator )
                drawLine(
                    color = gridColor,
                    start = Offset(cx - globeRadius, cy),
                    end = Offset(cx + globeRadius, cy),
                    strokeWidth = strokeWidth
                )

                // Draw vertical line ( prime meridian )
                drawLine(
                    color = gridColor,
                    start = Offset(cx, cy - globeRadius),
                    end = Offset(cx, cy + globeRadius),
                    strokeWidth = strokeWidth
                )

                // Curved longitude grid paths
                val leftLongPath = Path().apply {
                    moveTo(cx, cy - globeRadius)
                    quadraticTo(cx - globeRadius * 0.7f, cy, cx, cy + globeRadius)
                }
                val rightLongPath = Path().apply {
                    moveTo(cx, cy - globeRadius)
                    quadraticTo(cx + globeRadius * 0.7f, cy, cx, cy + globeRadius)
                }
                drawPath(leftLongPath, color = gridColor, style = Stroke(width = strokeWidth))
                drawPath(rightLongPath, color = gridColor, style = Stroke(width = strokeWidth))

                // Curved latitude grid paths
                val topLatPath = Path().apply {
                    moveTo(cx - globeRadius * 0.86f, cy - globeRadius * 0.5f)
                    quadraticTo(cx, cy - globeRadius * 0.2f, cx + globeRadius * 0.86f, cy - globeRadius * 0.5f)
                }
                val bottomLatPath = Path().apply {
                    moveTo(cx - globeRadius * 0.86f, cy + globeRadius * 0.5f)
                    quadraticTo(cx, cy + globeRadius * 0.2f, cx + globeRadius * 0.86f, cy + globeRadius * 0.5f)
                }
                drawPath(topLatPath, color = gridColor, style = Stroke(width = strokeWidth))
                drawPath(bottomLatPath, color = gridColor, style = Stroke(width = strokeWidth))

                // 3. Draw the prominent white white portal archway
                // Width of portal is 60dp, height is 100dp
                val portalW = 66.dp.toPx()
                val portalH = 100.dp.toPx()
                val archThickness = 14.dp.toPx()

                // Outline paths centered
                val archLeft = cx - portalW / 2
                val archRight = cx + portalW / 2
                val archBottom = cy + portalH / 2 - 10f
                val archTop = cy - portalH / 2 + portalW / 2

                val archPath = Path().apply {
                    // Outer border
                    moveTo(archLeft, archBottom)
                    lineTo(archLeft, archTop)
                    arcTo(
                        rect = Rect(archLeft, archTop - portalW / 2, archRight, archTop + portalW / 2),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(archRight, archBottom)
                    // Inner gap cutout
                    val innerLeft = archLeft + archThickness
                    val innerRight = archRight - archThickness
                    val innerTop = archTop
                    val innerW = innerRight - innerLeft
                    lineTo(innerRight, archBottom)
                    lineTo(innerRight, innerTop)
                    arcTo(
                        rect = Rect(innerLeft, innerTop - innerW / 2, innerRight, innerTop + innerW / 2),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = -180f,
                        forceMoveTo = false
                    )
                    lineTo(innerLeft, archBottom)
                    close()
                }
                drawPath(
                    path = archPath,
                    color = Color.White,
                    style = Fill
                )

                // 4. Draw the white stylized human figure leaping dynamically through the portal
                // Figure head
                val headX = cx - 2.dp.toPx()
                val headY = cy - 8.dp.toPx()
                drawCircle(
                    color = Color.White,
                    radius = 9.dp.toPx(),
                    center = Offset(headX, headY)
                )

                // Figure sweeping dynamic torso & arms reaching up inside the arch
                // Recreating the vector shape representing leaning back and raising right arm-hand
                val humanPath = Path().apply {
                    // Left leg sweeping of figure
                    moveTo(cx - 30.dp.toPx(), cy + 30.dp.toPx())
                    // Curve sweeps up on the left side of torso
                    quadraticTo(cx - 5.dp.toPx(), cy + 18.dp.toPx(), cx - 18.dp.toPx(), cy + 5.dp.toPx())
                    // Leading up to raised hand
                    quadraticTo(cx - 10.dp.toPx(), cy - 8.dp.toPx(), cx + 20.dp.toPx(), cy - 16.dp.toPx())
                    // Sweeps back from fingertip to body right side
                    quadraticTo(cx + 8.dp.toPx(), cy - 3.dp.toPx(), cx - 1.dp.toPx(), cy + 6.dp.toPx())
                    // Torso outline down to leg
                    quadraticTo(cx - 5.dp.toPx(), cy + 24.dp.toPx(), cx - 30.dp.toPx(), cy + 30.dp.toPx())
                    close()
                }
                drawPath(
                    path = humanPath,
                    color = Color.White,
                    style = Fill
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large clean minimalist brand typography
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "In",
                color = Color.White,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )
            Text(
                text = "Reach",
                color = Color(0xFF3B82F6), // Vibrant Cobalt Accent
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "PORTAL OF SECURE OPPORTUNITIES",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center
        )
    }
}
