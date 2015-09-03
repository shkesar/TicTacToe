package github.shkesar.TicTacToe;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TicTacToe extends Application {
    private static ArrayList<Button> cells = new ArrayList<>();

    private static ArrayList<Boolean> filledCells = new ArrayList<>(9);

    private FillAI aiLogic = new SmartAI();

    public TicTacToe() {
        // initialize filledCells list
        for (int i = 0; i < 9; i++) {
            filledCells.add(false);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        clearGridArray();
        BorderPane root = new BorderPane();
        GridPane grid = new GridPane();

        Button clearButton = new Button("Clear");

        ColumnConstraints column1 = new ColumnConstraints(50, 75, 100);
        RowConstraints row1 = new RowConstraints(50, 75, 100);

        grid.getColumnConstraints().addAll(column1, column1, column1);
        grid.getRowConstraints().addAll(row1, row1, row1);
        grid.setHgap(50);
        grid.setVgap(50);

        for (int index = 0; index < 9; index++) {
            grid.add(createCell(), index % 3, index / 3);
        }
        BorderPane.setMargin(grid, new Insets(25));
        root.setCenter(grid);

        BorderPane.setAlignment(clearButton, Pos.CENTER);
        BorderPane.setMargin(clearButton, new Insets(20));

        clearButton.setFont(new Font(20));
        clearButton.setOnAction(ae -> clearGrid());
        root.setBottom(clearButton);

        root.setMaxWidth(375);
        root.setMaxHeight(450);

        Scene scene = new Scene(root, 375, 450);
        primaryStage.setMaxWidth(375);
        primaryStage.setMinWidth(300);
        primaryStage.setMaxHeight(450);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tic-Tac-Toe");
        primaryStage.show();

        fillCell("O", aiLogic.nextMove());
    }

    private Button createCell() {
        Button cell = new Button("");
        cell.setMinHeight(50);
        cell.setPrefHeight(75);
        cell.setMaxHeight(100);
        cell.setMinWidth(50);
        cell.setPrefWidth(75);
        cell.setMaxWidth(100);
        cell.setFont(new Font(23));

        cell.setOnAction(ae -> {
            int index = cells.indexOf(cell);
            if (filledCells.get(index)) {
                return;
            } else {
                fillCell("X", index);
            }

            if (hasWon("X")) {
                System.out.println("You have won");
                disableGrid();
                return;
            }
            if (isGridFull()) {
                System.out.println("Game is draw");
                disableGrid();
                return;
            }

            // AI logic
            int aiIndex = aiLogic.nextMove();

            fillCell("O", aiIndex);

            if (hasWon("O")) {
                System.out.println("Computer has won");
                disableGrid();
                return;
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
        fillCell("O", aiLogic.nextMove());
    }

    public static void main(String[] args) {
        Application.launch(args);
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

    public interface FillAI {
        int nextMove();
    }


    class RandomAI implements FillAI {
        @Override
        public int nextMove() {
            int aiIndex = (int)Math.floor(Math.random() * 9);
            while (filledCells.get(aiIndex)) {
                aiIndex = (int)Math.floor(Math.random() * 9);
            }
            return aiIndex;
        }
    }

    class SmartAI implements FillAI {
        @Override
        public int nextMove() {
            int aiIndex;

            // winning move check
            aiIndex = getEmptyInLine("O");
            if (aiIndex != -1) {
                return aiIndex;
            }

            // preventive move check
            aiIndex = getEmptyInLine("X");
            if (aiIndex != -1) {
                return aiIndex;
            }

            // corner-fill move
            Random random = new Random();
            List<Integer> corners = new ArrayList() {{
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
            }

            aiIndex = (int)Math.floor(Math.random() * 9);
            while (filledCells.get(aiIndex)) {
                aiIndex = (int)Math.floor(Math.random() * 9);
            }
            return aiIndex;
        }

        private boolean isCellEmpty(int index) {
            return cells.get(index).getText().equals("");
        }

        private boolean isCellFilled(int index) {
            return !isCellEmpty(index);
        }

        private String cellContent(int index) {
            return cells.get(index).getText();
        }

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
}
