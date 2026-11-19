package github.shkesar.tictactoe

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun App() {
    val state = rememberSaveable(saver = GameState.Saver) { GameState() }
    val haptics = LocalHapticFeedback.current

    // AI moves after a short "thinking" pause. Any change of board/round/mode restarts
    // (and so cancels) a pending move — e.g. tapping New Round while the AI thinks.
    LaunchedEffect(state.board, state.current, state.mode, state.difficulty, state.round) {
        if (state.isAiTurn) {
            delay(450)
            state.playAiMove()
        }
    }
    LaunchedEffect(state.winner, state.round) {
        if (state.winner != null) haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Box(Modifier.fillMaxSize().background(Neon.background), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier
                    .fillMaxSize()
                    .widthIn(max = 520.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Title()

                SegmentedControl(
                    options = Mode.entries.map { it.label },
                    selected = state.mode.ordinal,
                    onSelect = { state.selectMode(Mode.entries[it]) },
                )

                AnimatedVisibility(state.mode == Mode.VS_AI) {
                    SegmentedControl(
                        options = Difficulty.entries.map { it.label },
                        selected = state.difficulty.ordinal,
                        onSelect = { state.selectDifficulty(Difficulty.entries[it]) },
                        compact = true,
                    )
                }

                ScoreRow(state)

                BoxWithConstraints(
                    Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    val boardSize = min(maxWidth, maxHeight)
                    GameBoard(
                        state = state,
                        onTap = { i ->
                            if (state.canPlay(i)) {
                                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                state.play(i)
                            }
                        },
                        modifier = Modifier.size(boardSize),
                    )
                }

                StatusText(state)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    PillButton("New Round", onClick = state::newRound, primary = true)
                    PillButton("Reset Score", onClick = state::resetScores)
                }
            }
        }
    }
}

@Composable
private fun Title() {
    Text(
        "TIC · TAC · TOE",
        style = TextStyle(
            brush = Neon.title,
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 2.sp,
            shadow = Shadow(Neon.Cyan.copy(alpha = 0.55f), Offset.Zero, 28f),
        ),
    )
}

@Composable
private fun ScoreRow(state: GameState) {
    val vsAI = state.mode == Mode.VS_AI
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ScoreCard(
            label = if (vsAI) "You (X)" else "Player X",
            value = state.scoreX,
            accent = Neon.Cyan,
            active = !state.isOver && state.current == Player.X,
            modifier = Modifier.weight(1f),
        )
        ScoreCard(
            label = "Draws",
            value = state.draws,
            accent = Neon.Text,
            active = false,
            modifier = Modifier.weight(1f),
        )
        ScoreCard(
            label = if (vsAI) "AI (O)" else "Player O",
            value = state.scoreO,
            accent = Neon.Magenta,
            active = !state.isOver && state.current == Player.O,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatusText(state: GameState) {
    val vsAI = state.mode == Mode.VS_AI
    val winner = state.winner
    val status: Pair<String, Color> = when {
        winner == Player.X -> (if (vsAI) "You win!" else "Player X wins!") to Neon.Cyan
        winner == Player.O -> (if (vsAI) "AI wins, try again!" else "Player O wins!") to Neon.Magenta
        state.isDraw -> "It's a draw!" to Neon.Text
        state.isAiTurn -> "AI is thinking…" to Neon.Muted
        vsAI -> "Your turn" to Neon.Text
        else -> "Player ${state.current}'s turn" to state.current.color
    }

    AnimatedContent(
        targetState = status,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "status",
    ) { (text, color) ->
        Text(text, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}
