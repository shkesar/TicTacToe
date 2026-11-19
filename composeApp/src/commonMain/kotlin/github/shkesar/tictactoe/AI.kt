package github.shkesar.tictactoe

import kotlin.random.Random

enum class Difficulty(val label: String) {
    EASY("Easy"), MEDIUM("Medium"), HARD("Hard")
}

/** A computer opponent. Returns the index (0..8) of the cell to play. */
fun interface AI {
    fun nextMove(board: Board, me: Player): Int

    companion object {
        fun forDifficulty(d: Difficulty): AI = when (d) {
            Difficulty.EASY -> EasyAI
            Difficulty.MEDIUM -> MediumAI
            Difficulty.HARD -> HardAI
        }
    }
}

/** Plays a random empty cell. */
object EasyAI : AI {
    override fun nextMove(board: Board, me: Player): Int = board.emptyCells.random()
}

/** Wins if it can, blocks if it must, prefers the centre, otherwise random. */
object MediumAI : AI {
    override fun nextMove(board: Board, me: Player): Int =
        completingCell(board, me)
            ?: completingCell(board, me.other)
            ?: 4.takeIf { board[it] == null }
            ?: board.emptyCells.random()

    /** The empty cell that completes a line of two [player] marks, or null. */
    private fun completingCell(board: Board, player: Player): Int? {
        for (line in Board.LINES) {
            val mine = line.count { board[it] == player }
            val empty = line.filter { board[it] == null }
            if (mine == 2 && empty.size == 1) return empty[0]
        }
        return null
    }
}

/** Minimax: never loses. Picks randomly among equally good moves for variety. */
object HardAI : AI {
    override fun nextMove(board: Board, me: Player): Int {
        val scored = board.emptyCells.map { i -> i to minimax(board.place(i, me), me.other, me, 1) }
        val best = scored.maxOf { it.second }
        return scored.filter { it.second == best }.random(Random).first
    }

    private fun minimax(b: Board, toMove: Player, me: Player, depth: Int): Int {
        if (b.winningLine(me) != null) return 10 - depth
        if (b.winningLine(me.other) != null) return depth - 10
        if (b.isFull) return 0

        val scores = b.emptyCells.map { minimax(b.place(it, toMove), toMove.other, me, depth + 1) }
        return if (toMove == me) scores.max() else scores.min()
    }
}
