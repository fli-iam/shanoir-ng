package org.example.front_end

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicSecureTextField
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecureTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.example.front_end.common_elements.MenuBar


@Composable
@Preview
fun LoginWindow(modifier: Modifier = Modifier, onLoginSuccess: () -> Unit = {}) {
    MaterialTheme {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                var id by remember { mutableStateOf("") }

                Text(
                    text = "Identifiant :"
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp, 0.dp, 0.dp, 0.dp),
                    value = id,
                    onValueChange = { id = it },
                )
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(0.dp, 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Mot de passe :"
                )

                val state = rememberTextFieldState()
                var showPassword by remember { mutableStateOf(false) }
                SecureTextField(
                    state = state,
                    textObfuscationMode =
                        if (showPassword) {
                            TextObfuscationMode.Visible
                        } else {
                            TextObfuscationMode.RevealLastTyped
                        },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                )
            }
            Button(
                modifier = modifier,
                onClick = {
                    onLoginSuccess()
                },
            ) {
                Text("Login")
            }
        }
    }
}
