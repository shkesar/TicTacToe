# TicTacToe Codebase

A fully-featured Tic-Tac-Toe game built with **Compose Multiplatform**, featuring a dark neon aesthetic, three AI difficulty levels, animations, and haptic feedback. Runs on Android, Desktop (JVM), and iOS.

## Project Structure

```
composeApp/
├── src/
│   ├── commonMain/kotlin/github/shkesar/tictactoe/    # Shared logic
│   │   ├── App.kt                                      # Main Compose UI
│   │   ├── GameState.kt                                # Game state & logic
│   │   ├── GameBoard.kt                                # Board UI & animations
│   │   ├── Board.kt                                    # Game board model
│   │   ├── AI.kt                                       # AI opponents (3 levels)
│   │   ├── Components.kt                               # Reusable UI components
│   │   └── Theme.kt                                    # Neon color palette
│   ├── androidMain/kotlin/...                          # Android entry point
│   ├── desktopMain/kotlin/...                          # JVM/Desktop entry point
│   ├── iosMain/kotlin/...                              # iOS entry point
│   └── commonTest/kotlin/...                           # Shared tests
├── build.gradle.kts
├── README.md
├── build.gradle.kts                                    # Root build config
├── settings.gradle.kts                                 # Multiplatform setup
└── gradle/wrapper/                                     # Gradle distribution

Requires: JDK 21, Kotlin 2.0+, Compose Multiplatform
```

## Core Modules

### Game State & Logic (`GameState.kt`)

**Key class:** `GameState`

Manages all mutable game state using Compose's `mutableStateOf`. Observable by the UI and automatically saved/restored via `Saver`.

**Properties:**
- `mode: Mode` - VS_AI or TWO_PLAYER
- `difficulty: Difficulty` - EASY, MEDIUM, or HARD (AI only)
- `board: Board` - Current 3x3 board state
- `current: Player` - Whose turn it is (X or O)
- `scoreX, scoreO, draws` - Session win counts
- `round` - Increments on each new round (triggers LaunchedEffect restarts)

**Methods:**
- `play(index)` - Place a mark and handle win/draw logic
- `playAiMove()` - Trigger the AI's next move
- `newRound()` - Reset board and increment round counter
- `resetScores()` - Clear all scores and start fresh
- `selectMode(m)` - Switch between vs AI / 2-player (resets scores)
- `selectDifficulty(d)` - Change AI difficulty (restarts round)
- `canPlay(index)` - True if the cell is empty and it's not the AI's turn

**Computed properties:**
- `isAiTurn: Boolean` - True when AI should move
- `isOver: Boolean` - True if game won or board full
- `isDraw: Boolean` - True if board full with no winner
- `winner: Player?` - X, O, or null
- `winningLine: List<Int>?` - Indices [0-8] of three winning cells, or null

**Save/Restore:**
Uses `Saver` to encode/decode all state to/from a List. Persists across app restarts.

### Board Model (`Board.kt`)

**Key class:** `Board`

Immutable data class representing the 3x3 board.

**Data:**
- `cells: List<Player?>` - 9 cells indexed 0-8, row-major (top-left is 0, bottom-right is 8)

**Methods:**
- `place(index, player)` - Returns a new Board with the mark placed
- `winningLine(player)` - Returns List<Int> if player has three in a row, else null
- `winner()` - Returns Player or null
- `isFull` - True if all 9 cells occupied
- `emptyCells` - List<Int> of unoccupied indices
- `encode() / decode()` - Serialize to/from String (X/O/-)

**Constants:**
- `LINES` - All 8 winning combinations (3 rows, 3 cols, 2 diagonals)

### AI Opponents (`AI.kt`)

Three AI strategies as singletons implementing the `AI` function interface.

#### EasyAI
Picks a uniformly random empty cell. No strategy.

#### MediumAI
1. Wins if possible (finds a cell completing a line of 2)
2. Blocks opponent's winning move
3. Takes center cell (index 4) if available
4. Random pick otherwise

#### HardAI
Implements **minimax** algorithm with alpha-beta pruning-like pruning. Scores moves as:
- `+10 - depth` if move leads to AI win
- `-(10 - depth)` if opponent wins
- `0` for draw

Picks randomly among equally-scored moves for variety. **Never loses.**

### UI: Main App (`App.kt`)

**Function:** `App()`

Entry point for Compose. Sets up:
- **Title:** Glowing "TIC · TAC · TOE" with cyan-magenta gradient
- **Mode toggle:** vs AI / 2 Players (via `SegmentedControl`)
- **Difficulty toggle:** Easy / Medium / Hard (shown only in vs AI mode)
- **Score row:** Three cards showing X score, draws, O score (with accent colors)
- **Game board:** 3x3 grid wrapped in `BoxWithConstraints` to scale responsively
- **Status text:** Animated message (Your turn / AI is thinking / You win / etc.)
- **Buttons:** New Round (primary style) and Reset Score

