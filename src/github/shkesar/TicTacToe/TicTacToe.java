package github.shkesar.TicTacToe;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToe extends Application {
    private static ArrayList<Button> cells = new ArrayList<>();

    private static ArrayList<Boolean> filledCells = new ArrayList<>(9);

    private TicTacToeAI aiLogic;

    private Label statusLabel;

    public TicTacToe() {
        statusLabel = new Label("");
        aiLogic = new SmartTicTacToeAI();

        // initialize filledCells list
        for (int i = 0; i < 9; i++) {
            filledCells.add(false);
        }
        clearGridArray();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        GridPane board = new GridPane() {{
            ColumnConstraints column1 = new ColumnConstraints(50, 75, 100);
            RowConstraints row1 = new RowConstraints(50, 75, 100);

            getColumnConstraints().addAll(column1, column1, column1);
            getRowConstraints().addAll(row1, row1, row1);
            for (int index = 0; index < 9; index++) {
                add(createCell(), index % 3, index / 3);
            }
            setHgap(50);
            setVgap(50);
        }};
        Button clearButton = new Button("Clear") {{
            setFont(new Font(20));
            setOnAction(ae -> clearGrid());
        }};

        // layout
        BorderPane root = new BorderPane() {{
            VBox bottomPanel = new VBox(5, statusLabel, clearButton);
            bottomPanel.setAlignment(Pos.CENTER);
            BorderPane.setMargin(bottomPanel, new Insets(20));

            setCenter(board);
            setBottom(bottomPanel);

            setMargin(board, new Insets(25));
            setMaxWidth(375);
            setMaxHeight(450);
        }};

        Scene scene = new Scene(root, 375, 450);

        primaryStage.setMaxWidth(375);
        primaryStage.setMinWidth(300);
        primaryStage.setMaxHeight(500);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tic-Tac-Toe");
        primaryStage.show();
    }

    private Button createCell() {
        Button cell = new Button("") {{
            setMinHeight(50);
            setPrefHeight(75);
            setMaxHeight(100);
            setMinWidth(50);
            setPrefWidth(75);
            setMaxWidth(100);
            setFont(new Font(23));
        }};

        cell.setOnAction(ae -> {
            int clickedCellIndex = cells.indexOf(cell);
            if (filledCells.get(clickedCellIndex)) {
                return;
            } else {
                fillCell("X", clickedCellIndex);
            }

            if (hasWon("X")) {
                statusLabel.setText("You have won");
                disableGrid();
                return;
            }
            if (isGridFull()) {
                statusLabel.setText("Game is draw");
                disableGrid();
                return;
            }

            // AI logic
            int aiIndex = aiLogic.nextMove();
            fillCell("O", aiIndex);

            if (hasWon("O")) {
                statusLabel.setText("Computer has won");
                disableGrid();
            }
        });

        cells.add(cell);
        return cell;
    }

    private static void fillCell(String player, int index) {
        cells.get(index).setText(player);
        filledCells.set(index, true);
    }

    private static void clearGridArray() {
        for (int i = 0; i < 9; i++) {
            filledCells.set(i, false);
        }
        enableGrid();
    }

    private void clearGrid() {
        cells.forEach(cell -> cell.setText(""));
        clearGridArray();
        statusLabel.setText("");
    }

    private static void disableGrid() {
        cells.forEach(cell -> cell.setDisable(true));
    }

    private static void enableGrid() {
        cells.forEach(cell -> cell.setDisable(false));
    }

    private static boolean isGridFull() {
        for (boolean cellFilled : filledCells) {
            if (!cellFilled)
                return false;
        }
        return true;
    }

    private static boolean hasWon(String playerString) {
        // horizontal check
        for (int r = 0; r < 3; r++) {
            if (cells.get(r * 3).getText().equals(playerString) &&
                    cells.get(r * 3 + 1).getText().equals(playerString) &&
                    cells.get(r * 3 + 2).getText().equals(playerString))
                return true;
        }

        // vertical check
        for (int c = 0; c < 3; c++) {
            if (cells.get(c).getText().equals(playerString) &&
                    cells.get(3 + c).getText().equals(playerString) &&
                    cells.get(6 + c).getText().equals(playerString))
            return true;
        }

        // left-to-right down diagonal
        if (cells.get(0).getText().equals(playerString) &&
                cells.get(4).getText().equals(playerString) &&
                cells.get(8).getText().equals(playerString))
            return true;

        // right-to-left down diagonal
        if (cells.get(2).getText().equals(playerString) &&
                cells.get(4).getText().equals(playerString) &&
                cells.get(6).getText().equals(playerString))
            return true;

        return false;
    }

    public interface TicTacToeAI {
        int nextMove();
    }

    @SuppressWarnings("unused")
    class RandomAI implements TicTacToeAI {
        @Override
        public int nextMove() {
            int aiIndex = (int)Math.floor(Math.random() * 9);
            while (filledCells.get(aiIndex)) {
                aiIndex = (int)Math.floor(Math.random() * 9);
            }
            return aiIndex;
        }
    }

    class SmartTicTacToeAI implements TicTacToeAI {
        @Override
        public int nextMove() {
            int aiIndex;

            // winning move check
            aiIndex = getEmptyInLine("o");
            if (aiIndex != -1) {
                return aiIndex;
            }

            // preventive move check
            aiIndex = getEmptyInLine("x");
            if (aiIndex != -1) {
                return aiIndex;
            }

            /*// corner-fill move
            ArrayList<Integer> corners = new ArrayList() {{
                add(0);add(2);add(6);add(8);
            }};
            int cornersFilled = (int)corners.stream().map(index -> cells.get(index).getText().equals("O") ? 1 : 0).reduce(0, (a,b) -> a + b);

            if (cornersFilled == 0) {
                List<Integer> emptyCorners = new ArrayList<>();
                if (isCellEmpty(0)) emptyCorners.add(0);
                if (isCellEmpty(2)) emptyCorners.add(2);
                if (isCellEmpty(6)) emptyCorners.add(6);
                if (isCellEmpty(8)) emptyCorners.add(8);

                return emptyCorners.get((int) Math.floor(Math.random() * emptyCorners.size()));
            } else
            if (cornersFilled == 1) {
                int filledCorner = -1;
                if (isCellFilled(0)) filledCorner = 0;
                if (isCellFilled(2)) filledCorner = 2;
                if (isCellFilled(6)) filledCorner = 6;
                if (isCellFilled(8)) filledCorner = 8;

                if (filledCorner == 0 || filledCorner == 8) {
                    if (isCellFilled(2))
                        return 6;
                    if (isCellFilled(6))
                        return 2;
                    return anyAmong(2, 6);
                }
                else if (filledCorner == 2 || filledCorner == 6) {
                    if (isCellFilled(0))
                        return 8;
                    if (isCellFilled(8))
                        return 0;
                    return anyAmong(0, 8);
                }

                throw new ArrayIndexOutOfBoundsException("Debug: Logic failure");
            }*/

            // random position move
            aiIndex = (int)Math.floor(Math.random() * 9);
            while (filledCells.get(aiIndex)) {
                aiIndex = (int)Math.floor(Math.random() * 9);
            }
            return aiIndex;
        }

        // utility methods
        private boolean isCellEmpty(int index) {
            return cellContent(index).equals("");
        }
        @SuppressWarnings("unused")
        private boolean isCellFilled(int index) {
            return !isCellEmpty(index);
        }
        private String cellContent(int index) {
            return cells.get(index).getText();
        }
        @SuppressWarnings("unused")
        private int anyAmong(int a, int b) {
            if((int)Math.floor(Math.random() * 2) == 0)
                return a;
            else
                return b;
        }
        private int getEmptyInLine(String cellString) {
            // prevention move check (horizontal)
            for (int r = 0; r < 3; r++) {
                if (cells.get(r * 3).getText().equals(cellString) &&
                        cells.get(r * 3 + 1).getText().equals(cellString) &&
                        cells.get(r * 3 + 2).getText().equals(""))
                    return (r * 3 + 2);
                if (cells.get(r * 3).getText().equals(cellString) &&
                        cells.get(r * 3 + 1).getText().equals("") &&
                        cells.get(r * 3 + 2).getText().equals(cellString))
                    return (r * 3 + 1);
                if (cells.get(r * 3).getText().equals("") &&
                        cells.get(r * 3 + 1).getText().equals(cellString) &&
                        cells.get(r * 3 + 2).getText().equals(cellString))
                    return (r * 3);
            }

            // prevention move check (vertical)
            for (int c = 0; c < 3; c++) {
                if (cells.get(c).getText().equals(cellString) &&
                        cells.get(3 + c).getText().equals(cellString) &&
                        cells.get(6 + c).getText().equals(""))
                    return (6 + c);
                if (cells.get(c).getText().equals(cellString) &&
                        cells.get(3 + c).getText().equals("") &&
                        cells.get(6 + c).getText().equals(cellString))
                    return (3 + c);
                if (cells.get(c).getText().equals("") &&
                        cells.get(3 + c).getText().equals(cellString) &&
                        cells.get(6 + c).getText().equals(cellString))
                    return (c);
            }

            // prevention move (left-to-right down diagonal)
            if (cells.get(0).getText().equals(cellString) &&
                    cells.get(4).getText().equals(cellString) &&
                    cells.get(8).getText().equals(""))
                return 8;
            if (cells.get(0).getText().equals(cellString) &&
                    cells.get(4).getText().equals("") &&
                    cells.get(8).getText().equals(cellString))
                return 4;
            if (cells.get(0).getText().equals("") &&
                    cells.get(4).getText().equals(cellString) &&
                    cells.get(8).getText().equals(cellString))
                return 0;

            // prevention move (right-to-left down diagonal)
            if (cells.get(2).getText().equals(cellString) &&
                    cells.get(4).getText().equals(cellString) &&
                    cells.get(6).getText().equals(""))
                return 6;
            if (cells.get(2).getText().equals(cellString) &&
                    cells.get(4).getText().equals("") &&
                    cells.get(6).getText().equals(cellString))
                return 4;
            if (cells.get(2).getText().equals("") &&
                    cells.get(4).getText().equals(cellString) &&
                    cells.get(6).getText().equals(cellString))
                return 2;
            return -1;
        }
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
