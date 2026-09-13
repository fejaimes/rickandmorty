package com.example.rickandmorty.presentation.characters

import com.example.rickandmorty.domain.entity.CharacterEntity
import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import com.example.rickandmorty.domain.usecase.GetCharactersUseCase
import com.example.rickandmorty.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CharactersViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getCharactersUseCase = mockk<GetCharactersUseCase>()

    @After
    fun tearDown() {
        confirmVerified(getCharactersUseCase)
    }

    @Test
    fun `init emits Content with IDLE when first page has next page`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))

        // when
        val viewModel = CharactersViewModel(getCharactersUseCase)
        val initialState = viewModel.uiState.value
        advanceUntilIdle()

        // then
        assertEquals(CharactersUiState.Loading, initialState)
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.IDLE),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
    }

    @Test
    fun `init emits Content with END_REACHED when first page is the last one`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = false))

        // when
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.END_REACHED),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
    }

    @Test
    fun `init emits Error when first page fails`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.failure(RuntimeException("Network error"))

        // when
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // then
        assertEquals(CharactersUiState.Error, viewModel.uiState.value)
        coVerify(exactly = 1) { getCharactersUseCase(1) }
    }

    @Test
    fun `retry from Error requests first page again`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns
                Result.failure(RuntimeException("Network error")) andThen
                Result.success(page(1, 2, hasNextPage = true))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.retry()
        val stateAfterRetry = viewModel.uiState.value
        advanceUntilIdle()

        // then
        assertEquals(CharactersUiState.Loading, stateAfterRetry)
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.IDLE),
            viewModel.uiState.value
        )
        coVerify(exactly = 2) { getCharactersUseCase(1) }
    }

    @Test
    fun `loadNextPage appends characters of next page`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))
        coEvery { getCharactersUseCase(2) } returns Result.success(page(3, 4, hasNextPage = false))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.loadNextPage()
        val stateWhileLoading = viewModel.uiState.value
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.LOADING),
            stateWhileLoading
        )
        assertEquals(
            CharactersUiState.Content(characters(1, 2, 3, 4), AppendState.END_REACHED),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
        coVerify(exactly = 1) { getCharactersUseCase(2) }
    }

    @Test
    fun `loadNextPage failure keeps characters and pauses pagination`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))
        coEvery { getCharactersUseCase(2) } returns Result.failure(RuntimeException("Network error"))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.loadNextPage()
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.ERROR),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
        coVerify(exactly = 1) { getCharactersUseCase(2) }
    }

    @Test
    fun `retry from append ERROR requests the same page again`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))
        coEvery { getCharactersUseCase(2) } returns
                Result.failure(RuntimeException("Network error")) andThen
                Result.success(page(3, 4, hasNextPage = true))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // when
        viewModel.retry()
        val stateAfterRetry = viewModel.uiState.value
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.LOADING),
            stateAfterRetry
        )
        assertEquals(
            CharactersUiState.Content(characters(1, 2, 3, 4), AppendState.IDLE),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
        coVerify(exactly = 2) { getCharactersUseCase(2) }
    }

    @Test
    fun `loadNextPage does nothing when END_REACHED`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = false))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.END_REACHED),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
    }

    @Test
    fun `loadNextPage does nothing while next page is LOADING`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))
        coEvery { getCharactersUseCase(2) } returns Result.success(page(3, 4, hasNextPage = true))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.loadNextPage()
        viewModel.loadNextPage()
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2, 3, 4), AppendState.IDLE),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
        coVerify(exactly = 1) { getCharactersUseCase(2) }
    }

    @Test
    fun `retry does nothing when state is Content with IDLE`() = runTest {
        // given
        coEvery { getCharactersUseCase(1) } returns Result.success(page(1, 2, hasNextPage = true))
        val viewModel = CharactersViewModel(getCharactersUseCase)
        advanceUntilIdle()

        // when
        viewModel.retry()
        advanceUntilIdle()

        // then
        assertEquals(
            CharactersUiState.Content(characters(1, 2), AppendState.IDLE),
            viewModel.uiState.value
        )
        coVerify(exactly = 1) { getCharactersUseCase(1) }
    }

    private fun character(id: Int) = CharacterEntity(
        id = id,
        name = "Character $id",
        status = CharacterStatusEntity.ALIVE,
        species = "Human",
        imageUrl = "https://rickandmortyapi.com/api/character/avatar/$id.jpeg"
    )

    private fun characters(vararg ids: Int) = ids.map(::character)

    private fun page(vararg ids: Int, hasNextPage: Boolean) = PagedCharactersEntity(
        characterEntities = characters(*ids),
        hasNextPage = hasNextPage
    )
}
