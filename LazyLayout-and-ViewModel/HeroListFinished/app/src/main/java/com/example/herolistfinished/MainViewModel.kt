package com.example.herolistfinished

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.herolistfinished.model.Hero
import com.example.herolistfinished.repository.HeroRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(private val repository: HeroRepository): ViewModel() {

    private val _sortedHeroes = MutableStateFlow(
        repository.getHeroes()
            .sortedBy { it.name }
    )

    val sortedHeroes : StateFlow<List<Hero>> get() = _sortedHeroes

}

class ViewModelFactory(private val repository: HeroRepository) :
    ViewModelProvider.NewInstanceFactory() {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: " + modelClass.name)
    }
}