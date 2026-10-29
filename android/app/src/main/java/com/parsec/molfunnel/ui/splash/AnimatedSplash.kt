package com.parsec.molfunnel.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parsec.molfunnel.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max

val SpaceGroteskFontFamily = try {
    FontFamily(
        Font(R.font.space_grotesk, FontWeight.Bold),
        Font(R.font.space_grotesk, FontWeight.Normal)
    )
} catch (e: Throwable) {
    FontFamily.SansSerif
}

@Composable
fun AnimatedSplash(
    appName: String = "MolFunnel",
    tagline: String = "Quantum drug discovery, filtered.",
    onFinished: () -> Unit
) {
    val letters = remember(appName) { appName.toList() }

    // 1. Logo
    val logoScale = remember { Animatable(0.6f) }
    val logoAlpha = remember { Animatable(0f) }
    val logoRotation = remember { Animatable(-18f) }

    // 2. Letters
    val letterProgress = remember(letters) { letters.map { Animatable(0f) } }
    val spacing = remember { Animatable(22f) }

    // 4. Tagline typewriter
    val typed = remember { Animatable(0f) }
    var typingDone by remember { mutableStateOf(false) }

    // Global fade out
    val screenAlpha = remember { Animatable(1f) }

    // 3. Shimmer wave + cursor blink
    val infinite = rememberInfiniteTransition(label = "loop")
    val wave by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing)),
        label = "wave"
    )
    val cursorAlpha by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "cursor"
    )

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(500))
        }
        launch {
            logoRotation.animateTo(0f, spring(dampingRatio = 0.8f, stiffness = 200f))
        }
        launch {
            logoScale.animateTo(1f, spring(dampingRatio = 0.75f, stiffness = 200f))
        }

        delay(600)

        launch { spacing.animateTo(2f, tween(1100, easing = FastOutSlowInEasing)) }
        letterProgress.forEachIndexed { i, anim ->
            launch {
                delay(i * 70L)
                anim.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
            }
        }

        delay(letters.size * 70L + 500L)

        typed.animateTo(
            tagline.length.toFloat(),
            tween(tagline.length * 35, easing = LinearEasing)
        )
        typingDone = true

        delay(700)
        screenAlpha.animateTo(0f, tween(350))
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .alpha(screenAlpha.value),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Image(
                painter = painterResource(R.drawable.app_logo),
                contentDescription = "Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        rotationZ = logoRotation.value
                        alpha = logoAlpha.value
                    }
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.value.dp)) {
                letters.forEachIndexed { i, ch ->
                    val p = letterProgress[i].value

                    // highlight wave: a soft gray band travelling left to right
                    val pos = wave * (letters.size + 4) - 2
                    val band = max(0f, 1f - abs(pos - i) / 2f)
                    val color = lerp(Color.Black, Color(0xFF9A9A9A), band)

                    Text(
                        text = ch.toString(),
                        style = TextStyle(
                            fontFamily = SpaceGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp,
                            color = if (p >= 1f) color else Color.Black
                        ),
                        modifier = Modifier.graphicsLayer {
                            alpha = p
                            translationY = (1f - p) * 70f
                            scaleX = 0.8f + 0.2f * p
                            scaleY = 0.8f + 0.2f * p
                        }
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val shown = tagline.take(typed.value.toInt())
            val cursor = if (!typingDone) "|" else ""
            Text(
                text = shown + cursor,
                style = TextStyle(
                    fontFamily = SpaceGroteskFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp,
                    color = Color(0xFF555555)
                ),
                modifier = Modifier.alpha(if (typingDone) 1f else 0.6f + 0.4f * cursorAlpha)
            )
        }
    }
}
