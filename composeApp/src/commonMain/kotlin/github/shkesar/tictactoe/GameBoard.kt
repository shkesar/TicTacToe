package github.shkesar.tictactoe

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private val BoardGap = 12.dp

@Composable
fun GameBoard(state: GameState, onTap: (Int) -> Unit, modifier: Modifier = Modifier) {
    val line = state.winningLine

    // Shake the whole board when a round ends in a draw.
    val shake = remember { Animatable(0f) }
    LaunchedEffect(state.isDraw, state.round) {
        if (state.isDraw) {
            shake.animateTo(0f, keyframes {
                durationMillis = 420
                -14f at 60
                14f at 120
                -10f at 180
                10f at 240
                -5f at 300
                5f at 360
            })
        }
    }

    Box(modifier.graphicsLayer { translationX = shake.value * density }) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(BoardGap)) {
            for (row in 0 until 3) {
                Row(
                    Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(BoardGap),
                ) {
                    for (col in 0 until 3) {
                        val i = row * 3 + col
                        Tile(
                            player = state.board[i],
                            winning = line?.contains(i) == true,
                            dimmed = line != null && i !in line,
                            onClick = { onTap(i) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }
        if (line != null) {
            WinLine(line, BoardGap, Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun Tile(
    player: Player?,
    winning: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(if (pressed && player == null) 0.92f else 1f, label = "press")
    val alpha by animateFloatAsState(if (dimmed) 0.35f else 1f, tween(350), label = "dim")
    val pulse = if (winning) {
        rememberInfiniteTransition(label = "pulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
            label = "pulseScale",
        ).value
    } else 1f

    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .graphicsLayer {
                scaleX = pressScale * pulse
                scaleY = pressScale * pulse
                this.alpha = alpha
            }
            .shadow(
                elevation = if (winning) 16.dp else 6.dp,
                shape = shape,
                ambientColor = if (winning) Neon.Gold else Color.Black,
                spotColor = if (winning) Neon.Gold else Color.Black,
            )
            .clip(shape)
            .background(if (winning) Neon.TileWin else Neon.Tile)
            .border(2.dp, if (winning) Neon.Gold else Neon.Hairline, shape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (player != null) {
            Mark(player, Modifier.fillMaxSize(0.6f))
        }
    }
}

/** An X or O that "draws itself in" with a neon glow. */
@Composable
private fun Mark(player: Player, modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(320, easing = FastOutSlowInEasing))
    }
    val color = player.color

    Canvas(modifier) {
        val stroke = size.minDimension * 0.13f
        val inset = stroke
        val p = progress.value
        if (player == Player.X) {
            val first = (p * 2f).coerceIn(0f, 1f)
            val second = (p * 2f - 1f).coerceIn(0f, 1f)
            val tl = Offset(inset, inset)
            val br = Offset(size.width - inset, size.height - inset)
            val tr = Offset(size.width - inset, inset)
            val bl = Offset(inset, size.height - inset)
            if (first > 0f) glowLine(color, tl, lerp(tl, br, first), stroke)
            if (second > 0f) glowLine(color, tr, lerp(tr, bl, second), stroke)
        } else {
            val arcSize = Size(size.width - inset * 2, size.height - inset * 2)
            for ((w, a) in Neon.glowLayers) {
                drawArc(
                    color = color.copy(alpha = a),
                    startAngle = -90f,
                    sweepAngle = 360f * p,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = stroke * w, cap = StrokeCap.Round),
                )
            }
        }
    }
}

/** Gold line drawn across the three winning cells. */
@Composable
private fun WinLine(line: List<Int>, gap: Dp, modifier: Modifier = Modifier) {
    val progress = remember(line) { Animatable(0f) }
    LaunchedEffect(line) {
        delay(220)
        progress.animateTo(1f, tween(420, easing = FastOutSlowInEasing))
    }

    Canvas(modifier) {
        val g = gap.toPx()
        val cell = (size.width - 2 * g) / 3
        fun center(i: Int) = Offset((i % 3) * (cell + g) + cell / 2, (i / 3) * (cell + g) + cell / 2)

        val a = center(line.first())
        val b = center(line.last())
        val dir = b - a
        val ext = dir / dir.getDistance() * (cell * 0.38f)
        val start = a - ext
        val end = b + ext
        glowLine(Neon.Gold, start, lerp(start, end, progress.value), cell * 0.08f)
    }
}

private fun DrawScope.glowLine(color: Color, start: Offset, end: Offset, stroke: Float) {
    for ((w, a) in Neon.glowLayers) {
        drawLine(color.copy(alpha = a), start, end, strokeWidth = stroke * w, cap = StrokeCap.Round)
    }
}
