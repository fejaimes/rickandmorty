package com.example.rickandmorty.data.remote.dto

import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterPageDto(
    @SerialName("info")
    val info: InfoDto,
    @SerialName("results")
    val results: List<CharacterDto>
)

fun CharacterPageDto.toDomain(): PagedCharactersEntity = PagedCharactersEntity(
    characterEntities = results.map { it.toDomain() },
    hasNextPage = info.next != null
)
