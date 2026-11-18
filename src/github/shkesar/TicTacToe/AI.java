package github.shkesar.TicTacToe;

import java.util.List;
import java.util.Random;

/** A computer opponent. Returns the index (0-8) of the cell to play. */
public interface AI {
    Random RANDOM = new Random();

    int nextMove(Board board, char me);

    static AI forDifficulty(Difficulty d) {
        switch (d) {
            case EASY:   return new EasyAI();
            case MEDIUM: return new MediumAI();
            default:     return new HardAI();
        }
    }

    enum Difficulty { EASY, MEDIUM, HARD }

    static int randomEmpty(Board board) {
        List<Integer> empty = board.emptyCells();
        return empty.get(RANDOM.nextInt(empty.size()));
    }

    /** Plays a random empty cell. */
    class EasyAI implements AI {
        @Override
        public int nextMove(Board board, char me) {
            return randomEmpty(board);
        }
    }

    /** Wins if it can, blocks if it must, prefers the centre, otherwise random. */
    class MediumAI implements AI {
        @Override
        public int nextMove(Board board, char me) {
            int move = completingCell(board, me);
            if (move != -1) return move;

            move = completingCell(board, Board.opponent(me));
            if (move != -1) return move;

            if (board.isEmpty(4)) return 4;
            return randomEmpty(board);
        }

        /** The empty cell that completes a line of two {@code player} marks, or -1. */
        private static int completingCell(Board board, char player) {
            for (int[] line : Board.LINES) {
                int count = 0, empty = -1;
                for (int i : line) {
                    if (board.get(i) == player) count++;
                    else if (board.isEmpty(i)) empty = i;
                }
                if (count == 2 && empty != -1) return empty;
            }
            return -1;
        }
    }

    /** Minimax: never loses. Picks randomly among equally good moves for variety. */
    class HardAI implements AI {
        @Override
        public int nextMove(Board board, char me) {
            int bestScore = Integer.MIN_VALUE;
            java.util.ArrayList<Integer> best = new java.util.ArrayList<>();
            Board work = board.copy();
            for (int i : board.emptyCells()) {
                work.place(i, me);
                int score = minimax(work, Board.opponent(me), me, 1);
                work.clear(i);
                if (score > bestScore) {
                    bestScore = score;
                    best.clear();
                    best.add(i);
                } else if (score == bestScore) {
                    best.add(i);
                }
            }
            return best.get(RANDOM.nextInt(best.size()));
        }

        private static int minimax(Board b, char toMove, char me, int depth) {
            if (b.hasWon(me)) return 10 - depth;
            if (b.hasWon(Board.opponent(me))) return depth - 10;
            if (b.isFull()) return 0;

            boolean maximizing = toMove == me;
            int best = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
            for (int i : b.emptyCells()) {
                b.place(i, toMove);
                int score = minimax(b, Board.opponent(toMove), me, depth + 1);
                b.clear(i);
                best = maximizing ? Math.max(best, score) : Math.min(best, score);
            }
            return best;
        }
    }
}
