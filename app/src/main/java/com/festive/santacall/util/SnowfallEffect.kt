package com.festive.santacall.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import kotlin.random.Random

private data class Snowflake(
    var x: Float,
    var y: Float,
    val radius: Float,
    val speed: Float,
    val alpha: Float,
    var angle: Float
)

@Composable
fun SnowfallEffect(
    modifier: Modifier = Modifier,
    flakeCount: Int = 45
) {
    val snowflakes = remember {
        List(flakeCount) {
            Snowflake(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.003f + 0.0015f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                angle = Random.nextFloat() * 6.28f
            )
        }
    }

    var tick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(16L) // ~60 FPS
            snowflakes.forEach { flake ->
                flake.y += flake.speed
                flake.angle += 0.02f
                flake.x += kotlin.math.sin(flake.angle) * 0.001f
                if (flake.y > 1f) {
                    flake.y = -0.05f
                    flake.x = Random.nextFloat()
                }
                if (flake.x < 0f) flake.x = 1f
                if (flake.x > 1f) flake.x = 0f
            }
            tick++
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        if (tick >= 0) {
            snowflakes.forEach { flake ->
                drawCircle(
                    color = Color.White.copy(alpha = flake.alpha),
                    radius = flake.radius,
                    center = Offset(flake.x * size.width, flake.y * size.height)
                )
            }
        }
    }
}
