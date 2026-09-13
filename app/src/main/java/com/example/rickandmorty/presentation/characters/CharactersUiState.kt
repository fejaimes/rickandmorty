package com.example.rickandmorty.presentation.characters

import com.example.rickandmorty.domain.entity.CharacterEntity

sealed interface CharactersUiState {
    data object Loading : CharactersUiState
    data object Error : CharactersUiState
    data class Content(
        val characters: List<CharacterEntity>,
        val appendState: AppendState
    ) : CharactersUiState
}

enum class AppendState { IDLE, LOADING, ERROR, END_REACHED }