**LaunchedEffects:**
1. When `isAiTurn` becomes true: delay 450ms, then call `playAiMove()`
2. When game ends: haptic feedback (long press vibration)

Compose material3 dark color scheme with custom `Neon` palette overlaid.

### UI: Game Board (`GameBoard.kt`)

**Function:** `GameBoard(state, onTap, modifier)`

Renders a 3x3 grid of `Tile` composables. Handles:

- **Tiles:** Each cell is a rounded box with 2dp border, shadow, and click handler
  - Scales down on press (0.92x)
  - Shows neon glow when winning (16dp elevation with gold shadow)
  - Fades to 0.35 alpha when dimmed (non-winning cells after a win)
  - Pulses 1.0–1.06x when part of winning line

- **Marks (X/O):** Canvas-drawn with animated stroke + multi-layer glow effect
  - X draws two diagonal lines (animated simultaneously)
  - O draws arc (circumference grows from 0 to 360°)
  - Glow achieved via three overlapping strokes with decreasing opacity

- **Win line:** Gold line drawn across the three winning cells (after 220ms delay, over 420ms)
  - Extends slightly beyond cell centers for visual polish
  - Also uses `glowLine()` for multi-layer glow

- **Draw animation:** Whole board shakes on draw (6 oscillations in 420ms)

### UI: Components (`Components.kt`)

Reusable composables:
- **SegmentedControl** - Chip-style horizontal toggle bar (compact variant available)
- **ScoreCard** - Rounded box showing label + score value with accent color stripe
- **PillButton** - Rounded rectangle button with optional `primary` style (uses magenta-purple gradient)
- **Title** - Already described in App.kt

### Theme (`Theme.kt`)

**Object:** `Neon`

Dark neon color palette:
- **Background:** Vertical gradient (dark blue top → darker blue mid → navy bottom)
- **UI:** Tiles, cards, buttons in dark navy shades
- **Accents:**
  - Cyan → Player X, You label
  - Magenta → Player O, AI label
  - Gold → Winning line
  - Blue/Purple → Button/selection gradients
- **Utility colors:** Text (light lavender), Muted (gray), Hairline (subtle borders)
- **Glow:** `glowLayers` list of (stroke multiplier, alpha) for multi-layer neon effect

**Extension:** `Player.color` returns Cyan for X, Magenta for O

## Game Flow

1. **Init:** `GameState()` created with defaults (VS_AI, MEDIUM difficulty, empty board)
2. **UI renders:** Buttons, board, status text based on state
3. **Human play:** Click tile → `onTap(index)` → `state.play(index)`
   - `play()` updates board, checks for win/draw, switches player
4. **AI turn:** `isAiTurn` becomes true
   - `LaunchedEffect` in `App` waits 450ms (UI feedback: "AI is thinking…")
   - Calls `playAiMove()` → `AI.forDifficulty()` → chooses move → `play()`
5. **Win/draw:** Game ends, scores update, marks dim, animation triggers
6. **New round:** Reset board, keep scores, increment round counter
7. **Reset:** Clear all scores, restart game

## Running the Project

### Development

```bash
./gradlew build              # Compile all platforms
./gradlew desktopRun         # Run on JVM
./gradlew :composeApp:check  # Run tests
```

### Platforms

- **Android:** Compile with `./gradlew assembleDebug` or use Android Studio
- **Desktop:** Direct JVM execution (shown above)
- **iOS:** Build with Xcode (Compose Multiplatform integration)

## Key Dependencies

```kotlin
// In composeApp/build.gradle.kts (via version catalog)
androidx.compose.foundation
androidx.compose.material3
org.jetbrains.compose
org.jetbrains.kotlin.multiplatform
```

JDK 21+ required (uses modern kotlin stdlib).

## Styling & Animation Techniques

- **Gradients:** Brush (vertical for background, horizontal for accents) + multi-layer with transparency
- **Scaling:** `graphicsLayer { scaleX/Y }` for smooth hardware-accelerated transforms
- **Glow:** Overlaid strokes with 10% → 22% → 100% alpha on increasingly thin lines
- **Keyframe animation:** Board shake uses `keyframes` DSL for precise timing
- **Infinite loops:** Winning tile pulses via `infiniteRepeatable` with `RepeatMode.Reverse`
- **Canvas drawing:** X/O/lines rendered via Canvas composable for custom glow blending

## Testing

Basic test file exists (`AITest.kt`) for validating AI logic. Can be extended to test `GameState` persistence and board win-detection logic.

## Future Enhancement Ideas

- Undo/redo stack
- Online multiplayer (via WebSocket)
- Game replay / move history
- Statistics dashboard
- Custom board sizes (4x4, 5x5)
- Sound effects + music
- Alternative themes
