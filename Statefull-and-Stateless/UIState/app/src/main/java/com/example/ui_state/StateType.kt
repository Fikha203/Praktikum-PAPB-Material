package com.example.compose_state

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun FormInput(modifier: Modifier = Modifier) {
    var name by remember { mutableStateOf("") } //state
    OutlinedTextField(
        value = name, //display state
        onValueChange = { newName -> //event
            name = newName //update state
        },
        label = { Text("Nama") },
        modifier = Modifier.padding(8.dp)
    )
}

@Preview(showBackground = true, device = Devices.PIXEL_3A)
@Composable
private fun FormInputPreview() {
    FormInputSaveable()

}

@Composable
fun FormInputSaveable(modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") } //state
    OutlinedTextField(
        value = name, //display state
        onValueChange = { newName -> //event
            name = newName //update state
        },
        label = { Text("Nama") },
        modifier = Modifier.padding(8.dp)
    )
}



