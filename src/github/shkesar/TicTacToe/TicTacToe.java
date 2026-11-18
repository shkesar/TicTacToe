package github.shkesar.TicTacToe;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class TicTacToe extends Application {
    private static final char HUMAN = 'X';
    private static final char COMPUTER = 'O';

    private final Board board = new Board();
    private final StackPane[] tiles = new StackPane[9];
    private final Label[] marks = new Label[9];
    private final List<Animation> winAnimations = new ArrayList<>();

    private char current = 'X';
    private boolean vsAI = true;
    private AI ai = AI.forDifficulty(AI.Difficulty.MEDIUM);
    private boolean locked = false;
    private boolean gameOver = false;
    private PauseTransition pendingAI;

    private int scoreX, scoreO, draws;

    private GridPane grid;
    private Label statusLabel;
    private VBox cardX, cardO, cardDraw;
    private Label nameX, nameO, valueX, valueO, valueDraw;
    private HBox difficultyBar;

    @Override
    public void start(Stage stage) {
        Label title = new Label("TIC · TAC · TOE");
        title.getStyleClass().add("title");

        // --- mode toggle ---
        ToggleGroup modeGroup = new ToggleGroup();
        ToggleButton vsAiBtn = chip("vs AI", modeGroup);
        ToggleButton twoPlayerBtn = chip("2 Players", modeGroup);
        vsAiBtn.setSelected(true);
        keepOneSelected(modeGroup);
        modeGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            vsAI = sel == vsAiBtn;
            difficultyBar.setVisible(vsAI);
            difficultyBar.setManaged(vsAI);
            updateScoreNames();
            resetScore();
        });
        HBox modeBar = new HBox(8, vsAiBtn, twoPlayerBtn);
        modeBar.setAlignment(Pos.CENTER);

        // --- difficulty toggle ---
        ToggleGroup diffGroup = new ToggleGroup();
        ToggleButton easy = chip("Easy", diffGroup);
        ToggleButton medium = chip("Medium", diffGroup);
        ToggleButton hard = chip("Hard", diffGroup);
        easy.setUserData(AI.Difficulty.EASY);
        medium.setUserData(AI.Difficulty.MEDIUM);
        hard.setUserData(AI.Difficulty.HARD);
        medium.setSelected(true);
        keepOneSelected(diffGroup);
        diffGroup.selectedToggleProperty().addListener((obs, old, sel) -> {
            if (sel == null) return;
            ai = AI.forDifficulty((AI.Difficulty) sel.getUserData());
            newRound();
        });
        difficultyBar = new HBox(8, easy, medium, hard);
        difficultyBar.setAlignment(Pos.CENTER);
        difficultyBar.getStyleClass().add("difficulty-bar");

        // --- scoreboard ---
        nameX = new Label();
        nameO = new Label();
        valueX = new Label("0");
        valueO = new Label("0");
        valueDraw = new Label("0");
        cardX = scoreCard(nameX, valueX, "card-x");
        cardDraw = scoreCard(new Label("Draws"), valueDraw, "card-draw");
        cardO = scoreCard(nameO, valueO, "card-o");
        HBox scoreBar = new HBox(12, cardX, cardDraw, cardO);
        scoreBar.setAlignment(Pos.CENTER);
        updateScoreNames();

        // --- board ---
        grid = new GridPane();
        grid.getStyleClass().add("board");
        grid.setHgap(14);
        grid.setVgap(14);
        grid.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints(80, 110, 160);
            cc.setHgrow(Priority.ALWAYS);
            RowConstraints rc = new RowConstraints(80, 110, 160);
            rc.setVgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(cc);
            grid.getRowConstraints().add(rc);
        }
        for (int i = 0; i < 9; i++) {
            grid.add(createTile(i), i % 3, i / 3);
        }
        VBox.setVgrow(grid, Priority.ALWAYS);

        statusLabel = new Label();
        statusLabel.getStyleClass().add("status");

        // --- actions ---
        Button newRoundBtn = new Button("New Round");
        newRoundBtn.getStyleClass().addAll("action", "primary");
        newRoundBtn.setOnAction(e -> newRound());
        Button resetBtn = new Button("Reset Score");
        resetBtn.getStyleClass().add("action");
        resetBtn.setOnAction(e -> resetScore());
        HBox actions = new HBox(12, newRoundBtn, resetBtn);
        actions.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, title, modeBar, difficultyBar, scoreBar, grid, statusLabel, actions);
        root.getStyleClass().add("root-pane");
        root.setAlignment(Pos.TOP_CENTER);

        Scene scene = new Scene(root, 460, 700);
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Tic-Tac-Toe");
        stage.setMinWidth(380);
        stage.setMinHeight(620);
        stage.show();

        newRound();
    }

    // ------------------------------------------------------------------ UI builders

    private ToggleButton chip(String text, ToggleGroup group) {
        ToggleButton b = new ToggleButton(text);
        b.setToggleGroup(group);
        b.getStyleClass().add("chip");
        return b;
    }

    /** Prevents a toggle group from ending up with nothing selected. */
    private static void keepOneSelected(ToggleGroup group) {
        group.selectedToggleProperty().addListener((obs, old, sel) -> {
            if (sel == null && old != null) old.setSelected(true);
        });
    }

    private VBox scoreCard(Label name, Label value, String variant) {
        name.getStyleClass().add("score-name");
        value.getStyleClass().add("score-value");
        VBox card = new VBox(2, name, value);
        card.setAlignment(Pos.CENTER);
        card.getStyleClass().addAll("score-card", variant);
        return card;
    }

    private StackPane createTile(int index) {
        Label mark = new Label();
        mark.getStyleClass().add("mark");
        mark.setMouseTransparent(true);

        StackPane tile = new StackPane(mark);
        tile.getStyleClass().add("tile");
        tile.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        tile.setCursor(Cursor.HAND);

        tile.setOnMouseEntered(e -> {
            if (canHumanPlay() && board.isEmpty(index)) {
                mark.setText(String.valueOf(current));
                setMarkClass(mark, current, true);
            }
        });
        tile.setOnMouseExited(e -> {
            if (board.isEmpty(index)) mark.setText("");
        });
        tile.setOnMouseClicked(e -> {
            if (canHumanPlay() && board.isEmpty(index)) {
                play(index);
            }
        });

        tiles[index] = tile;
        marks[index] = mark;
        return tile;
    }

    private static void setMarkClass(Label mark, char player, boolean preview) {
        mark.getStyleClass().removeAll("mark-x", "mark-o", "preview");
        mark.getStyleClass().add(player == 'X' ? "mark-x" : "mark-o");
        if (preview) mark.getStyleClass().add("preview");
    }

    private void updateScoreNames() {
        nameX.setText(vsAI ? "You (X)" : "Player X");
        nameO.setText(vsAI ? "AI (O)" : "Player O");
    }

    // ------------------------------------------------------------------ game flow

    private boolean canHumanPlay() {
        return !locked && !gameOver && (!vsAI || current == HUMAN);
    }

    private void play(int index) {
        board.place(index, current);
        Label mark = marks[index];
        mark.setText(String.valueOf(current));
        setMarkClass(mark, current, false);
        popIn(mark);

        int[] line = board.winningLine(current);
        if (line != null) {
            endWithWin(current, line);
            return;
        }
        if (board.isFull()) {
            endWithDraw();
            return;
        }

        current = Board.opponent(current);
        refreshTurnUI();

        if (vsAI && current == COMPUTER) {
            locked = true;
            statusLabel.setText("AI is thinking…");
            pendingAI = new PauseTransition(Duration.millis(450));
            pendingAI.setOnFinished(e -> {
                pendingAI = null;
                locked = false;
                play(ai.nextMove(board, COMPUTER));
            });
            pendingAI.play();
        }
    }

    private void refreshTurnUI() {
        cardX.getStyleClass().remove("active");
        cardO.getStyleClass().remove("active");
        if (gameOver) return;
        (current == 'X' ? cardX : cardO).getStyleClass().add("active");
        if (vsAI) {
            statusLabel.setText(current == HUMAN ? "Your turn" : "AI is thinking…");
        } else {
            statusLabel.setText("Player " + current + "'s turn");
        }
    }

    private void endWithWin(char winner, int[] line) {
        gameOver = true;
        if (winner == 'X') {
            scoreX++;
            bump(valueX, scoreX);
        } else {
            scoreO++;
            bump(valueO, scoreO);
        }

        if (vsAI) {
            statusLabel.setText(winner == HUMAN ? "You win!" : "AI wins — try again!");
        } else {
            statusLabel.setText("Player " + winner + " wins!");
        }
        refreshTurnUI();

        for (int i = 0; i < 9; i++) {
            boolean inLine = i == line[0] || i == line[1] || i == line[2];
            if (inLine) {
                tiles[i].getStyleClass().add("win");
                ScaleTransition pulse = new ScaleTransition(Duration.millis(600), tiles[i]);
                pulse.setFromX(1); pulse.setFromY(1);
                pulse.setToX(1.07); pulse.setToY(1.07);
                pulse.setAutoReverse(true);
                pulse.setCycleCount(Animation.INDEFINITE);
                pulse.play();
                winAnimations.add(pulse);
            } else {
                FadeTransition dim = new FadeTransition(Duration.millis(300), tiles[i]);
                dim.setToValue(0.35);
                dim.play();
                winAnimations.add(dim);
            }
        }
    }

    private void endWithDraw() {
        gameOver = true;
        draws++;
        bump(valueDraw, draws);
        statusLabel.setText("It's a draw!");
        refreshTurnUI();

        TranslateTransition shake = new TranslateTransition(Duration.millis(60), grid);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.setOnFinished(e -> grid.setTranslateX(0));
        shake.play();
    }

    private void newRound() {
        if (pendingAI != null) {
            pendingAI.stop();
            pendingAI = null;
        }
        winAnimations.forEach(Animation::stop);
        winAnimations.clear();

        board.reset();
        for (int i = 0; i < 9; i++) {
            marks[i].setText("");
            tiles[i].getStyleClass().remove("win");
            tiles[i].setScaleX(1);
            tiles[i].setScaleY(1);
            tiles[i].setOpacity(1);
        }
        grid.setTranslateX(0);

        current = 'X';
        locked = false;
        gameOver = false;
        refreshTurnUI();
    }

    private void resetScore() {
        scoreX = scoreO = draws = 0;
        valueX.setText("0");
        valueO.setText("0");
        valueDraw.setText("0");
        newRound();
    }

    // ------------------------------------------------------------------ animations

    private static void popIn(Node node) {
        node.setScaleX(0.2);
        node.setScaleY(0.2);
        node.setOpacity(0);

        ScaleTransition grow = new ScaleTransition(Duration.millis(160), node);
        grow.setToX(1.15);
        grow.setToY(1.15);
        grow.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition settle = new ScaleTransition(Duration.millis(110), node);
        settle.setToX(1);
        settle.setToY(1);
        FadeTransition fade = new FadeTransition(Duration.millis(160), node);
        fade.setToValue(1);

        new ParallelTransition(new SequentialTransition(grow, settle), fade).play();
    }

    private static void bump(Label label, int value) {
        label.setText(String.valueOf(value));
        ScaleTransition up = new ScaleTransition(Duration.millis(140), label);
        up.setFromX(1); up.setFromY(1);
        up.setToX(1.5); up.setToY(1.5);
        up.setAutoReverse(true);
        up.setCycleCount(2);
        up.play();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
