package com.example.myapplication

import android.annotation.SuppressLint
import android.window.SplashScreen
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.screens.OnboardingScreen
import kotlinx.coroutines.delay

@Composable
fun app(modifier :Modifier){
    OnboardingScreen(onFinish = {})

}
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun SplashScreen(modifier :Modifier, doneLoading: () -> Unit    ){
    var name by remember { mutableStateOf("") }
    var showCursor by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // typing effect
        val target = "Insert_Name()"
        for (i in target.indices) {
            delay(100L)
            name += target[i]
        }

        // blinking cursor
        while (true) {
            delay(200L) // blink speed
            showCursor = !showCursor
        }
    }
    LaunchedEffect(Unit) {
        delay(2500L)
        doneLoading()
    }
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val width = maxWidth
        val height = maxHeight
        Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
//            Image(
//                painter = painterResource(id = R.drawable.logo),
//                contentDescription = "logo",
//                modifier = Modifier.size(width * 0.5f)
//            )
            Row {
                Text(text = name, fontSize = 30.sp, textAlign = TextAlign.Center)
                Box(
                    modifier = Modifier.width(10.dp), // fixed width for cursor
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (showCursor) Text(text = "│",fontSize = 30.sp) else Text(text = "│",fontSize = 30.sp, color=  colorScheme.background)
                }
            }

            Spacer(modifier = Modifier.height(height/900f *10))
            Text("Made by Insert_Name()", textAlign = TextAlign.End, modifier = Modifier.align(Alignment.End))
            Spacer(modifier = Modifier.height(height/900f *30))
            LoadingScreen()
        }
    }

}
@Composable
fun LoadingScreen() {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        PulsingShape(
            delayMillis = 0,
            shapeType = ShapeType.Circle,
            color = Color(0xFF4FC3F7)
        )
        Spacer(modifier = Modifier.width(20.dp))

        PulsingShape(
            delayMillis = 300,
            shapeType = ShapeType.Square,
            color = Color(0xFF81C784)
        )
        Spacer(modifier = Modifier.width(20.dp))

        PulsingShape(
            delayMillis = 600,
            shapeType = ShapeType.Triangle,
            color = Color(0xFFFFB74D)
        )
    }
}

enum class ShapeType { Circle, Square, Triangle }

@Composable
fun PulsingShape(
    delayMillis: Int,
    shapeType: ShapeType,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")

    // Scale pulse animation
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = ""
    )

    // Rotation synced with pulsing
    val rotation = when (shapeType) {
        ShapeType.Square -> {
            // 0 -> 90 degrees depending on scale
            ((scale - 0.7f) / (1.3f - 0.7f)) * 90f
        }
        ShapeType.Triangle -> {
            // 0 -> 120 degrees depending on scale
            ((scale - 0.7f) / (1.3f - 0.7f)) * 120f
        }
        else -> 0f // circle doesn't rotate
    }

    when (shapeType) {
        ShapeType.Circle -> {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .scale(scale)
                    .background(color, CircleShape)
            )
        }

        ShapeType.Square -> {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .scale(scale)
                    .rotate(rotation)
                    .background(color, RoundedCornerShape(4.dp))
            )
        }

        ShapeType.Triangle -> {
            Canvas(
                modifier = Modifier
                    .size(36.dp)
                    .scale(scale)
                    .rotate(rotation)
            ) {
                val side = size.width
                val height = (side * Math.sqrt(3.0) / 2f).toFloat()

                val path = Path().apply {
                    moveTo(side / 2f, 0f)              // top vertex
                    lineTo(side, height)               // bottom right
                    lineTo(0f, height)                 // bottom left
                    close()
                }
                drawPath(path, color)
            }
        }

    }
}