package org.example.front_end

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Shanoir Uploader",
    ) {
        var cureentScreen by remember { mutableStateOf(Screens.LOGIN)}

        when (cureentScreen) {
            Screens.LOGIN -> LoginWindow(
                onLoginSuccess = {
                    cureentScreen = Screens.MAIN
                }
            )

            Screens.MAIN -> MainWindow()
        }
    }
}

/**
 * Contains all the differents screens of Shanoir Uploader (do not include pop-up)
 */
enum class Screens {
    LOGIN,
    MAIN,
}
