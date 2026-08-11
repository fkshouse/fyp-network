# FYP Network — Android

Kotlin + Jetpack Compose client for the rebuilt social/professional network.

## Stack
- **Kotlin** + **Jetpack Compose** (Material 3) — no XML layouts, no `findViewById`
- **Hilt** — dependency injection
- **Retrofit + OkHttp + kotlinx.serialization** — networking, with a custom
  `Authenticator` that transparently refreshes expired access tokens
- **DataStore** — stores the JWT access/refresh token pair
- **Coil** — image loading
- **Navigation Compose** — screen routing

## Prerequisites
- Android Studio (Ladybug or newer) with the Android SDK (min API 26 / Android 8.0, target API 35)
- The backend running locally (see `../backend/README.md`) — start that **first**

## Running it
1. Open the `android/` folder as a project in Android Studio and let Gradle sync.
2. Make sure the backend + Docker containers are up (`docker compose up -d` and
   `npm run start:dev` in `../backend`).
3. Run on the **emulator**: the default `BASE_URL` (`http://10.0.2.2:3000/api/`)
   already points at your host machine's localhost — nothing to change.
4. Run on a **physical device** instead: edit the `debug` build type in
   `app/build.gradle.kts` to point `BASE_URL` at your machine's LAN IP
   (e.g. `http://192.168.1.23:3000/api/`), and make sure your phone is on the
   same Wi-Fi network as your machine.
5. Hit ▶ Run. You should land on the login screen — use the seeded test
   account (`jane.doe@example.com` / `password123`) if you ran the backend seed.

## Project structure
```
app/src/main/java/com/fypnetwork/
├── data/
│   ├── local/        TokenManager (DataStore)
│   ├── remote/        Retrofit API interfaces + DTOs
│   └── repository/    AuthRepository, PostsRepository
├── di/                Hilt modules, auth interceptor/authenticator
├── ui/
│   ├── auth/           Login/Register screens + ViewModels
│   ├── feed/            Feed/CreatePost screens + ViewModels
│   ├── navigation/    NavGraph, Destinations
│   └── theme/            Compose Material3 theme
├── FypApplication.kt   @HiltAndroidApp entry point
└── MainActivity.kt      Compose entry point, decides start destination
```

## Notes on architecture choices
- **MVVM**: each screen has a `ViewModel` exposing a single `StateFlow<UiState>`
  data class, rather than the original app's Activities/Fragments doing
  networking, parsing, and UI all in one place.
- **Optimistic UI**: liking a post updates the UI instantly and rolls back
  silently if the server call fails, rather than blocking on a spinner.
- **Token refresh**: handled once, centrally, in `TokenAuthenticator` — no
  individual screen needs to know or care that a token expired mid-session.
