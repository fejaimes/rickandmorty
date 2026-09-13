package com.example.rickandmorty.presentation.characters.components


import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.rickandmorty.R
import com.example.rickandmorty.domain.entity.CharacterEntity
import com.example.rickandmorty.domain.entity.CharacterStatusEntity
import com.example.rickandmorty.presentation.theme.RickAndMortyTheme
import com.example.rickandmorty.presentation.theme.dimens
import com.example.rickandmorty.presentation.theme.extendedColors

@Composable
fun CharacterItem(
    character: CharacterEntity,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(
                horizontal = MaterialTheme.dimens.spacingS,
                vertical = MaterialTheme.dimens.spacingM
            ),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spacingL),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CharacterAvatar(name = character.name, imageUrl = character.imageUrl)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spacingXS)
        ) {
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spacingS),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(MaterialTheme.dimens.statusDotSize)
                        .background(character.status.dotColor(), CircleShape)
                )
                Text(
                    text = stringResource(character.status.labelRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "·",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                Text(
                    text = character.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CharacterAvatar(
    name: String,
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    var isImageVisible by remember(imageUrl) { mutableStateOf(false) }

    val initial = remember(name) {
        name.trim().take(1).uppercase().ifEmpty { "?" }
    }

    Box(
        modifier = modifier
            .size(MaterialTheme.dimens.avatarSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .semantics(mergeDescendants = true) { contentDescription = name },
        contentAlignment = Alignment.Center,
    ) {
        if (!isImageVisible) {
            Text(
                text = initial,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }

        AsyncImage(
            model = imageUrl?.takeIf { it.isNotBlank() },
            contentDescription = null,
            contentScale = ContentScale.Crop,
            onState = { state -> isImageVisible = state is AsyncImagePainter.State.Success },
            modifier = Modifier.matchParentSize(),
        )
    }
}

@Composable
private fun CharacterStatusEntity.dotColor(): Color = when (this) {
    CharacterStatusEntity.ALIVE -> MaterialTheme.extendedColors.statusAlive
    CharacterStatusEntity.DEAD -> MaterialTheme.colorScheme.error
    CharacterStatusEntity.UNKNOWN -> MaterialTheme.colorScheme.outline
}

@StringRes
private fun CharacterStatusEntity.labelRes(): Int = when (this) {
    CharacterStatusEntity.ALIVE -> R.string.character_status_alive
    CharacterStatusEntity.DEAD -> R.string.character_status_dead
    CharacterStatusEntity.UNKNOWN -> R.string.character_status_unknown
}

@OptIn(ExperimentalCoilApi::class)
@Preview
@Composable
private fun CharacterItemPreview() {
    RickAndMortyTheme {
        CharacterItem(
            character = CharacterEntity(
                id = 1,
                name = "Rick Sanchez",
                status = CharacterStatusEntity.ALIVE,
                species = "Human",
                imageUrl = "https://rickandmortyapi.com/api/character/avatar/1.jpeg"
            )
        )
    }
}
