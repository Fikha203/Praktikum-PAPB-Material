package com.example.herolistfinished

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.herolistfinished.repository.HeroRepository
import com.example.herolistfinished.ui.theme.HeroListFinishedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HeroListFinishedTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JetHeroesApp()
                }

            }
        }
    }
}

@Composable
fun JetHeroesApp(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = viewModel(factory = ViewModelFactory(HeroRepository()))
) {

    val sortedHeroes by viewModel.sortedHeroes.collectAsState()

    Box(modifier = modifier) {

        LazyColumn {
            items(sortedHeroes, key = { it.id }) { hero ->
                HeroListItem(
                    name = hero.name,
                    photoUrl = hero.photoUrl,
                    modifier = Modifier.fillMaxWidth()
                )

            }
        }

    }
}

@Preview(showBackground = true, showSystemUi = true, device = Devices.PIXEL_3A)
@Composable
fun JetHeroesAppPreview() {
    HeroListFinishedTheme {
        JetHeroesApp()
    }
}

@Composable
fun HeroListItem(
    name: String,
    photoUrl: String,
    modifier: Modifier = Modifier
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { }
    ) {

        AsyncImage(
            model = photoUrl,
            contentDescription = "Image of $name",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .padding(8.dp)
                .size(60.dp)
                .clip(CircleShape)
        )

        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HeroListItemPreview() {
    HeroListFinishedTheme {
        HeroListItem(
            name = "Hero",
            photoUrl = "https://raw.githubusercontent.com/dicodingacademy/assets/main/android_compose_academy/pahlawan/9.jpg"
        )
    }
}













