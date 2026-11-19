package github.shkesar.tictactoe

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue

enum class Mode(val label: String) {
    VS_AI("vs AI"), TWO_PLAYER("2 Players")
}

/**
 * All game state, observable by Compose. The AI's move is triggered from the UI
 * (see App.kt) whenever [isAiTurn] becomes true, after a short "thinking" delay.
 */
class GameState(
    mode: Mode = Mode.VS_AI,
    difficulty: Difficulty = Difficulty.MEDIUM,
    board: Board = Board(),
    current: Player = Player.X,
    scoreX: Int = 0,
    scoreO: Int = 0,
    draws: Int = 0,
    round: Int = 0,
) {
    var mode by mutableStateOf(mode)
        private set
    var difficulty by mutableStateOf(difficulty)
        private set
    var board by mutableStateOf(board)
        private set
    var current by mutableStateOf(current)
        private set
    var scoreX by mutableIntStateOf(scoreX)
        private set
    var scoreO by mutableIntStateOf(scoreO)
        private set
    var draws by mutableIntStateOf(draws)
        private set

    /** Increments on every new round; used to restart per-round effects. */
    var round by mutableIntStateOf(round)
        private set

    val winner: Player? get() = board.winner()
    val winningLine: List<Int>? get() = winner?.let { board.winningLine(it) }
    val isDraw: Boolean get() = winner == null && board.isFull
    val isOver: Boolean get() = winner != null || board.isFull
    val isAiTurn: Boolean get() = mode == Mode.VS_AI && current == AI_PLAYER && !isOver

    fun canPlay(index: Int): Boolean = !isOver && !isAiTurn && board[index] == null

    fun play(index: Int) {
        if (isOver || board[index] != null) return
        board = board.place(index, current)
        when (board.winner()) {
            Player.X -> scoreX++
            Player.O -> scoreO++
            null -> if (board.isFull) draws++ else current = current.other
        }
    }

    fun playAiMove() {
        if (isAiTurn) play(AI.forDifficulty(difficulty).nextMove(board, AI_PLAYER))
    }

    fun newRound() {
        board = Board()
        current = Player.X
        round++
    }

    fun resetScores() {
        scoreX = 0
        scoreO = 0
        draws = 0
        newRound()
    }

    fun selectMode(m: Mode) {
        if (m == mode) return
        mode = m
        resetScores()
    }

    fun selectDifficulty(d: Difficulty) {
        if (d == difficulty) return
        difficulty = d
        newRound()
    }

    companion object {
        val HUMAN_PLAYER = Player.X
        val AI_PLAYER = Player.O

        val Saver: Saver<GameState, Any> = listSaver(
            save = {
                listOf(
                    it.mode.ordinal, it.difficulty.ordinal, it.board.encode(), it.current.ordinal,
                    it.scoreX, it.scoreO, it.draws, it.round,
                )
            },
            restore = {
                GameState(
                    mode = Mode.entries[it[0] as Int],
                    difficulty = Difficulty.entries[it[1] as Int],
                    board = Board.decode(it[2] as String),
                    current = Player.entries[it[3] as Int],
                    scoreX = it[4] as Int,
                    scoreO = it[5] as Int,
                    draws = it[6] as Int,
                    round = it[7] as Int,
                )
            },
        )
    }
}
