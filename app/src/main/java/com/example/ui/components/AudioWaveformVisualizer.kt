package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SleekCardBorder
import com.example.ui.theme.SleekLilac
import com.example.ui.theme.SleekPrimary
import com.example.ui.theme.SleekSecondary
import com.example.ui.theme.SleekSurfaceVariant
import kotlin.math.sin

@Composable
fun AudioWaveformVisualizer(
    isRecording: Boolean,
    amplitude: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(
                color = SleekSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            )
            .border(1.dp, SleekCardBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(60.dp)) {
            val barCount = 28
            val totalWidth = size.width
            val totalHeight = size.height
            val barSpacing = 4.dp.toPx()
            val totalSpacing = barSpacing * (barCount - 1)
            val barWidth = (totalWidth - totalSpacing) / barCount

            val gradientBrush = Brush.verticalGradient(
                colors = if (isRecording) {
                    listOf(
                        Color(0xFFBA1A1A),
                        SleekPrimary,
                        SleekSecondary
                    )
                } else {
                    listOf(
                        SleekPrimary,
                        SleekSecondary,
                        SleekLilac
                    )
                }
            )

            for (i in 0 until barCount) {
                val normalizedIndex = i.toFloat() / barCount
                val waveOffset = sin((normalizedIndex * 4 * Math.PI + phase).toDouble()).toFloat()

                val baseHeightFraction = if (isRecording) {
                    val dynamicAmp = (amplitude * 1.5f).coerceIn(0.15f, 0.95f)
                    val modulation = (waveOffset + 1f) * 0.35f
                    (dynamicAmp * modulation + 0.12f).coerceIn(0.08f, 1f)
                } else {
                    0.08f
                }

                val barHeight = (totalHeight * baseHeightFraction).coerceAtLeast(4.dp.toPx())
                val x = i * (barWidth + barSpacing)
                val y = (totalHeight - barHeight) / 2f

                drawRoundRect(
                    brush = gradientBrush,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2, barWidth / 2)
                )
            }
        }
    }
}

