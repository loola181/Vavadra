package com.vavarda.clicker

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal enum class StartupPhase(
    @StringRes val titleRes: Int,
    @StringRes val bodyRes: Int,
    val progressTarget: Float
) {
    BOOTSTRAP(
        titleRes = R.string.loading_phase_bootstrap,
        bodyRes = R.string.loading_flavor,
        progressTarget = 0.38f
    ),
    RESTORING(
        titleRes = R.string.loading_phase_restore,
        bodyRes = R.string.loading_restore_flavor,
        progressTarget = 0.84f
    )
}

@Composable
internal fun StartupLoadingScreen(phase: StartupPhase) {
    val haloPulse by rememberInfiniteTransition(label = "loading_halo")
        .animateFloat(
            initialValue = 0.92f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loading_halo_value"
        )
    val emblemScale by rememberInfiniteTransition(label = "loading_emblem")
        .animateFloat(
            initialValue = 0.985f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "loading_emblem_value"
        )
    val progressValue by animateFloatAsState(
        targetValue = phase.progressTarget,
        animationSpec = tween(durationMillis = 520, easing = FastOutSlowInEasing),
        label = "startup_progress_value"
    )
    val sheenOffset by rememberInfiniteTransition(label = "loading_sheen")
        .animateFloat(
            initialValue = -220f,
            targetValue = 420f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "loading_sheen_value"
        )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D0718),
                        Color(0xFF140B24),
                        Color(0xFF090511)
                    )
                )
            )
    ) {
        VavardaBackdrop(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.62f)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x553A1E6D),
                            Color.Transparent
                        ),
                        center = Offset(540f, 760f),
                        radius = 980f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp, vertical = 40.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(168.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0x553F1F7D),
                        radius = size.minDimension * 0.48f * haloPulse,
                        center = center
                    )
                    drawCircle(
                        color = Color(0x22FFFFFF),
                        radius = size.minDimension * 0.34f,
                        center = center,
                        style = Stroke(width = 6f)
                    )
                }

                Card(
                    shape = RoundedCornerShape(34.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC1A1030)),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                    modifier = Modifier.graphicsLayer(
                        scaleX = emblemScale,
                        scaleY = emblemScale
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .size(118.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x663F1F7D),
                                        Color.Transparent
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(82.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                shape = RoundedCornerShape(999.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0x332B1B49)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
            ) {
                Text(
                    text = stringResource(phase.titleRes),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    color = Color(0xFFF7E9C8),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.title_game),
                color = Color(0xFFF6EEFF),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 28.sp,
                fontFamily = MaterialTheme.typography.displayLarge.fontFamily
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(phase.bodyRes),
                color = Color(0xFFD9CBFF),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .width(228.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color(0x332B1B49))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progressValue)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6B42B5),
                                    Color(0xFFFFD978),
                                    Color(0xFF8E62E9)
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                start = Offset(sheenOffset, 0f),
                                end = Offset(sheenOffset + 120f, 60f)
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.state_loading),
                color = Color.White.copy(alpha = 0.94f),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
internal fun VavardaBackdrop(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "backdrop_transition")
    val orbAX by transition.animateFloat(
        initialValue = -120f,
        targetValue = 220f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 11000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdrop_orb_a_x"
    )
    val orbAY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdrop_orb_a_y"
    )
    val orbBX by transition.animateFloat(
        initialValue = 180f,
        targetValue = -160f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 13000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdrop_orb_b_x"
    )
    val orbBY by transition.animateFloat(
        initialValue = -90f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdrop_orb_b_y"
    )

    Canvas(modifier = modifier) {
        drawCircle(
            color = Color(0x182CCEFF),
            radius = size.minDimension * 0.23f,
            center = Offset(size.width * 0.18f + orbAX, size.height * 0.20f + orbAY)
        )
        drawCircle(
            color = Color(0x14FFE082),
            radius = size.minDimension * 0.17f,
            center = Offset(size.width * 0.84f + orbBX, size.height * 0.32f + orbBY)
        )
        drawCircle(
            color = Color(0x0D38D39F),
            radius = size.minDimension * 0.12f,
            center = Offset(size.width * 0.64f - orbAX * 0.45f, size.height * 0.92f - orbBY * 0.18f)
        )
    }
}
