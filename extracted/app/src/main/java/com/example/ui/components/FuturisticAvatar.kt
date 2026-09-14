package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.NeonPurplePrimary
import com.example.ui.theme.NeonPurpleSecondary

@Composable
fun FuturisticAvatar(
    size: Dp = 48.dp,
    pulsing: Boolean = true,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        if (pulsing) {
            Canvas(modifier = Modifier.size(size)) {
                val radius = (this.size.minDimension / 2f) * scale
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPurplePrimary.copy(alpha = 0.45f),
                            Color.Transparent
                        )
                    ),
                    radius = radius
                )
                drawCircle(
                    color = NeonPurpleSecondary.copy(alpha = 0.3f),
                    radius = radius * 0.9f,
                    style = Stroke(width = 1.5f)
                )
            }
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * 0.78f)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            DarkCardElevated,
                            DeepBlack
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        listOf(NeonPurpleSecondary, NeonPurplePrimary)
                    ),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "TODO AI Avatar",
                tint = NeonPurpleSecondary,
                modifier = Modifier.size(size * 0.42f)
            )
        }
    }
}
