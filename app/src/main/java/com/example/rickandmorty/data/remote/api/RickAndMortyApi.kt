package com.example.rickandmorty.data.remote.api

import com.example.rickandmorty.data.remote.dto.CharacterPageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class RickAndMortyApi @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun getCharacters(page: Int): CharacterPageDto =
        httpClient.get("character") {
            parameter("page", page)
        }.body()
}
