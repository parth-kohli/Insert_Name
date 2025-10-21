package com.example.myapplication.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.material3.Text // Kept for Text composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.room.UserSettings


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = remember { UserSettings(context) }
    val hasCompletedOnboarding by settings.hasCompletedOnboarding.collectAsState(initial = null)
    LaunchedEffect(hasCompletedOnboarding) {
        if (hasCompletedOnboarding == true) {
            // If they've already finished onboarding, call onFinish immediately
            onFinish()
        }
    }
    // --- Define colors ---
    // (Gradient is removed, as it's provided by the parent)
    val highlightColor = Color.White
    val secondaryTextColor = Color.White.copy(alpha = 0.7f)
    val buttonColor = Color.White.copy(alpha = 0.1f)

    // --- Pager setup ---
    val pageCount = 3 // We have 3 screens: 1, 2, and the upcoming 3
    val contentPagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    // Main layout Column. This will be placed on top of your gradient.
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // 4. MAIN SCREEN CONTENT
        HorizontalPager(
            state = contentPagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingScreen1()
                1 -> OnboardingScreen2()
                2 -> OnboardingScreen3() // Placeholder for your next screen
            }
        }

        // 5. PAGER INDICATOR
        HorizontalPagerIndicator(
            pagerState = contentPagerState,
            pageCount = pageCount,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp),
            activeColor = highlightColor,
            inactiveColor = highlightColor.copy(alpha = 0.4f)
        )

        // 6. BOTTOM NAVIGATION (Skip / Next / Get Started)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- SKIP BUTTON ---
            // Shows on all pages except the last one
            val isLastPage = contentPagerState.currentPage == pageCount - 1
            if (isLastPage) {
                // Empty spacer to keep "Get Started" button on the right
                Spacer(modifier = Modifier.height(48.dp))
            } else {
                Text(
                    text = "Skip",
                    color = secondaryTextColor,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            coroutineScope.launch { contentPagerState.scrollToPage(pageCount - 1) }
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // --- NEXT / GET STARTED BUTTON ---
            val buttonText = if (isLastPage) "Get Started" else "Next"
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(buttonColor)
                    .clickable {
                        if (isLastPage) {
                            coroutineScope.launch {
                                settings.setOnboardingCompleted(true)
                                onFinish()
                            }
                        } else {
                            coroutineScope.launch {
                                contentPagerState.animateScrollToPage(
                                    contentPagerState.currentPage + 1
                                )
                            }
                        }
                    }
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = buttonText,
                    color = highlightColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ChaoticNewsVisual(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "newsVisualTransition")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "visualRotation"
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "visualScale"
    )

    Box(
        modifier = modifier
            .size(200.dp)
            .graphicsLayer(
                rotationZ = rotation
            ),
        contentAlignment = Alignment.Center
    ) {
        // Central icon
        Canvas(modifier = Modifier
            .size(80.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 * 0.7f
            drawCircle(Color.Gray.copy(alpha = 0.5f), radius = radius, center = center) // Made slightly more visible
            drawCircle(Color.White, radius = radius * 0.6f, center = center)
            drawLine(
                Color.LightGray, // Was DarkGray
                start = Offset(center.x - radius * 0.4f, center.y),
                end = Offset(center.x + radius * 0.4f, center.y),
                strokeWidth = 4f
            )
            drawLine(
                Color.LightGray, // Was DarkGray
                start = Offset(center.x - radius * 0.2f, center.y + radius * 0.2f),
                end = Offset(center.x + radius * 0.4f, center.y + radius * 0.2f),
                strokeWidth = 4f
            )
        }

        val colors = listOf(
            Color.White.copy(alpha = 0.7f),
            Color(0xFF9C27B0).copy(alpha = 0.7f), // Purple
            Color(0xFF03A9F4).copy(alpha = 0.7f)  // Light Blue
        )
        val lineCount = 10
        val density = LocalDensity.current
        for (i in 0 until lineCount) {
            val angle = (360f / lineCount) * i
            val startRadius = with(density) {
                60.dp.toPx()
            }
            val endRadius = with(density) {
                90.dp.toPx()
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val startX = center.x + startRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val startY = center.y + startRadius * sin(Math.toRadians(angle.toDouble())).toFloat()
                val endX = center.x + endRadius * cos(Math.toRadians(angle.toDouble())).toFloat()
                val endY = center.y + endRadius * sin(Math.toRadians(angle.toDouble())).toFloat()

                drawLine(
                    color = colors[i % colors.size],
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 6f,

                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen1(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp), // Padding for content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Align to top
    ) {
        // Header / Spacer
        Spacer(modifier = Modifier.height(100.dp)) // Increased spacer

        // Visual Representation of Chaos
        ChaoticNewsVisual(modifier = Modifier.padding(vertical = 48.dp)) // Added padding

        // Text Content
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lost in the News Cycle?",
                style = TextStyle( // Replaced MaterialTheme
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White, // Replaced MaterialTheme
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Every story has multiple angles. We sort through the noise to find the clearest view, so you don't have to.",
                style = TextStyle(fontSize = 18.sp), // Replaced MaterialTheme
                color = Color.White.copy(alpha = 0.7f), // Replaced MaterialTheme
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        // Removed Button and Spacer
    }
}

private enum class AnimationState {
    Idle, Scanning, Highlighted
}

@Composable
fun FindTheCenterVisual(modifier: Modifier = Modifier) {
    var animationState by remember { mutableStateOf(AnimationState.Idle) }
    val biasSpectrumColors = listOf(
        Color(0xFF0D47A1), // Dark Blue
        Color(0xFF1976D2), // Medium Blue
        Color(0xFF90CAF9), // Light Blue
        Color.LightGray,   // Center (will be highlighted)
        Color(0xFFF48FB1), // Light Red
        Color(0xFFE53935), // Medium Red
        Color(0xFFB71C1C)  // Dark Red
    )

    val scannerPosition by animateFloatAsState(
        targetValue = if (animationState >= AnimationState.Scanning) 1f else 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 300),
        label = "scannerPosition"
    )

    val highlightScale by animateFloatAsState(
        targetValue = if (animationState == AnimationState.Highlighted) 1.2f else 1f,
        animationSpec = tween(durationMillis = 500),
        label = "highlightScale"
    )

    val highlightStrokeWidth by animateDpAsState(
        targetValue = if (animationState == AnimationState.Highlighted) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 500),
        label = "highlightStrokeWidth"
    )

    LaunchedEffect(Unit) {
        animationState = AnimationState.Scanning
        delay(1500)
        animationState = AnimationState.Highlighted
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Placeholder Headline
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.2f)) // This is fine
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .padding(start = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.4f)) // This is fine
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(Modifier.height(32.dp))

        // This color is used for scanner and highlight, Yellow is good
        val highlightColor = Color(0xFFFFD700) // Using a gold-yellow

        Canvas(modifier = Modifier
            .fillMaxWidth(0.9f)
            .height(60.dp)) {

            val blockCount = biasSpectrumColors.size
            val blockSpacing = 8.dp.toPx()
            val totalSpacing = blockSpacing * (blockCount - 1)
            val blockWidth = (size.width - totalSpacing) / blockCount
            val blockHeight = size.height
            val highlightStroke = highlightStrokeWidth.toPx()

            biasSpectrumColors.forEachIndexed { index, color ->
                val isCenterBlock = index == blockCount / 2
                val currentScale = if (isCenterBlock) highlightScale else 1f
                val scaledWidth = blockWidth * currentScale
                val scaledHeight = blockHeight * currentScale
                val xOffset = index * (blockWidth + blockSpacing)
                val scaleOffsetWidth = (scaledWidth - blockWidth) / 2
                val scaleOffsetHeight = (scaledHeight - blockHeight) / 2

                drawRoundRect(
                    color = color,
                    topLeft = Offset(xOffset - scaleOffsetWidth, -scaleOffsetHeight),
                    size = Size(scaledWidth, scaledHeight),
                    cornerRadius = CornerRadius(4.dp.toPx())
                )

                if (isCenterBlock && highlightStroke > 0) {
                    drawRoundRect(
                        color = highlightColor, // Use defined highlight color
                        topLeft = Offset(xOffset - scaleOffsetWidth, -scaleOffsetHeight),
                        size = Size(scaledWidth, scaledHeight),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = highlightStroke)
                    )
                }
            }

            // 3. Draw Scanner Line
            if (animationState == AnimationState.Scanning) {
                val scannerX = scannerPosition * size.width
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            highlightColor.copy(alpha = 0.8f), // Use defined highlight color
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(scannerX - 2.dp.toPx(), -10.dp.toPx()),
                    size = Size(4.dp.toPx(), size.height + 20.dp.toPx())
                )
            }
        }
    }
}


