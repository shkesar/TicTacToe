package github.shkesar.TicTacToe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Pure game state for a 3x3 board. Cells hold 'X', 'O' or EMPTY. */
public class Board {
    public static final char EMPTY = ' ';

    /** All 8 winning lines: rows, columns, diagonals. */
    public static final int[][] LINES = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    private final char[] cells = new char[9];

    public Board() {
        reset();
    }

    public void reset() {
        Arrays.fill(cells, EMPTY);
    }

    public char get(int i) {
        return cells[i];
    }

    public boolean isEmpty(int i) {
        return cells[i] == EMPTY;
    }

    public void place(int i, char player) {
        cells[i] = player;
    }

    public void clear(int i) {
        cells[i] = EMPTY;
    }

    public List<Integer> emptyCells() {
        List<Integer> empty = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            if (cells[i] == EMPTY) empty.add(i);
        }
        return empty;
    }

    public boolean isFull() {
        for (char c : cells) {
            if (c == EMPTY) return false;
        }
        return true;
    }

    /** Returns the winning line for {@code player}, or null if they haven't won. */
    public int[] winningLine(char player) {
        for (int[] line : LINES) {
            if (cells[line[0]] == player && cells[line[1]] == player && cells[line[2]] == player) {
                return line;
            }
        }
        return null;
    }

    public boolean hasWon(char player) {
        return winningLine(player) != null;
    }

    public Board copy() {
        Board b = new Board();
        System.arraycopy(cells, 0, b.cells, 0, 9);
        return b;
    }

    public static char opponent(char player) {
        return player == 'X' ? 'O' : 'X';
    }
}
