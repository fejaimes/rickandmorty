package com.example.rickandmorty.data.remote.dto

import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import org.junit.Assert.*
import org.junit.Test

class CharacterPageDtoTest {

    @Test
    fun `toDomain maps CharacterPageDto to PagedCharactersEntity correctly`() {
        // given
        val infoDto = InfoDto(
            count = 2,
            pages = 1,
            next = "https://rickandmortyapi.com/api/character?page=2",
            prev = null
        )
        val characterDto1 = CharacterDto(
            id = 1,
            name = "Rick Sanchez",
            status = "Alive",
            species = "Human",
            image = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
        )
        val characterDto2 = CharacterDto(
            id = 2,
            name = "Morty Smith",
            status = "Dead",
            species = "Human",
            image = "https://rickandmortyapi.com/api/character/avatar/2.jpeg"
        )
        val characterPageDto = CharacterPageDto(
            info = infoDto,
            results = listOf(characterDto1, characterDto2)
        )

        // when
        val domainEntity = characterPageDto.toDomain()

        // then
        assertTrue(domainEntity.hasNextPage)
        assertEquals(2, domainEntity.characterEntities.size)

        val firstCharacter = domainEntity.characterEntities[0]
        assertEquals(1, firstCharacter.id)
        assertEquals("Rick Sanchez", firstCharacter.name)
        assertEquals(CharacterStatusEntity.ALIVE, firstCharacter.status)
        assertEquals("Human", firstCharacter.species)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/1.jpeg", firstCharacter.imageUrl)

        val secondCharacter = domainEntity.characterEntities[1]
        assertEquals(2, secondCharacter.id)
        assertEquals("Morty Smith", secondCharacter.name)
        assertEquals(CharacterStatusEntity.DEAD, secondCharacter.status)
        assertEquals("Human", secondCharacter.species)
        assertEquals("https://rickandmortyapi.com/api/character/avatar/2.jpeg", secondCharacter.imageUrl)
    }

    @Test
    fun `toDomain sets hasNextPage to false when next is null`() {
        // given
        val infoDto = InfoDto(
            count = 1,
            pages = 1,
            next = null,
            prev = null
        )
        val characterPageDto = CharacterPageDto(
            info = infoDto,
            results = emptyList()
        )

        // when
        val domainEntity = characterPageDto.toDomain()

        // then
        assertFalse(domainEntity.hasNextPage)
        assertTrue(domainEntity.characterEntities.isEmpty())
    }
}
