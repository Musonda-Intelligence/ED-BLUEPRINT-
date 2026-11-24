# ED Blueprint (Android)

Online learning app for Engineering Drawing students. Kotlin, Jetpack Compose.
Built in increments; see `docs/PROGRESS.md` for what works today.

## Run it

1. Install Android Studio (Ladybug or newer) with JDK 17.
2. Open this folder in Android Studio and let Gradle sync.
3. In `gradle.properties`, set `EDB_API_BASE_URL` to your real API address
   (must start with `https://` and end with `/api/`).
4. Choose a phone or emulator (Android 8.0 / API 26 or newer) and press Run.

The app needs the backend described in `docs/API_CONTRACT.md` to log anyone in.

## Folders

- `app/src/main/java/com/edblueprint/app/ui/theme` colours and type (from the logo)
- `ui/components` buttons, inputs, layout pieces used by every screen
- `ui/auth`, `ui/student`, `ui/admin` the screens
- `data/` talking to the server and storing the login token
- `docs/` API contract and progress

## First build

This code was written without an Android SDK available, so it has not been compiled yet.
Expect to fix a few small compile errors on the first sync. Send them to Claude and they
can be fixed quickly.

## What the app expects from the server

See `docs/API_CONTRACT.md`. Premium files must only be served by `/materials/{id}/file`
after the server checks the subscription. Do not put premium files at public URLs.
