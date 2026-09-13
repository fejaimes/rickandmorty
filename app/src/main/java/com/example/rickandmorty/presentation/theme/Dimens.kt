package com.example.rickandmorty.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class RickAndMortyDimens(
    val spacingXXS: Dp = 2.dp,
    val spacingXS: Dp = 4.dp,
    val spacingS: Dp = 8.dp,
    val spacingM: Dp = 12.dp,
    val spacingL: Dp = 16.dp,
    val spacingXL: Dp = 20.dp,
    val spacingXXL: Dp = 24.dp,
    val avatarSize: Dp = 56.dp,
    val statusDotSize: Dp = 8.dp
)

val CompactDimens = RickAndMortyDimens()

val LocalDimens = staticCompositionLocalOf { CompactDimens }

val MaterialTheme.dimens: RickAndMortyDimens
    @Composable
    @ReadOnlyComposable
    get() = LocalDimens.current
