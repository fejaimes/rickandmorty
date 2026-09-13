package com.example.rickandmorty.presentation.characters.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.rickandmorty.R
import com.example.rickandmorty.presentation.theme.RickAndMortyTheme
import com.example.rickandmorty.presentation.theme.dimens

@Composable
fun ErrorBanner(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contentColor = MaterialTheme.colorScheme.onErrorContainer
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(MaterialTheme.dimens.spacingL),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.dimens.spacingM),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error),
            contentDescription = null,
            tint = contentColor
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.characters_error_message),
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
            TextButton(
                onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(contentColor = contentColor),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}

@Preview
@Composable
private fun ErrorBannerPreview() {
    RickAndMortyTheme {
        ErrorBanner(onRetry = {})
    }
}
