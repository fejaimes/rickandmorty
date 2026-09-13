package com.example.rickandmorty.presentation.characters

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.entity.CharacterEntity
import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import com.example.rickandmorty.presentation.characters.components.CharacterItem
import com.example.rickandmorty.presentation.characters.components.ErrorBanner
import com.example.rickandmorty.presentation.theme.RickAndMortyTheme
import com.example.rickandmorty.presentation.theme.dimens
import kotlinx.coroutines.flow.filter

private const val LOAD_MORE_THRESHOLD = 5
private const val CHARACTER_CONTENT_TYPE = "character"
private const val APPEND_FOOTER_KEY = "append_footer"

@Composable
fun CharactersScreen(
    modifier: Modifier = Modifier,
    viewModel: CharactersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CharactersContent(
        uiState = uiState,
        onLoadMore = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CharactersContent(
    uiState: CharactersUiState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.characters_title)) })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                CharactersUiState.Loading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )

                CharactersUiState.Error -> ErrorBanner(
                    onRetry = onRetry,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(MaterialTheme.dimens.spacingS)
                )

                is CharactersUiState.Content -> CharactersList(
                    characters = uiState.characters,
                    appendState = uiState.appendState,
                    onLoadMore = onLoadMore,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun CharactersList(
    characters: List<CharacterEntity>,
    appendState: AppendState,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val currentOnLoadMore by rememberUpdatedState(onLoadMore)

    val isNearEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index
            lastVisibleIndex != null &&
                    lastVisibleIndex >= layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD
        }
    }

    LaunchedEffect(listState, appendState) {
        snapshotFlow { isNearEnd }
            .filter { it }
            .collect { currentOnLoadMore() }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MaterialTheme.dimens.spacingS,
            end = MaterialTheme.dimens.spacingS,
            bottom = MaterialTheme.dimens.spacingXXL
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spacingXXS)
    ) {
        items(
            items = characters,
            key = { it.id },
            contentType = { CHARACTER_CONTENT_TYPE }
        ) { character ->
            CharacterItem(character = character)
        }

        when (appendState) {
            AppendState.LOADING, AppendState.ERROR -> item(
                key = APPEND_FOOTER_KEY,
                contentType = APPEND_FOOTER_KEY
            ) {
                AppendFooter(
                    isError = appendState == AppendState.ERROR,
                    onRetry = onRetry,
                    modifier = Modifier.padding(top = MaterialTheme.dimens.spacingS)
                )
            }

            AppendState.IDLE, AppendState.END_REACHED -> Unit
        }
    }
}

@Composable
private fun AppendFooter(
    isError: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        ErrorBanner(
            onRetry = onRetry,
            modifier = if (isError) {
                Modifier
            } else {
                Modifier
                    .alpha(0f)
                    .clearAndSetSemantics {}
            }
        )
        if (!isError) {
            CircularProgressIndicator()
        }
    }
}

private val previewCharacters = listOf(
    CharacterEntity(1, "Rick Sanchez", CharacterStatusEntity.ALIVE, "Human", ""),
    CharacterEntity(2, "Morty Smith", CharacterStatusEntity.ALIVE, "Human", ""),
    CharacterEntity(3, "Birdperson", CharacterStatusEntity.DEAD, "Bird-Person", ""),
    CharacterEntity(4, "Mr. Poopybutthole", CharacterStatusEntity.UNKNOWN, "Poopybutthole", "")
)

@Preview
@Composable
private fun CharactersLoadingPreview() {
    RickAndMortyTheme {
        CharactersContent(uiState = CharactersUiState.Loading, onLoadMore = {}, onRetry = {})
    }
}

@Preview
@Composable
private fun CharactersErrorPreview() {
    RickAndMortyTheme {
        CharactersContent(uiState = CharactersUiState.Error, onLoadMore = {}, onRetry = {})
    }
}

@Preview
@Composable
private fun CharactersAppendLoadingPreview() {
    RickAndMortyTheme {
        CharactersContent(
            uiState = CharactersUiState.Content(previewCharacters, AppendState.LOADING),
            onLoadMore = {},
            onRetry = {}
        )
    }
}

@Preview
@Composable
private fun CharactersAppendErrorPreview() {
    RickAndMortyTheme {
        CharactersContent(
            uiState = CharactersUiState.Content(previewCharacters, AppendState.ERROR),
            onLoadMore = {},
            onRetry = {}
        )
    }
}
