package github.shkesar.tictactoe

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AITest {
    /** Plays a full game and returns the winner, or null for a draw. */
    private fun game(x: AI, o: AI): Player? {
        var board = Board()
        var p = Player.X
        while (true) {
            board = board.place((if (p == Player.X) x else o).nextMove(board, p), p)
            board.winner()?.let { return it }
            if (board.isFull) return null
            p = p.other
        }
    }

    @Test
    fun hardAgainstItselfAlwaysDraws() {
        repeat(50) { assertNull(game(HardAI, HardAI)) }
    }

    @Test
    fun hardNeverLoses() {
        repeat(300) {
            assertEquals(false, game(EasyAI, HardAI) == Player.X, "Hard (O) lost to Easy")
            assertEquals(false, game(MediumAI, HardAI) == Player.X, "Hard (O) lost to Medium")
            assertEquals(false, game(HardAI, EasyAI) == Player.O, "Hard (X) lost to Easy")
        }
    }

    @Test
    fun mediumTakesWinsAndBlocks() {
        // O can win at 8 (diagonal 0-4-8) and must otherwise block X at 2: winning comes first.
        val winBoard = Board.decode("OXX-O----")
        assertEquals(8, MediumAI.nextMove(winBoard, Player.O))

        val blockBoard = Board.decode("XX--O----")
        assertEquals(2, MediumAI.nextMove(blockBoard, Player.O))
    }

    @Test
    fun saveRestoreRoundTrip() {
        val b = Board.decode("XO-XO-X--")
        assertEquals(b, Board.decode(b.encode()))
        assertEquals(Player.X, b.winner())
    }
}
