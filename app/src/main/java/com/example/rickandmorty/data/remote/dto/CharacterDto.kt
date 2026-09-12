package com.example.rickandmorty.data.remote.dto

import com.example.rickandmorty.domain.entity.CharacterEntity
import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CharacterDto(
    @SerialName("id")
    val id: Int,
    @SerialName("name")
    val name: String,
    @SerialName("status")
    val status: String,
    @SerialName("species")
    val species: String,
    @SerialName("image")
    val image: String
)

fun CharacterDto.toDomain(): CharacterEntity = CharacterEntity(
    id = id,
    name = name,
    status = status.toCharacterStatus(),
    species = species,
    imageUrl = image
)

private fun String.toCharacterStatus(): CharacterStatusEntity =
    when (lowercase()) {
        "alive" -> CharacterStatusEntity.ALIVE
        "dead" -> CharacterStatusEntity.DEAD
        else -> CharacterStatusEntity.UNKNOWN
    }
