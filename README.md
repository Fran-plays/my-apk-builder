# PetMorph AI 🐾

Turn any image into your personal VTuber companion. Upload a photo of a pet,
anime character, mascot or drawing — PetMorph removes the background, animates
it with squash-and-stretch, gives it a voice and lets it float over other apps.

**100% original demo character included: _Mochi_** (generated placeholder art,
works immediately after install, no account needed).

---

## 1. Project structure

```
petmorph-ai/
├── app/                              # Android app (Kotlin + Jetpack Compose)
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/petmorph/ai/
│       │   │   ├── MainActivity.kt
│       │   │   ├── PetMorphApp.kt
│       │   │   ├── core/AppContainer.kt          # manual DI
│       │   │   ├── ui/
│       │   │   │   ├── CompanionViewModel.kt     # state management
│       │   │   │   ├── screens/                  # all 10+ screens
│       │   │   │   └── theme/
│       │   │   ├── navigation/AppNavHost.kt
│       │   │   ├── data/
│       │   │   │   ├── model/CompanionEntity.kt  # Room entity, personality enum
│       │   │   │   ├── local/                    # Room DB + DAO
│       │   │   │   └── repository/CompanionRepository.kt
│       │   │   ├── domain/                       # (extension point)
│       │   │   ├── network/ApiClient.kt          # Retrofit DTOs + API
│       │   │   ├── database/                     # (Room lives in data/local)
│       │   │   ├── animation/
│       │   │   │   ├── AnimationState.kt         # 13 states
│       │   │   │   ├── AnimationEngine.kt        # squash/stretch/rig-free engine
│       │   │   │   └── CompanionCanvas.kt        # bitmap + layers renderer
│       │   │   ├── audio/                        # ToneGenerator SFX (royalty-free)
│       │   │   ├── overlay/
│       │   │   │   ├── FloatingCompanionService.kt
│       │   │   │   └── OverlayPermissionHelper.kt
│       │   │   ├── ai/AIProvider.kt              # AIProvider + offline fallback
│       │   │   ├── tts/                          # TTSProvider + Android TTS impl
│       │   │   ├── storage/StorageProvider.kt
│       │   │   ├── settings/SettingsManager.kt   # DataStore
│       │   │   └── subscription/SubscriptionService.kt  # freemium gate
│       │   ├── res/                              # Mochi asset, icons
│       │   └── test/                             # JVM unit tests
│       └── androidTest/
├── server/                           # Node.js + TypeScript backend
│   ├── src/
│   │   ├── index.ts                  # Express bootstrap
│   │   ├── config.ts                 # env-driven config
│   │   ├── controllers/  routes/  services/
│   │   ├── providers/                # AIProvider, TTSProvider, ImageProcessor, StorageProvider
│   │   ├── middleware/               # rate limiting, upload validation, auth, errors
│   │   ├── models/                   # zod schemas
│   │   ├── database/                 # pg pool + schema.sql migration
│   │   └── utils/logger.ts
│   ├── tests/                        # jest + supertest
│   ├── .env.example
│   └── Dockerfile
├── gradle/libs.versions.toml
└── README.md
```

## 2. Build instructions (Android)

Requirements: JDK 17+, Android SDK 34 (command-line tools are enough — Android
Studio is **not** required).

```bash
# from repo root
./gradlew assembleDebug        # -> app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease      # -> app/build/outputs/apk/release/app-release.apk
./gradlew test                 # JVM unit tests (animation, AI responder, voice config)
```

The debug build points at `http://10.0.2.2:4000` (Android emulator → host
loopback). Change `API_BASE_URL` in `app/build.gradle.kts` for a real server.
**No API keys exist inside the APK** — the app works fully offline; cloud
features activate automatically when the backend is reachable.

## 3. Backend

```bash
cd server
cp .env.example .env          # fill in what you have; mocks cover the rest
npm install
npm run migrate               # creates PostgreSQL schema (001)
npm run dev                   # http://localhost:4000
npm test                      # validation, provider, API error tests
```

### Environment variables

| Variable | Purpose |
|---|---|
| `PORT` | listen port (default 4000) |
| `DATABASE_URL` | PostgreSQL connection string |
| `JWT_SECRET` | signing secret for optional auth tokens |
| `AI_PROVIDER` | `openai` or `mock` (default `mock`) |
| `AI_API_KEY` / `AI_BASE_URL` / `AI_MODEL` | AI provider credentials (server-side only) |
| `TTS_PROVIDER` | `cloud` or `mock` |
| `IMAGE_PROVIDER` | `local` (sharp) or `removebg` |
| `STORAGE_PROVIDER` | `local` (disk) or `s3` |
| `MAX_UPLOAD_MB` | upload size cap (default 10) |

