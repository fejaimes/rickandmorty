package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.api.RickAndMortyApi
import com.example.rickandmorty.data.remote.dto.toDomain
import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import com.example.rickandmorty.domain.repository.CharacterRepository
import javax.inject.Inject

class CharacterRepositoryImpl @Inject constructor(
    private val api: RickAndMortyApi
) : CharacterRepository {
    override suspend fun getCharacters(page: Int): Result<PagedCharactersEntity> =
        runCatching { api.getCharacters(page).toDomain() }
}
