package com.example.myapplication.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.materialcore.Icon
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { 3 })

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> OnboardingCard(
                    title = "Welcome to MyApp",
                    description = "Get unbiased news and understand the full picture.",
                    illustration = {
                        Icon(
                            Icons.Default.Public,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(120.dp)
                        )
                    }
                )

                1 -> OnboardingCard(
                    title = "How We Analyze Bias",
                    description = "We compare stories across the Left / Center / Right spectrum to give you balanced insights.",
                    illustration = {
                        // simple visual placeholder
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Red.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("L", color = Color.White) }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Gray.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("C", color = Color.White) }

                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Blue.copy(alpha = 0.7f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) { Text("R", color = Color.White) }
                        }
                    }
                )

                2 -> OnboardingCard(
                    title = "Understand Every Angle",
                    description = "View a neutral summary alongside perspectives from different sides.",
                    illustration = {
                        // Mockup UI block
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Text("Neutral Summary", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "A clear unbiased summary of the story.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Divider()
                            Text("Left Perspective", color = Color.Red)
                            Text("Center Perspective", color = Color.Gray)
                            Text("Right Perspective", color = Color.Blue)
                        }
                    }
                )
            }
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.inversePrimary
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {

            if (pagerState.currentPage == 2) {
                Button(onClick = onFinish) {
                    Text("Get Started")
                }
            } else {
                val scope= rememberCoroutineScope ()
                TextButton(
                    onClick = {
                        scope.launch {
                            launch { pagerState.scrollToPage(2) }
                        }
                    }
                ) {
                    Text("Skip")
                }
            }
        }
    }
}

@Composable
fun HorizontalPagerIndicator(pagerState: PagerState, activeColor: Color) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center){
        for (i in 0..2) {
            Box(modifier = Modifier.height(10.dp).width(10.dp).clip(CircleShape).background(if (pagerState.currentPage==i) activeColor else Color.Gray)) {
            }
            Spacer(Modifier.width(3.dp))
        }

    }

}

@Composable
private fun OnboardingCard(
    title: String,
    description: String,
    illustration: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        illustration()
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            description,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
