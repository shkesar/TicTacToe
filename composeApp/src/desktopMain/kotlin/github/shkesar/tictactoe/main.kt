package github.shkesar.tictactoe

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Tic-Tac-Toe",
        state = rememberWindowState(width = 420.dp, height = 800.dp),
    ) {
        App()
    }
}
