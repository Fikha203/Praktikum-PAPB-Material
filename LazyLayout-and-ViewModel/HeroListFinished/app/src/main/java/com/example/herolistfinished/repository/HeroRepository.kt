package com.example.herolistfinished.repository

import com.example.herolistfinished.model.Hero
import com.example.herolistfinished.model.HeroesData

class HeroRepository {

    fun getHeroes(): List<Hero> {
        return HeroesData.heroes
    }

}