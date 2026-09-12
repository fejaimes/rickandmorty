package com.example.rickandmorty.domain.entity

data class PagedCharactersEntity(
    val characterEntities: List<CharacterEntity>,
    val hasNextPage: Boolean
)
