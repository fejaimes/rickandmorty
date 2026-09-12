package com.example.rickandmorty.domain.usecase

import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import com.example.rickandmorty.domain.repository.CharacterRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetCharactersUseCaseTest {

    private val repository = mockk<CharacterRepository>()
    private lateinit var useCase: GetCharactersUseCase

    @Before
    fun setUp() {
        useCase = GetCharactersUseCase(repository)
    }

    @After
    fun tearDown() {
        confirmVerified(repository)
    }

    @Test
    fun `invoke returns success when repository succeeds`() = runTest {
        // given
        val pagedCharactersEntity = mockk<PagedCharactersEntity>()
        coEvery { repository.getCharacters(1) } returns Result.success(pagedCharactersEntity)

        // when
        val result = useCase(1)

        // then
        assertTrue(result.isSuccess)
        assertEquals(pagedCharactersEntity, result.getOrNull())

        coVerify(exactly = 1) {
            repository.getCharacters(1)
        }
    }

    @Test
    fun `invoke returns failure when repository fails`() = runTest {
        // given
        val exception = RuntimeException("Network error")
        coEvery { repository.getCharacters(1) } returns Result.failure(exception)

        // when
        val result = useCase(1)

        // then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())

        coVerify(exactly = 1) {
            repository.getCharacters(1)
        }
    }
}
