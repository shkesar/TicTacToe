package github.shkesar.tictactoe

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Dark neon palette shared by every platform. */
object Neon {
    val BgTop = Color(0xFF0F0C29)
    val BgMid = Color(0xFF1A1A2E)
    val BgBottom = Color(0xFF16213E)

    val Tile = Color(0xFF1B2547)
    val TileWin = Color(0xFF27365F)
    val Card = Color(0xFF1C1F3D)
    val Button = Color(0xFF232849)

    val Cyan = Color(0xFF00F5FF)
    val Magenta = Color(0xFFFF2E97)
    val Blue = Color(0xFF00C6FF)
    val Purple = Color(0xFF7B2FF7)
    val Gold = Color(0xFFFFD166)

    val Text = Color(0xFFE6E9FF)
    val Muted = Color(0xFF8A94C7)
    val Hairline = Color.White.copy(alpha = 0.08f)

    val background = Brush.verticalGradient(listOf(BgTop, BgMid, BgBottom))
    val selected = Brush.horizontalGradient(listOf(Blue, Purple))
    val primary = Brush.horizontalGradient(listOf(Magenta, Purple))
    val title = Brush.horizontalGradient(listOf(Cyan, Magenta))

    /** (stroke width multiplier, alpha) layers that fake a neon glow on any platform. */
    val glowLayers = listOf(3.2f to 0.10f, 2.0f to 0.22f, 1.0f to 1.0f)
}

val Player.color: Color get() = if (this == Player.X) Neon.Cyan else Neon.Magenta
