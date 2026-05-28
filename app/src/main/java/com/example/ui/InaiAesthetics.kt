package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun DrawingKolam(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD49F43) // Elegant brass-gold
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val r = w / 4

        // Draw center dot
        drawCircle(
            color = color,
            radius = 6f,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )

        // Draw symmetrical flower/kolam loops
        val path = Path()

        // Loop 1: Top
        path.moveTo(cx, cy)
        path.cubicTo(cx - r, cy - r, cx + r, cy - r, cx, cy)

        // Loop 2: Right
        path.moveTo(cx, cy)
        path.cubicTo(cx + r, cy - r, cx + r, cy + r, cx, cy)

        // Loop 3: Bottom
        path.moveTo(cx, cy)
        path.cubicTo(cx + r, cy + r, cx - r, cy + r, cx, cy)

        // Loop 4: Left
        path.moveTo(cx, cy)
        path.cubicTo(cx - r, cy + r, cx - r, cy - r, cx, cy)

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        // Draw small satellite dots
        drawCircle(color = color, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx, cy - r - 10f))
        drawCircle(color = color, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx + r + 10f, cy))
        drawCircle(color = color, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx, cy + r + 10f))
        drawCircle(color = color, radius = 4f, center = androidx.compose.ui.geometry.Offset(cx - r - 10f, cy))
    }
}

@Composable
fun DrawingKolamBorderHorizontal(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFD49F43)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val segments = 10
        val step = w / segments
        val path = Path()

        for (i in 0..segments) {
            val currX = i * step
            val nextX = (i + 1) * step
            val midX = currX + step / 2

            // Wave pattern
            path.moveTo(currX, h / 2)
            path.quadraticTo(midX, 0f, nextX, h / 2)
            path.quadraticTo(midX, h, nextX, h / 2)
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}
