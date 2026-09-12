package com.example.rickandmorty.domain.entity

data class CharacterEntity(
    val id: Int,
    val name: String,
    val status: CharacterStatusEntity,
    val species: String,
    val imageUrl: String
)
