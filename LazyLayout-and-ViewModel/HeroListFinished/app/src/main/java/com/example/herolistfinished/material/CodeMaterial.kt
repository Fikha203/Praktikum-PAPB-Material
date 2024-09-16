package com.example.herolistfinished.material

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun Contact(names: List<String>) {
    LazyColumn {
        // menambahkan satu item
        item {
            Text(text = "Header 1", fontWeight = FontWeight.ExtraBold)
        }
        // Menambahkan banyak item berupa List maupun Array
        items(names) { name ->
            Text(name)
        }
        // Menambahkan banyak item berupa List maupun Array dengan index
        itemsIndexed(names) { index, item ->
            Text("Item at index $index is $item")
        }
    }
}

/*
class ViewModelFactory(private val repository: HeroRepository) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JetHeroesViewModel::class.java)) {
            return JetHeroesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}
*/