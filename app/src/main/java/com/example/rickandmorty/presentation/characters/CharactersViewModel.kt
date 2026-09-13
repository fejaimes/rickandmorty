package com.example.rickandmorty.presentation.characters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rickandmorty.domain.entity.PagedCharactersEntity
import com.example.rickandmorty.domain.usecase.GetCharactersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharactersViewModel @Inject constructor(
    private val getCharactersUseCase: GetCharactersUseCase
) : ViewModel() {

    companion object {
        private const val FIRST_PAGE = 1
    }

    private val _uiState = MutableStateFlow<CharactersUiState>(CharactersUiState.Loading)
    val uiState: StateFlow<CharactersUiState> = _uiState.asStateFlow()

    private var nextPage = FIRST_PAGE

    init {
        loadFirstPage()
    }

    fun loadNextPage() {
        val current = _uiState.value
        if (current is CharactersUiState.Content && current.appendState == AppendState.IDLE) {
            appendNextPage(current)
        }
    }

    fun retry() {
        when (val current = _uiState.value) {
            CharactersUiState.Error -> loadFirstPage()
            is CharactersUiState.Content ->
                if (current.appendState == AppendState.ERROR) appendNextPage(current)

            CharactersUiState.Loading -> Unit
        }
    }

    private fun loadFirstPage() {
        _uiState.value = CharactersUiState.Loading
        viewModelScope.launch {
            getCharactersUseCase(FIRST_PAGE).fold(
                onSuccess = { page ->
                    nextPage = FIRST_PAGE + 1
                    _uiState.value = CharactersUiState.Content(
                        characters = page.characterEntities,
                        appendState = page.toAppendState()
                    )
                },
                onFailure = {
                    _uiState.value = CharactersUiState.Error
                }
            )
        }
    }

    private fun appendNextPage(current: CharactersUiState.Content) {
        _uiState.value = current.copy(appendState = AppendState.LOADING)
        viewModelScope.launch {
            getCharactersUseCase(nextPage).fold(
                onSuccess = { page ->
                    nextPage++
                    _uiState.value = CharactersUiState.Content(
                        characters = current.characters + page.characterEntities,
                        appendState = page.toAppendState()
                    )
                },
                onFailure = {
                    _uiState.value = current.copy(appendState = AppendState.ERROR)
                }
            )
        }
    }

    private fun PagedCharactersEntity.toAppendState(): AppendState =
        if (hasNextPage) AppendState.IDLE else AppendState.END_REACHED
}
