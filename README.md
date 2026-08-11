# FYP Network

A social/professional networking app — think a stripped-down mix of LinkedIn and a basic project tracker (connections, a feed, groups, and tasks). This was originally my final year project at university, and I recently went back and rebuilt it from scratch on a modern stack.

## Backstory

I built the original version of this app for my final year project. It did the job — passed the module, worked on the day of the demo — but it was very much a student project, and it was built on tech that was already getting old back then, let alone now:

- **Android**: plain Java, built in Eclipse with the old ADT plugin (no Gradle at all), `minSdkVersion 14` / `targetSdkVersion 21`, `ListView` + `Fragment` + `ActionBar` tabs, `AsyncTask` for networking, Volley for HTTP, a hand-rolled `SQLiteOpenHelper` for local storage, and manual JSON parsing with `JSONObject`/`JSONArray` everywhere.
- **Backend**: raw PHP with `mysqli`, string-interpolated SQL queries (yes, SQL-injectable), database credentials hardcoded directly in the PHP files, and passwords "encrypted" with `sha1(password + username)` — not exactly bcrypt.
- **Database**: MySQL, no migrations, no real thought put into indexes or constraints.

None of that is a knock on my past self — that's just what you build in a few months as a student who's never shipped anything to production. But I wanted a piece that actually reflects how I'd build something today, so instead of patching the old codebase, I used it as a functional spec and rebuilt it end to end: new architecture, new stack, new (much better) security posture, same core idea.

## What it does

- Register/login, with a profile you can edit (headline, company, bio, photo, password)
- A feed you can post to, with images, comments, likes, and edit/delete on your own posts
- Send/accept/decline/remove connections, search for people with live status (Connect / Request sent / Accept / Remove shown correctly per person), view anyone's profile and their posts
- Notifications for likes, comments, connection requests/accepts, group additions, and task assignments - clickable, routing to the right place, with an unread-count badge on the nav bar
- Groups with role-based membership (owner/admin/member) - create, edit, delete, and add/remove members after the fact, not just at creation
- A per-group task board with a real detail view per task: status picker, assignee, due date with a countdown, and a completion percentage - not just a tap-to-cycle status

## Tech stack

| Layer | Then (2015-era FYP) | Now |
|---|---|---|
| Android | Java, Eclipse ADT, no Gradle, `ListView`/`AsyncTask` | Kotlin, Jetpack Compose, Hilt, Coroutines/Flow |
| Backend | Raw PHP + `mysqli` (SQL-injectable) | NestJS (TypeScript) |
| Auth | `sha1(password + username)`, no tokens | JWT access + rotating refresh tokens, bcrypt |
| Database | MySQL, no migrations | PostgreSQL + Prisma |
| Media | `move_uploaded_file` to local disk | MinIO (S3-compatible, local) |
| CI | None | GitHub Actions (lint, test, build) |

I picked this stack deliberately rather than just picking "whatever's newest." A few of the calls I made, and why:

**Kotlin + Compose over staying in Java/XML.** This is genuinely where Android hiring is right now — Compose has been the recommended approach for a while, and showing I can build a real app with it (state hoisting, ViewModels driving `StateFlow`, Hilt for DI) is a much stronger portfolio signal than a Java/XML app would be.

**NestJS over a plain Express app.** I could've written this in bare Express and it would've worked fine for something this size, but Nest's module/DI structure is closer to what you actually see in larger production codebases, and it comes with Swagger generation and a testing setup out of the box. It also gave me a reason to work in TypeScript on the backend, which I wanted for variety — same type-safety mindset as Kotlin, different ecosystem.

**PostgreSQL + Prisma over MySQL/TypeORM.** Postgres is the default choice for most new backend work I see in job postings these days, and Prisma's migration workflow and generated types made the schema changes across phases (adding connections, notifications, groups, tasks) much less error-prone than hand-writing migrations would've been. The trade-off is Prisma's query API is a bit more opinionated/less flexible than raw SQL or a query builder — for a project this size that trade-off is easily worth it.

**JWT with rotating refresh tokens, not sessions.** Since this is a mobile client hitting an API, token-based auth made more sense than cookie sessions. Refresh tokens are stored hashed (never in plaintext) and rotate on every use — a stolen refresh token is only good for a single use before it's invalidated. Access tokens are short-lived (15 min) so a leaked one has a small blast radius.

**MinIO instead of actually using S3.** I wanted the whole thing runnable locally for free, with no AWS account and no bill. MinIO speaks the S3 API, so the code (`@aws-sdk/client-s3`) is the same code you'd use against real S3 — swapping the endpoint is the only change needed to point this at a real bucket later.

