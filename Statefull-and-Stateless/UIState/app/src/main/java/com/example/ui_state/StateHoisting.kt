package com.example.compose_state

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.ui_state.ui.theme.UI_StateTheme

@Composable
fun StatefulCounter(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        var count by rememberSaveable { mutableStateOf(0) }
        Text("Button clicked $count times:")
        Button(onClick = { count++ }) {
            Text("Click me!")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun CounterPreview() {
    UI_StateTheme {
        StatefulCounter()

    }
}


@Composable
fun StatelessCounter(
    count: Int,           //state
    onClick: () -> Unit,  //event
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(16.dp)
    ) {
        Text("Button clicked $count times:")
        Button(onClick = { onClick() }) {
            Text("Click me!")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun StatelessCounterPreview() {
    var count by rememberSaveable { mutableStateOf(0) }
    var count2 by rememberSaveable { mutableStateOf(0) }

    UI_StateTheme {
        Column {
            StatelessCounter(count = count, onClick = { count++ })
            StatelessCounter(count = count2, onClick = { count2+=2 })
        }
    }
}