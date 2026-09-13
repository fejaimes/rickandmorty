# Rick and Morty — Listado de personajes

Aplicación Android nativa que consume la [Rick and Morty API](https://rickandmortyapi.com/) y muestra el listado paginado de personajes con:

- Carga inicial con indicador de progreso.
- Scroll infinito.
- Imágenes con placeholder mientras cargan.
- Manejo de errores con reintento.

**Stack:** Kotlin · Jetpack Compose · Material 3 · Hilt · Ktor · kotlinx.serialization · Coil 3 · JUnit 4 · MockK

---

## Cómo ejecutar

1. Crea `key.properties` en la raíz del proyecto (el archivo está en `.gitignore`):

   ```properties
   baseUrl=https://rickandmortyapi.com/api/
   ```

2. Compila e instala, o ejecuta los tests (en Windows usa `gradlew.bat`):

   ```bash
   ./gradlew installDebug
   ./gradlew testDebugUnitTest
   ```

**Requisitos:** Android Studio (con su JDK incluido) y un dispositivo o emulador con Android 10 (API 29) o superior.

---

## Estructura

```
app/src/main/java/com/example/rickandmorty/
├── data/            # Ktor API, DTOs @Serializable y CharacterRepositoryImpl
├── domain/          # Entidades, interfaz CharacterRepository y GetCharactersUseCase
├── di/              # Módulos de Hilt: red, repositorio e imágenes (Coil)
└── presentation/
    ├── MainActivity.kt
    ├── characters/  # CharactersScreen, CharactersViewModel, CharactersUiState, components/
    └── theme/       # Colores, colores extendidos, dimensiones y tipografía
```

---

## Decisiones técnicas

### 1. Arquitectura

- **Clean Architecture en tres capas.** Las dependencias apuntan hacia el dominio: `presentation → domain ← data`.
- **MVVM.** `CharactersViewModel` expone un único `StateFlow<CharactersUiState>`. La UI lo observa con `collectAsStateWithLifecycle`, que deja de recolectar cuando la app pasa a segundo plano.
- **Repository y caso de uso.** `GetCharactersUseCase` delega en `CharacterRepository`. El repositorio envuelve cada llamada en `runCatching` y devuelve `Result<T>`, así ninguna excepción de red o de serialización llega a la UI.
- **Hilt** resuelve todas las dependencias mediante `NetworkModule`, `RepositoryModule` e `ImageModule`.

### 2. Red

- **Ktor con engine OkHttp y kotlinx.serialization.** Con `ignoreUnknownKeys` se modelan solo los campos que la pantalla necesita (`id`, `name`, `status`, `species`, `image`).
- **URL base fuera del código:** se lee de `key.properties` y se expone como `BuildConfig.BASE_URL`.
- **`expectSuccess = true`:** cualquier respuesta distinta de 2xx se convierte en error, que el repositorio transforma en `Result.failure`.

### 3. Estado de UI y paginación

```kotlin
sealed interface CharactersUiState {
    data object Loading : CharactersUiState
    data object Error : CharactersUiState
    data class Content(
        val characters: List<CharacterEntity>,
        val appendState: AppendState
    ) : CharactersUiState
}

enum class AppendState { IDLE, LOADING, ERROR, END_REACHED }
```

- **Un estado para la pantalla y otro para el final de la lista.** `CharactersUiState` indica qué se ve: carga inicial, error inicial o lista. `AppendState` indica qué ocurre al final: nada, cargando, error o no hay más páginas. Así un fallo al paginar no borra lo ya cargado.
- **Paginación en el ViewModel.** Reutiliza `GetCharactersUseCase(page)`: el ViewModel guarda la siguiente página y concatena los resultados. Cada transición de estado está cubierta por tests unitarios.
- **Scroll infinito.** Un `derivedStateOf` detecta cuándo el último elemento visible está a 5 filas o menos del final, y un `LaunchedEffect` con `snapshotFlow` llama a `loadNextPage()`. El ViewModel ignora la llamada si no está en `IDLE`, lo que evita peticiones duplicadas mientras carga, tras un error o al llegar a la última página.
- **Errores según contexto.**
  - Si falla la primera página, se muestra un banner centrado con "Reintentar".
  - Si falla una página siguiente, la lista se conserva y el banner aparece al final. El scroll infinito se pausa hasta que el usuario reintenta.

### 4. Imágenes con Coil 3

- **Configuración inyectada con Hilt, sin tocar la `Application`.** `ImageModule` provee un `ImageLoader` singleton, y `MainActivity` lo registra con `setSingletonImageLoaderFactory` (inyectado como `Lazy<ImageLoader>`). Todos los `AsyncImage` lo usan sin pasarlo explícitamente.
- **Caché explícita:** en memoria hasta el 25 % disponible y en disco hasta 100 MB (`cacheDir/image_cache`), con `crossfade` y `DebugLogger` solo en debug.
- **`HttpClient` dedicado a imágenes** (`@ImageHttpClient`) sin plugin de logging. Compartir el cliente de la API hacía que su log `BODY` volcara los bytes JPEG de cada avatar en logcat.
- **Placeholder con la inicial.** Mientras la imagen carga, o si falla, el avatar muestra la inicial del nombre. Se usa `AsyncImage` con `onState` en lugar de `SubcomposeAsyncImage`, que es más costoso dentro de listas.

### 5. Diseño y theming

- **Partimos de un diseño Material 3 hecho en Claude Design.** Se implementó el listado.
- **Dynamic color.** Los colores fijos del diseño se sustituyeron por roles de `MaterialTheme.colorScheme` (`surfaceContainer`, `secondaryContainer`, `errorContainer`…), así la app respeta la paleta del sistema en Android 12+.
- **Ningún valor de dimensión o color escrito a mano fuera de `theme/`:**
  - `MaterialTheme.dimens` (`CompositionLocal`) centraliza espaciados y tamaños.
  - `MaterialTheme.extendedColors` aporta colores sin rol en M3, como el verde de "Vivo", con variante clara y oscura para mantener el contraste.
  - Las esquinas usan `MaterialTheme.shapes` y los textos, `MaterialTheme.typography`.
- **Textos en español** desde `strings.xml`.

### 6. Testing

- **Tests unitarios** con JUnit 4 y MockK no relajado, `confirmVerified` en `@After`, estructura *given / when / then* y `kotlinx-coroutines-test` con una `MainDispatcherRule`.
- **Cobertura:**
  - DTOs y su mapeo a dominio.
  - Repositorio y caso de uso.
  - `CharactersViewModelTest` con 10 casos: carga inicial, última página, error inicial, reintento, acumulación de páginas, error al paginar, reintento de página y descarte de peticiones duplicadas.

---

## Qué mejoraría con más tiempo

- **Tests de UI y regresión visual:**
  - tests de Compose para los estados de `CharactersContent`;
  - screenshot tests de los componentes.
- **Errores más útiles** mensajes distintos para "sin conexión" y "error del servidor".
- **Caché local con Room** para uso offline.
- **Dimensiones adaptativas.** `RickAndMortyDimens` ya lo permite; faltan variantes para tablets y pantallas grandes según `WindowSizeClass`.
- **Paleta de marca para Android 10 y 11**, donde no hay dynamic color y hoy se usan los colores de la plantilla.
- **Accesibilidad:** revisar fuentes grandes, contraste y lectura con TalkBack en todos los estados.

---

## Uso de IA

La **interfaz gráfica** —pantalla de personajes, componentes Compose— se desarrolló con ayuda de **Claude Code** (Anthropic). Se siguió **Spec-Driven Development (SDD)** con el plugin **[Superpowers](https://github.com/obra/superpowers)**, cuyas skills estructuran el trabajo en fases y exigen la aprobación del desarrollador antes de pasar a la siguiente.

### Flujo seguido

1. **Brainstorming** (`superpowers:brainstorming`)
   - La IA leyó el diseño de Claude Design y el código existente.
   - Planteó preguntas de alcance una a una: qué partes del diseño implementar, estilo de carga, tipo de paginación, tema y manejo de errores.
   - Propuso alternativas con sus pros y contras; el desarrollador eligió en cada punto.
2. **Spec** — el diseño aprobado se documentó en `docs/superpowers/specs/` y el desarrollador lo revisó.
3. **Plan de implementación** (`superpowers:writing-plans`) — el spec se descompuso en tareas pequeñas con archivos, código y comandos de verificación, guardadas en `docs/superpowers/plans/`.
4. **Ejecución con TDD** (`superpowers:executing-plans`, `superpowers:test-driven-development`)
   - Primero los tests del ViewModel, comprobando que fallan; después la implementación.
   - Luego los componentes Compose y los módulos de Hilt, compilando y verificando en cada tarea.
5. **Depuración sistemática** (`superpowers:systematic-debugging`)
   - Ante comportamientos inesperados en el dispositivo físico, primero se buscó la causa raíz y después se corrigió.
   - Se usaron logcat, capturas de pantalla y logs temporales de diagnóstico.
   - Así se identificaron el rate limit 429 de la API, el volcado de imágenes en el log y el salto del pie de lista al reintentar.