@Composable
fun OnboardingScreen2(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp), // Padding for content
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top // Align to top
    ) {
        Spacer(modifier = Modifier.height(100.dp)) // Increased spacer

        // Visual Representation
        FindTheCenterVisual(modifier = Modifier.padding(vertical = 48.dp)) // Added padding

        // Text Content
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Find the Unbiased Center.",
                style = TextStyle( // Replaced MaterialTheme
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White, // Replaced MaterialTheme
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "For any major headline, we analyze dozens of sources to pinpoint the most objective, fact-based article. Start with the story, not the spin.",
                style = TextStyle(fontSize = 18.sp), // Replaced MaterialTheme
                color = Color.White.copy(alpha = 0.7f), // Replaced MaterialTheme
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
        // Removed Button and Spacer
    }
}
// Placeholder for the third screen
private enum class AnimationStateS3 { // New enum for Screen 3
    Idle, FocusLeft, FocusCenter, FocusRight
}

@Composable
fun OnboardingScreen3Visual(modifier: Modifier = Modifier) {
    var animationState by remember { mutableStateOf(AnimationStateS3.Idle) }
    val biasSpectrumColors = listOf(
        Color(0xFF0D47A1), // Far Left (Index 0)
        Color(0xFF1976D2), // Mid-Left (Index 1)
        Color(0xFF90CAF9), // Light Blue (Index 2)
        Color.LightGray,   // Center (Index 3)
        Color(0xFFF48FB1), // Light Red (Index 4)
        Color(0xFFE53935), // Mid-Right (Index 5)
        Color(0xFFB71C1C)  // Far Right (Index 6)
    )
    val highlightColor = Color(0xFFFFD700) // Gold-yellow for highlighting

    // --- Animation Targets for each block's scale and offset ---
    val blockCount = biasSpectrumColors.size
    val centerIndex = blockCount / 2

    // Target for horizontal offset when focusing
    val focusedOffsetX = 16.dp

    // Scale for focused block
    val focusedScale = 1.3f
    val normalScale = 1f

    // --- State to track the currently focused index ---
    var focusedIndex by remember { mutableStateOf(-1) } // -1 means none

    // Scale animations for each block based on focus
    val scaleFactors = (0 until blockCount).map { index ->
        animateFloatAsState(
            targetValue = if (index == focusedIndex) focusedScale else normalScale,
            animationSpec = tween(durationMillis = 400),
            label = "scale_${index}"
        ).value
    }

    // Offset animations for each block
    val offsetFactors = (0 until blockCount).map { index ->
        animateDpAsState(
            targetValue = if (index == focusedIndex) focusedOffsetX else 0.dp,
            animationSpec = tween(durationMillis = 400),
            label = "offset_${index}"
        ).value
    }

    // Label alpha animation (only for the currently focused label)
    val labelAlphas = (0 until blockCount).map { index ->
        animateFloatAsState(
            targetValue = if (index == focusedIndex) 1f else 0f,
            animationSpec = tween(durationMillis = 300, delayMillis = 100),
            label = "labelAlpha_${index}"
        ).value
    }

    // --- Animation Sequence ---
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000) // Initial delay
            focusedIndex = 0 // Focus Left
            delay(1500)
            focusedIndex = centerIndex // Focus Center
            delay(1500)
            focusedIndex = blockCount - 1 // Focus Right
            delay(1500)
            focusedIndex = -1 // Reset to Idle
            delay(1000)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 1. Placeholder Headline (Same as Screen 2)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Gray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(8.dp)
                    .padding(start = 12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Gray.copy(alpha = 0.4f))
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(Modifier.height(32.dp))

        // 2. Dynamic Bias Spectrum Visual with Labels
        // Use BoxWithConstraints to get the available width and calculate block sizes
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .height(80.dp)
        ) {
            val density = LocalDensity.current
            val blockSpacing = 8.dp
            val blockHeight = 60.dp

            // --- Correctly calculate baseBlockWidthPx here ---
            val totalSpacingPx = with(density) { blockSpacing.toPx() * (blockCount - 1) }
            val baseBlockWidthPx = (with(density) { maxWidth.toPx() } - totalSpacingPx) / blockCount
            // ---

            Canvas(modifier = Modifier.fillMaxSize()) {
                val baseBlockHeightPx = blockHeight.toPx()

                // --- FIX: Draw unfocused blocks first ---
                biasSpectrumColors.forEachIndexed { index, color ->
                    if (index != focusedIndex) { // Only draw if NOT focused
                        val currentScale = scaleFactors[index] // Will be normalScale
                        val scaledWidthPx = baseBlockWidthPx * currentScale
                        val scaledHeightPx = baseBlockHeightPx * currentScale
                        val initialXOffsetPx = index * (baseBlockWidthPx + blockSpacing.toPx())
                        val xPos = initialXOffsetPx - (scaledWidthPx - baseBlockWidthPx) / 2
                        val yPos = (size.height - scaledHeightPx) / 2f

                        drawRoundRect(
                            color = color,
                            topLeft = Offset(xPos, yPos),
                            size = Size(scaledWidthPx, scaledHeightPx),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }

                // --- FIX: Draw focused block LAST ---
                if (focusedIndex != -1) {
                    val index = focusedIndex
                    val color = biasSpectrumColors[index]
                    val currentScale = scaleFactors[index] // Will be focusedScale
                    val currentOffsetDp = offsetFactors[index]
                    val currentOffsetX = with(density) { currentOffsetDp.toPx() }

                    val scaledWidthPx = baseBlockWidthPx * currentScale
                    val scaledHeightPx = baseBlockHeightPx * currentScale
                    val initialXOffsetPx = index * (baseBlockWidthPx + blockSpacing.toPx())
                    val xPos = initialXOffsetPx - (scaledWidthPx - baseBlockWidthPx) / 2 + currentOffsetX
                    val yPos = (size.height - scaledHeightPx) / 2f

                    // Draw focused block
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(xPos, yPos),
                        size = Size(scaledWidthPx, scaledHeightPx),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                    // Draw highlight stroke
                    drawRoundRect(
                        color = highlightColor,
                        topLeft = Offset(xPos, yPos),
                        size = Size(scaledWidthPx, scaledHeightPx),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        style = Stroke(width = with(density) { 3.dp.toPx() })
                    )
                }
            }

            // Text Labels Overlay
            val labelTexts = listOf("Far Left", "Mid-Left", "Light", "Unbiased", "Light", "Mid-Right", "Far Right")
            val labelColors = listOf(Color.White, Color.White, Color.White, highlightColor, Color.White, Color.White, Color.White)

            labelTexts.forEachIndexed { index, text ->
                if (labelAlphas[index] > 0.01f) {
                    val currentOffsetDp = offsetFactors[index]
                    // We can use baseBlockWidthPx here because it's calculated in the parent BoxWithConstraints
                    val initialXOffsetPx = index * (baseBlockWidthPx + with(density) { blockSpacing.toPx() })
                    val xPos = initialXOffsetPx - (baseBlockWidthPx * scaleFactors[index] - baseBlockWidthPx) / 2 + with(density) { currentOffsetDp.toPx() }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart) // Align to top-start for 0,0 reference
                            .offset(
                                x = with(density) { xPos / density.density }.dp,
                                y = (blockHeight + 4.dp) // Position below the block
                            )
                            .graphicsLayer(alpha = labelAlphas[index])
                            .background(Color.Black.copy(alpha = labelAlphas[index] * 0.4f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = text,
                            color = labelColors[index],
                            fontSize = 12.sp,
                            fontWeight = if (index == centerIndex) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun OnboardingScreen3(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(100.dp))

        OnboardingScreen3Visual(modifier = Modifier.padding(vertical = 48.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Explore the Full Spectrum.",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "After reading the central story, see how different outlets cover the same event. Understand the full conversation and make up your own mind.",
                style = TextStyle(fontSize = 18.sp),
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}