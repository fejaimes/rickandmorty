package com.example.rickandmorty.data.remote.dto

import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class CharacterDtoTest {

    @Test
    fun `toDomain maps CharacterDto with Alive status correctly`() {
        // given
        val dto = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            image = "https://rickandmortyapi.com/avatar/1.jpeg"
        )

        // when
        val entity = dto.toDomain()

        // then
        assertEquals(1, entity.id)
        assertEquals("Rick Sanchez", entity.name)
        assertEquals(CharacterStatusEntity.ALIVE, entity.status)
        assertEquals("Human", entity.species)
        assertEquals("https://rickandmortyapi.com/avatar/1.jpeg", entity.imageUrl)
    }

    @Test
    fun `toDomain maps CharacterDto with Dead status correctly`() {
        // given
        val dto = CharacterDto(
            id = 2,
            name = "Adjudicator Rick",
            status = "dead",
            species = "Human",
            image = "https://rickandmortyapi.com/avatar/8.jpeg"
        )

        // when
        val entity = dto.toDomain()

        // then
        assertEquals(CharacterStatusEntity.DEAD, entity.status)
    }

    @Test
    fun `toDomain maps CharacterDto with unknown status to UNKNOWN`() {
        // given
        val dto = CharacterDto(
            id = 3,
            name = "Alien Morty",
            status = "unknown",
            species = "Alien",
            image = "https://rickandmortyapi.com/avatar/14.jpeg"
        )

        // when
        val entity = dto.toDomain()

        // then
        assertEquals(CharacterStatusEntity.UNKNOWN, entity.status)
    }

    @Test
    fun `toDomain maps CharacterDto with unexpected status string to UNKNOWN`() {
        // given
        val dto = CharacterDto(
            id = 4,
            name = "Quantum Rick",
            status = "Mutated",
            species = "Human",
            image = "https://rickandmortyapi.com/avatar/22.jpeg"
        )

        // when
        val entity = dto.toDomain()

        // then
        assertEquals(CharacterStatusEntity.UNKNOWN, entity.status)
    }
}
