package com.example.rickandmorty.domain.repository

import com.example.rickandmorty.domain.entity.PagedCharactersEntity

fun interface CharacterRepository {
    suspend fun getCharacters(page: Int): Result<PagedCharactersEntity>
}
