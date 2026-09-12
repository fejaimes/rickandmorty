package com.example.rickandmorty.data.repository

import com.example.rickandmorty.data.remote.api.RickAndMortyApi
import com.example.rickandmorty.data.remote.dto.CharacterPageDto
import com.example.rickandmorty.data.remote.dto.toDomain
import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CharacterRepositoryImplTest {

    private val api = mockk<RickAndMortyApi>()

    private lateinit var repository: CharacterRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic("com.example.rickandmorty.data.remote.dto.CharacterPageDtoKt")
        repository = CharacterRepositoryImpl(api)
    }


    @After
    fun tearDown() {
        unmockkStatic("com.example.rickandmorty.data.remote.dto.CharacterPageDtoKt")
        confirmVerified(api)
    }

    @Test
    fun `getCharacters returns success when api returns success`() = runTest {
        // given
        val response = mockk<CharacterPageDto>()
        val pagedCharactersEntity = mockk<PagedCharactersEntity>()

        coEvery { api.getCharacters(any()) } returns response
        every { response.toDomain() } returns pagedCharactersEntity

        // when
        val result = repository.getCharacters(1)

        // then
        assertTrue(result.isSuccess)
        assertEquals(pagedCharactersEntity, result.getOrNull())

        verify(exactly = 1) {
            response.toDomain()
        }
        coVerify(exactly = 1) {
            api.getCharacters(any())
        }

    }

    @Test
    fun `getCharacters returns failure when api throws exception`() = runTest {
        // given
        val exception = RuntimeException("Network error")
        coEvery { api.getCharacters(any()) } throws exception

        // when
        val result = repository.getCharacters(1)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) {
            api.getCharacters(any())
        }
    }
}