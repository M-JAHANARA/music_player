package com.jahanmusic.app.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jahanmusic.app.R
import com.jahanmusic.app.ui.theme.AccentPurple
import kotlinx.coroutines.delay

/**
 * Loading matches loading.png:
 * bg = img_loading_screen, center logo.png, "JAHAN MUSIC" + "Feel The Music",
 * thin progress bar. Responsive on all phones.
 */
@Composable
fun LoadingScreen(onLoadingComplete: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(1000), label = "alpha"
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.85f,
        animationSpec = tween(1000, easing = EaseOutBack), label = "scale"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        // 2.5s fake load with smooth progress
        val steps = 50
        repeat(steps) {
            delay(50)
            progress = (it + 1) / steps.toFloat()
        }
        onLoadingComplete()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background waves (img_loading_screen.png)
        Image(
            painter = painterResource(id = R.drawable.img_loading_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alphaAnim.value)
                    .scale(scaleAnim.value)
                    .padding(horizontal = 32.dp)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                // App logo itself (logo.png) — big + circular
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Jahan Music Logo",
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "JAHAN MUSIC",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light,
                    color = AccentPurple,
                    letterSpacing = 6.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Feel The Music",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 3.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .width(200.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = AccentPurple,
                    trackColor = AccentPurple.copy(alpha = 0.2f),
                )
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
