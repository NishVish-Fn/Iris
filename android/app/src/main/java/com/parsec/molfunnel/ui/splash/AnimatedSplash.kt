package com.parsec.molfunnel.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
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
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

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
    appName: String = "Iris",
    tagline: String = "Quantum drug discovery, filtered.",
    onFinished: () -> Unit
) {
    val letters = remember(appName) { appName.toList() }

    val unfold = remember { Animatable(0f) }
    val letterProgress = remember(letters) { letters.map { Animatable(0f) } }
    val spacing = remember { Animatable(22f) }
    val typed = remember { Animatable(0f) }
    var typingDone by remember { mutableStateOf(false) }
    val screenAlpha = remember { Animatable(1f) }

    val infinite = rememberInfiniteTransition(label = "loop")
    val wave by infinite.animateFloat(
        0f, 1f, infiniteRepeatable(tween(1600, easing = LinearEasing)), label = "wave"
    )
    val cursorAlpha by infinite.animateFloat(
        1f, 0f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "cursor"
    )
    val floatDeg by infinite.animateFloat(
        0f, 360f, infiniteRepeatable(tween(4200, easing = LinearEasing)), label = "float"
    )
    val rad = Math.toRadians(floatDeg.toDouble())
    val bob = sin(rad).toFloat()          // -1..1, drives up/down float
    val sway = cos(rad).toFloat()         // out of phase, drives 3D sway

    LaunchedEffect(Unit) {
        launch { unfold.animateTo(1f, tween(3200, easing = LinearEasing)) }

        delay(2500)

        launch { spacing.animateTo(2f, tween(1100, easing = FastOutSlowInEasing)) }
        letterProgress.forEachIndexed { i, anim ->
            launch {
                delay(i * 70L)
                anim.animateTo(1f, tween(450, easing = FastOutSlowInEasing))
            }
        }

        delay(letters.size * 70L + 500L)

        typed.animateTo(tagline.length.toFloat(), tween(tagline.length * 35, easing = LinearEasing))
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

            Box(Modifier.size(300.dp), contentAlignment = Alignment.Center) {

                // soft floating shadow (breathes opposite to the float)
                val shadowIn = seg(unfold.value, 0.5f, 1f)
                Canvas(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (-18).dp)
                        .size(170.dp, 34.dp)
                        .graphicsLayer {
                            alpha = shadowIn * (0.65f - 0.25f * bob)
                            scaleX = 1f - 0.12f * bob
                        }
                ) {
                    scale(1f, 0.2f, center) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                listOf(Color(0x55000000), Color.Transparent),
                                center = center,
                                radius = size.width / 2f
                            ),
                            radius = size.width / 2f,
                            center = center
                        )
                    }
                }

                // the unfolding logo, floating and swaying in 3D
                OrbitingMoleculeLogo(
                    unfold = unfold.value,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = bob * 10.dp.toPx()
                            rotationY = 14f * sway
                            cameraDistance = 14f * density
                        }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(spacing.value.dp)) {
                letters.forEachIndexed { i, ch ->
                    val p = letterProgress[i].value
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

            Text(
                text = tagline.take(typed.value.toInt()) + if (!typingDone) "|" else "",
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