**Cursor-based pagination for the feed**, not offset/limit. Offset pagination gets slow and can skip/duplicate items as new posts come in; cursoring on `id` avoids both problems and is the standard approach once you're thinking past a toy dataset.

**Notifications as a side effect, not a public endpoint.** There's no `POST /notifications` — notifications only ever get created from inside `PostsService`, `ConnectionsService`, and `TasksService` as a consequence of a real action (like, comment, connection response, task assignment). That was a deliberate choice: letting clients create arbitrary notifications is both a spam vector and just not how notifications should work.

**Role-based permissions on groups**, done the boring, unglamorous way — checking membership and role on every group/task mutation server-side rather than trusting the client to only show the "right" buttons. The Android app also respects roles in the UI, but the server never assumes the client is being honest.

## Security, compared to the original

This was one of the biggest motivations for the rewrite. The old backend had three separate problems I wasn't proud of:

1. **SQL injection.** Every query was built with string interpolation (`"...VALUES('$name', '$email')"`). The new backend goes through Prisma's parameterized query builder everywhere — there's no path to raw string-built SQL in the codebase.
2. **Weak password hashing.** `sha1(password + username)` isn't a password hash, it's a fast general-purpose hash — exactly what you don't want for passwords, since it's cheap to brute-force at scale. The new backend uses bcrypt with a cost factor of 12.
3. **Hardcoded credentials in source.** The old PHP had the DB username/password committed directly in the file. Everything now comes from `.env`, which is gitignored, with `.env.example` committed instead so anyone cloning the repo knows what to set.

Refresh tokens are also stored hashed (not plaintext) in the database, so a database leak alone doesn't hand out working session tokens.

## Architecture

```
backend/    NestJS + Prisma + PostgreSQL API, MinIO for media
android/    Kotlin + Jetpack Compose client
```

**Backend** is organized as one Nest module per domain (`auth`, `users`, `posts`, `connections`, `notifications`, `groups`, `tasks`, `media`), each with its own controller/service/DTOs, wired together through Nest's DI container. Cross-module calls go through injected services (e.g. `PostsService` calls into `NotificationsService`) rather than reaching into another module's internals.

**Android** follows MVVM: each screen has a `ViewModel` exposing a single `StateFlow<UiState>` data class, a Compose screen that renders it, and a `Repository` the ViewModel talks to. Networking goes through Retrofit + OkHttp, with a custom `Authenticator` that transparently refreshes an expired access token and retries the original request — no individual screen needs to know or care that a token expired mid-session. Auth state (logged in/out) is observed from `DataStore` at the root of the app and drives which nav graph start destination gets used, so a logout from anywhere in the app correctly kicks the user back to the login screen.

## Getting started

Everything runs locally for free — no AWS account, no paid services, nothing beyond Docker and Node.

### Backend
```bash
cd backend
cp .env.example .env
npm install
docker compose up -d              # Postgres + MinIO
npx prisma generate
npx prisma migrate dev --name init
npx prisma db seed                # optional: two test users + a sample post
npm run start:dev
```
API docs (Swagger) are at `http://localhost:3000/api/docs` once it's running.

Seeded test login: `jane.doe@example.com` / `password123`

### Android
Open `android/` in Android Studio, let Gradle sync, and run on the emulator — it's already pointed at `http://10.0.2.2:3000/api/`, which is the emulator's alias for your machine's localhost. For a physical device, point `BASE_URL` in `app/build.gradle.kts` at your machine's LAN IP instead.

Full setup notes for each half live in `backend/README.md` and `android/README.md`.

## Testing & CI

