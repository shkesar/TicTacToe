package github.shkesar.tictactoe

enum class Player {
    X, O;

    val other: Player get() = if (this == X) O else X
}

/** Immutable 3x3 board. Cells are indexed 0..8, row by row. */
data class Board(val cells: List<Player?> = List(9) { null }) {

    operator fun get(index: Int): Player? = cells[index]

    val isFull: Boolean get() = cells.all { it != null }

    val emptyCells: List<Int> get() = cells.indices.filter { cells[it] == null }

    fun place(index: Int, player: Player): Board =
        Board(cells.toMutableList().also { it[index] = player })

    /** The winning line for [player], or null if they haven't won. */
    fun winningLine(player: Player): List<Int>? =
        LINES.firstOrNull { line -> line.all { cells[it] == player } }

    fun winner(): Player? = Player.entries.firstOrNull { winningLine(it) != null }

    fun encode(): String = cells.joinToString("") { it?.name ?: "-" }

    companion object {
        /** All 8 winning lines: rows, columns, diagonals (ends first/last). */
        val LINES: List<List<Int>> = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8),
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8),
            listOf(0, 4, 8), listOf(2, 4, 6),
        )

        fun decode(s: String): Board = Board(s.map {
            when (it) {
                'X' -> Player.X
                'O' -> Player.O
                else -> null
            }
        })
    }
}