### API

```
GET    /api/health
GET    /api/characters
POST   /api/characters/create        (multipart: image + name/type/personality)
GET    /api/characters/:id
DELETE /api/characters/:id/delete    (Bearer token required)
POST   /api/ai/chat                  {name, personality, message, maxLength, language}
POST   /api/tts                      {text, voice?}  -> audio/wav
POST   /api/image/process            (multipart image) -> {imageBase64}
```

Security: zod validation, MIME + magic-byte checks, size limits, helmet,
CORS, per-route rate limits, optional JWT auth, sanitized logs. Secrets only
ever live in server environment variables.

### Deployment (Docker)

```bash
docker build -t petmorph-server server/
docker run -p 4000:4000 --env-file server/.env petmorph-server
```

Works as-is on Render / Railway / Fly.io / any Node host. Provision PostgreSQL,
set `DATABASE_URL`, run `npm run migrate`.

## 4. What works where

| Feature | Offline | With backend |
|---|---|---|
| Create companion from image (local bg removal + normalize) | ✅ | ✅ (cloud processing optional) |
| 13 animations, pseudo-blink, talking mouth, particles | ✅ | ✅ |
| Local Android TTS (speed/pitch/volume/mute) | ✅ | ✅ (+ cloud TTS endpoint) |
| Generated sound effects (9) | ✅ | ✅ |
| AI chat | ✅ local personality responder | ✅ cloud LLM via provider |
| Companion library (Room) create/rename/duplicate/delete | ✅ | ✅ |
| Floating overlay (drag/tap/double-tap/resize/opacity) | ✅ (needs overlay permission) | ✅ |
| Cloud backup / accounts | — | architecture ready (JWT) |

## 5. End-to-end flow (all implemented)

Onboarding (3 screens, skippable) → Home → Create (name, type: Cute Pet /
Anime / Mascot / VTuber Mini, personality, image upload) → Processing
(loading / error / retry states) → "Meet your new companion!" → animation
picker + voice tuning → Interact (tap/double-tap reactions, chat with speech
bubble + TTS) → Make Floating (permission explained first, graceful fallback
if denied) → Library management → Settings (theme, opacity, size, sounds,
subscription).

## 6. Known limitations (honest list)

- **Single-image animation is hybrid, not skeletal rigging.** Motion comes
  from squash/stretch, rotation, offsets, pseudo-blink (vertical squish),
  a mouth overlay during speech and particles. Limbs don't articulate — real
  rigging from one image isn't reliable, so we don't pretend.
- Background removal is a flood-fill heuristic; busy backgrounds may need the
  cloud `IMAGE_PROVIDER=removebg`.
- Talking-mouth sync uses the TTS utterance duration, not phoneme analysis.
- Cloud TTS/S3 providers are abstractions with mock/local defaults wired in;
  drop-in implementations are stubbed where credentials aren't configured.
- Play Billing is abstracted behind `SubscriptionService.BillingDelegate`
  (no fake payment screens, per spec) — connect your products to go live.
- The `removebg` provider and OpenAI provider make real network calls; tests
  cover the mock/local paths so CI needs no keys.

## 7. Implemented feature checklist

Splash · Onboarding · Home · Create (upload PNG/JPG/WEBP) · local background
removal · transparent PNG asset · normalize/crop/resize · 13 animation states ·
voice setup (rate/pitch/volume/mute) · animation preview · interactive preview
with chat + speech bubble · local TTS with mouth sync · 9 generated SFX ·
Room library (CRUD + duplicate) · floating overlay service (drag/tap/double
tap/size/opacity/hide/show/mute/close) · overlay permission UX with fallback ·
dark/light themes · DataStore settings · freemium gate · AI/TTS/Image/Storage
provider abstractions · offline-first AI chat fallback · demo character Mochi
seeded on first run · backend with all 8 specified routes + validation, rate
limiting, logging · jest/supertest tests · Gradle debug+release configs ·
ProGuard/R8 rules.

## 8. Cara Termudah (tanpa install apa pun di laptop)

1. Buat repo GitHub, upload isi folder ini, push ke branch `main`.
2. GitHub Actions otomatis membangun **app-debug.apk** dan **app-release.apk**
   (lihat tab **Actions** → run terbaru → bagian **Artifacts** → unduh).
3. Deploy backend: di Render pilih **New → Blueprint**, hubungkan repo —
   `server/render.yaml` otomatis membuat database PostgreSQL gratis,
   menjalankan migrasi, dan men-deploy API dengan health check.
4. Setelah backend hidup, ganti `API_BASE_URL` di `app/build.gradle.kts`
   (bagian `release`) ke URL Render Anda, push — APK baru terbangun otomatis
   dengan konfigurasi cloud yang benar.
