package org.example.front_end.common_elements

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MenuBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.Black)
    ) {
        Button(
            modifier = Modifier,
            onClick = {  }
        ){
            Text(text = "Fichier")
        }
    }
}