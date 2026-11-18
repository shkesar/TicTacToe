package github.shkesar.tictactoe

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Pill = RoundedCornerShape(50)

/** A pill-shaped row of options with one selected. */
@Composable
fun SegmentedControl(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Row(
        modifier
            .clip(Pill)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), Pill)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        options.forEachIndexed { i, label ->
            val isSelected = i == selected
            Box(
                Modifier
                    .clip(Pill)
                    .background(if (isSelected) Neon.selected else SolidColor(Color.Transparent))
                    .clickable { onSelect(i) }
                    .padding(
                        horizontal = if (compact) 16.dp else 22.dp,
                        vertical = if (compact) 7.dp else 10.dp,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    color = if (isSelected) Color.White else Neon.Muted,
                    fontWeight = FontWeight.Bold,
                    fontSize = if (compact) 13.sp else 15.sp,
                )
            }
        }
    }
}

/** Score tile; glows when it's that player's turn and bumps when the number goes up. */
@Composable
fun ScoreCard(
    label: String,
    value: Int,
    accent: Color,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val bump = remember { Animatable(1f) }
    var last by remember { mutableIntStateOf(value) }
    LaunchedEffect(value) {
        if (value > last) {
            bump.snapTo(1.45f)
            bump.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessMedium))
        }
        last = value
    }
    val border by animateColorAsState(if (active) accent else Neon.Hairline, label = "border")
    val glow by animateDpAsState(if (active) 14.dp else 0.dp, label = "glow")

    val shape = RoundedCornerShape(18.dp)
    Column(
        modifier
            .shadow(glow, shape, ambientColor = accent, spotColor = accent)
            .clip(shape)
            .background(Neon.Card)
            .border(1.5.dp, border, shape)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(label, color = Neon.Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(
            value.toString(),
            color = accent,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.graphicsLayer {
                scaleX = bump.value
                scaleY = bump.value
            },
        )
    }
}

@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.94f else 1f, label = "press")

    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (primary) Modifier.shadow(12.dp, Pill, ambientColor = Neon.Magenta, spotColor = Neon.Magenta)
                else Modifier
            )
            .clip(Pill)
            .background(if (primary) Neon.primary else SolidColor(Neon.Button))
            .border(1.dp, if (primary) Color.Transparent else Color.White.copy(alpha = 0.18f), Pill)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 26.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}