- `backend/src/**/*.spec.ts` — Jest unit tests, e.g. verifying that passwords are actually bcrypt-hashed rather than stored in a recoverable form, and that login returns the same error for "wrong password" and "no such user" (so the API doesn't leak which one it was).
- `.github/workflows/backend-ci.yml` — GitHub Actions spins up a real Postgres service container and runs lint → test → build on every push/PR to `main`.
- `.github/workflows/android-ci.yml` — lint, unit tests, and a debug build on every push/PR touching `android/`. One honest caveat: this repo doesn't have committed Gradle wrapper files (`gradlew`/`gradlew.bat`), so CI provisions a pinned Gradle version directly rather than using `./gradlew` the way most Android CI setups do. Functionally equivalent, but worth knowing - generating and committing a real wrapper (`gradle wrapper --gradle-version 8.9` from the `android/` folder) would let CI and local dev use the exact same Gradle version, which is the more standard setup.

## Known limitations

- Test coverage is deliberately light — a handful of unit tests on the auth and connections logic, not an exhaustive suite. For this project I prioritized breadth (getting every phase working end-to-end) over depth of testing on any one module.
- No real-time updates — the feed and notifications are pull-based (fetch on screen load / pull to refresh), not pushed over a websocket.
- No caching layer — every screen hits the API directly. Fine at this scale, would need addressing before this handled real traffic.
- The task board UI is intentionally simple (tap to cycle status) rather than full drag-and-drop, to keep the Compose code focused rather than fighting gesture detection.

## Visual design

The app was originally built on plain Material 3 defaults - functional but
generic. I gave it a real design pass afterward: a bold/vibrant palette
(electric violet → hot pink → sunset orange), a rounder shape system, and a
bolder type scale, all defined once in `ui/theme/` and inherited by every
default-styled component app-wide - every `Card`, `Button`, `AlertDialog`,
`Slider`, and `TextField` picked up the new shapes and colors automatically,
with zero per-screen changes required for that baseline uplift.

On top of that, a few screens got deliberate custom treatment:
- **Login/Register**: a gradient hero band with an overlapping rounded card,
  rather than a flat centered form
- **Feed**: a gradient ring around post author avatars, a gradient circular
  FAB instead of the flat default, and a spring-animated like button
- **Profile**: a gradient banner behind the avatar with a stat row, mirroring
  the auth screens' visual identity so the app reads as one coherent product
  rather than a stack of default Compose screens

Groups, Connections, Notifications, and the detail screens inherit the shared
theme (rounded cards, violet/pink accents on every default button, slider,
and progress bar) but weren't given their own bespoke hero treatments in this
pass - a reasonable next step if the visual identity needs to go further.

## Round one: the post-launch punch list

After getting the first version running end-to-end, I put together a punch list of 32 issues from actually using the app — some real bugs, some missing features, a couple of product decisions worth documenting rather than "fixing." Working through that list surfaced two bugs I'm glad I caught before calling this done:

**Two real client-side bugs, found by dogfooding:**
- `GET /auth/me` was being called through the *unauthenticated* Retrofit client (the one built specifically for login/register/refresh, which deliberately carries no bearer token to avoid a circular dependency in the token-refresh flow). That meant loading your own profile was silently sending no `Authorization` header and getting a 401 every single time. Fixed by moving it onto the authenticated client instead.
- MinIO's public-read bucket policy was only ever applied the *first* time the bucket got created - on every later restart the bucket already existed, so the policy step was skipped. Any bucket created before that code existed (or reset for any reason) stayed private forever, which is exactly what was causing uploaded images to silently fail to load after a restart.

**Product decisions worth calling out explicitly** (not fixed, because they weren't broken):
- The feed intentionally shows public posts from people you're not connected to, not just your connections' posts - the alternative (feed restricted to connections only) leaves a brand-new user staring at an empty screen until they connect with someone, which is worse for onboarding. This mirrors how LinkedIn's own feed behaves for new accounts.
- Bottom navigation (Feed / Groups / Alerts / Profile) is the actual pattern Facebook, LinkedIn, and Instagram all use today - it wasn't replaced with a different layout, since that would be moving away from the modern convention rather than toward it.

**Fixed/added this round:**
- Registration no longer auto-signs you in - it creates the account and shows a confirmation screen, and you log in separately afterward
- Feed and Groups both got pull-to-refresh, plus an "on resume" reload so returning from creating a post/task always shows current data instead of a stale list
- Comments actually work now - tapping the comment icon opens a real post detail screen with the comment thread and a reply box
- Notifications are clickable and route to the right place (a connection request opens Connections, a like/comment opens that post, a task/group notification opens that group) and get marked read on tap
- Post timestamps are human-readable ("3h ago" / a full date once it's old) instead of a raw ISO string
- You can edit or delete your own posts from a menu on the post card
- Profile fields are read-only until you tap "Edit profile" - no more accidentally-editable text fields sitting on the page
- Your own posts now show on your profile
- Tapping someone (in search, in your connections list, or on a post) opens their profile with their posts, instead of doing nothing
- Password change, with the current password required and all sessions revoked server-side on change
- Groups can be created with initial members from your connections, and members get notified when they're added (whether at creation or afterward)
- Task interaction moved from "tap to blindly cycle status" to an actual task detail view: status picker, an assignee picker, a due date (with a "Due in 3 days" / "Overdue by 2 days" countdown shown on the card), a completion percentage slider, and edit/delete

**Deliberately deferred, and why:**
- **Push notifications** - in-app notifications work today; turning them into real push notifications needs a Firebase Cloud Messaging project, which is its own piece of free-tier setup outside this codebase. Already listed under "what I'd do next" below.
- **Video playback** - the media pipeline is already format-agnostic (mime type is stored per attachment), but actually playing video back in the feed needs a player dependency (e.g. Media3/ExoPlayer) that I didn't want to bolt on without doing it properly.
- **Task comments and a dedicated media gallery tab** - both are reasonable next features, but each is closer to a small new module than a bug fix, so I scoped them out of this pass rather than rush them in.
- **Automatic "task due soon" reminder notifications** - the due date is there and the countdown display works; turning that into a scheduled background check needs a cron job (`@nestjs/schedule`), which is real but small infrastructure I'd rather add deliberately than squeeze in.

## Round two: more real usage, more real bugs

The first punch-list pass got the app fully usable, but actually living in it for a while (not just clicking through each screen once) turned up a second batch of issues - smaller in count, but a couple of them were genuinely instructive.

**The same class of bug, twice.** The API base URL needing to be `10.0.2.2` instead of `localhost` for the Android emulator is a well-known gotcha, and I'd handled it correctly for the API itself from the start. What I missed: the backend also embeds URLs in its *responses* for anything else that isn't the API host directly - specifically, image URLs pointing at MinIO. Those were still built from the backend's internal `localhost:9000`, which is correct for the backend's own connection to MinIO but wrong for anything the client needs to fetch directly. Same root cause as the API URL issue, just one layer further in, and easy to miss because there's no error - the image request just fails silently. Fixed by separating "the address the backend uses internally" from "the address embedded in responses for clients to use," which is really the more correct design regardless of the emulator specifically - a real deployment would need that same separation once MinIO sits behind a CDN or different hostname than the API.

**Connection status needed a source of truth.** The Connect/Remove/Request-sent button was originally tracked as local UI state - set it when you tap "Connect," done. That falls apart the moment the screen gets recreated (closing and reopening a profile) or the moment there's more than one place the button appears (a profile screen vs. a search result row), because there's no single place that state lives. Fixed by adding a real `GET /connections/status/:userId` endpoint that answers "what's my relationship with this person" from the database directly, and having both the profile screen and search results query it instead of tracking their own copy of the truth. This is a pattern worth calling out on its own: UI state that mirrors server state instead of deriving from it will eventually drift from reality, and the fix is almost always "stop tracking a copy, go ask the source."

**Fixed/added this round:**
- Groups can now be edited (name/description) and deleted, not just created - gated server-side by role (edit needs admin+, delete needs the owner specifically)
- Group membership can be managed after creation - add people from your connections, remove members (except the owner) - not just at the moment the group is created
- A real unread-notification badge on the Alerts tab, refreshed on every tab switch
- Search results now show your actual connection status with each person (Connect / Request sent / Accept / Remove) instead of always showing "Connect" regardless of reality - which also fixed a confusing "nothing happens when I click Connect" symptom that was really the backend correctly rejecting a duplicate request with no visible feedback

**A note on the build/debug process itself**, since it's part of the honest story of building this: a batch of Kotlin/Compose compile and runtime errors came up while actually building and running the app in Android Studio after each round of changes - missing imports for extension functions vs. member functions (a real, easy-to-miss Kotlin distinction: `Modifier.weight()` is a member of `RowScope`/`ColumnScope` and needs no import at all, while most other modifiers do), a runtime crash from passing a negative value to `Modifier.padding()` where `Modifier.offset()` was the actually-correct API, and a couple of genuinely stale build-cache situations. Debugging those against real Android Studio error output (not just "it looks right") is part of why later rounds of changes came with a proper static sweep across the whole codebase for the same class of mistake before shipping, rather than fixing only the one line that was reported.



If I kept going, these are the three things I'd tackle first — I'm calling them out explicitly rather than leaving them as a vague "roadmap" section, since they're the features I'd talk through if asked "what's missing":

- **Free-tier deployment.** Right now this only runs locally. Deploying the backend to a free tier (Render/Railway/Fly.io all have one) and pointing the Android app at it would turn this from "clone and run" into something with a live demo link — the main thing stopping me so far is that free-tier databases usually sleep/reset, which isn't a great demo experience, so I'd want to pair it with a lightweight "wake up" health check first.
- **Offline caching.** The Android app currently has zero offline story — kill your connection and every screen just shows an error. Adding a Room-backed cache layer (feed posts, at minimum) so the app degrades gracefully instead of going blank would be the next architectural piece I'd add, and it'd be a good excuse to introduce a proper single-source-of-truth repository pattern instead of the current direct API-to-UI-state flow.
- **Push notifications.** Notifications currently only show up if you open the app and pull the list. Firebase Cloud Messaging is free at this scale and would close that gap — the backend already generates notification records at the right moments, so this is mostly an Android-side + a small "register device token" endpoint on the backend.

None of these are blocking — the app works end-to-end without them — but they're the natural next layer once the core is solid.

## License

MIT — do whatever you want with it.
