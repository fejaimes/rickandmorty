package com.example.rickandmorty.domain.usecase

import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import com.example.rickandmorty.domain.repository.CharacterRepository
import javax.inject.Inject

class GetCharactersUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    suspend operator fun invoke(page: Int): Result<PagedCharactersEntity> =
        repository.getCharacters(page)
}
