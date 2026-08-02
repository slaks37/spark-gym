# Spark Gym

[![Platform](https://img.shields.io/badge/platform-Android%208.0%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![Language](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Build](https://github.com/slaks37/spark-gym/actions/workflows/build.yml/badge.svg)](https://github.com/slaks37/spark-gym/actions/workflows/build.yml)
[![Licence](https://img.shields.io/badge/licence-MIT-0EA5E9)](LICENSE)
[![Tests](https://img.shields.io/badge/tests-141%20passing-16A34A)](app/src/test/java/com/sparkgym)
[![Exercises](https://img.shields.io/badge/exercises-239-E8A317)](app/src/main/java/com/sparkgym/data/seed)
[![Programmes](https://img.shields.io/badge/programmes-14-0099CC)](app/src/main/java/com/sparkgym/data/seed/RoutineSeedPro.kt)
[![Foods](https://img.shields.io/badge/foods-192-16A34A)](app/src/main/java/com/sparkgym/data/seed/FoodSeed.kt)
[![Languages](https://img.shields.io/badge/i18n-EN%20%7C%20ID-7C4DFF)](app/src/main/java/com/sparkgym/core/util/Strings.kt)
[![Offline](https://img.shields.io/badge/offline-first-64748B)](#privacy)
[![No trackers](https://img.shields.io/badge/trackers-none-E53945)](#privacy)

An Android training app that puts four things most people juggle across four apps into one:

| Inspiration | What it contributes here |
|---|---|
| Muscle Monster / home-gym planners | 14 bundled programmes, a 239-exercise library, home-friendly filters |
| Arise (Solo Leveling) | Levels, ranks, six attributes, daily quests, penalties, achievements |
| Gym Log / Gym Trainer | The set-by-set logger, rest timer, personal records, volume history |
| — | **Muscle heat map** — a body silhouette shaded by what you actually trained |
| MyFitnessPal | Food diary, macro budget from your TDEE, water, barcode lookup, 8 scaling meal plans |
| Fitbit | Steps, heart rate, sleep and calories, via Health Connect *or* the Fitbit Web API |
| — | **AI coach** — rules over your own data: deload calls, plateau detection, weak-point and diet audits |

The pieces are wired together rather than bolted side by side: a logged set feeds
the heat map, the quest board and your Vitality attribute; a synced step count
feeds Agility and your calorie budget; a logged meal feeds Intellect.

---

## Install it

**Just want it on your phone?** Released versions are under
[Releases](../../releases) — download `spark-gym-release.apk`, allow "install from
unknown sources" for your browser or file manager, and open it.

**Want the newest build instead?** Every push is built by CI. Open the latest run
in the [Actions tab](../../actions/workflows/build.yml) and download
`spark-gym-debug-apk`. That one is debug-signed and installs alongside the real
app rather than over it — handy for trying a change, not for daily use.

**Building it yourself.** Requirements: **JDK 17**, **Android Studio Ladybug or
newer** (or just the Android SDK with `compileSdk 35` installed).

```bash
git clone <this repo>
cd spark-gym
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/
./gradlew installDebug           # straight onto a connected device
./gradlew test                   # unit tests for the XP, quest, heat map and nutrition maths
```

Opening the folder in Android Studio and pressing Run works too — no extra setup
is needed for everything except the direct Fitbit link (below).

### Shipping it yourself (Drive, a link, anywhere outside the Play Store)

Android will not install an unsigned APK, and it will not install an update
signed with a *different* key than the version already on the phone — it makes
you uninstall first, which throws away every workout the user has logged. So the
signing key is created **once** and kept forever. Losing it means no existing
install can ever be updated again.

```bash
keytool -genkeypair -v \
  -keystore spark-gym-release.jks \
  -alias spark-gym \
  -keyalg RSA -keysize 4096 -validity 10000
```

Keep `spark-gym-release.jks` somewhere safe and **out of the repository** — it is
already covered by `.gitignore`. Then add four repository secrets under
*Settings → Secrets and variables → Actions*:

| Secret | Value |
|---|---|
| `KEYSTORE_BASE64` | `base64 -w0 spark-gym-release.jks` |
| `KEYSTORE_PASSWORD` | the keystore password |
| `KEY_ALIAS` | `spark-gym` |
| `KEY_PASSWORD` | the key password |

Now either push a `v1.0.0` tag, or run the **Build** workflow manually with
*Also build a signed release APK* ticked. Both produce `spark-gym-release.apk`,
signed and installable. The job verifies the signature before publishing, and
fails outright if the secrets are missing rather than handing you an APK that
dies with "app not installed".

The release build is **not minified** — see the comment in
`app/build.gradle.kts` for why. That makes the APK larger than it needs to be,
and it means the shipped bytecode is exactly what CI tested.

**Bump `versionCode` in `app/build.gradle.kts` for every build you hand out.**
Android refuses to install an APK whose `versionCode` is not higher than the one
already there.

### Google Play

The same workflow also builds the App Bundle Play requires. See
[PLAY_STORE.md](PLAY_STORE.md) for the steps that are not code — signing choice,
the Health Connect declaration, the data safety form, and two risks worth
deciding on before you publish.

---

## Connecting Fitbit

There are two routes, and the app supports both. **Health Connect is the better
one** and needs no configuration at all.

### Route 1 — Health Connect (recommended, zero setup)

The Fitbit app writes steps, heart rate, sleep and calories into Health Connect.
Spark Gym reads them from there, so no account is linked and no token is ever
stored. It also picks up Samsung Health, Wear OS and anything else on the phone.

1. Install **Health Connect** (pre-installed on Android 14+).
2. In the Fitbit app: *Settings → Health Connect* → allow it to write.
3. In Spark Gym: **Status → watch icon → Grant access**.

### Route 2 — direct Fitbit account link (OAuth 2.0 + PKCE)

For phones without Health Connect, or if you want the data without the Fitbit app
installed. This needs a free developer registration because Fitbit requires a
client id per app.

1. Register an app at <https://dev.fitbit.com/apps/new>.
   - **OAuth 2.0 Application Type:** `Client`
   - **Redirect URL:** `sparkgym://fitbit-callback`
   - **Default Access Type:** Read-Only
2. Copy the **OAuth 2.0 Client ID**.
3. Create `local.properties` in the project root (it is git-ignored):

   ```properties
   sdk.dir=/path/to/Android/sdk
   fitbit.clientId=23XXXX
   # optional — only if you want a different custom scheme
   fitbit.redirectScheme=sparkgym
   ```
4. Rebuild. **Status → watch icon → Link Fitbit account.**

No client secret is used or needed: the flow is PKCE, so nothing sensitive is
compiled into the APK. Tokens are stored in `EncryptedSharedPreferences` and are
excluded from cloud backup.

If no client id is present the app still builds and runs — the Fitbit card simply
says "not configured" and Health Connect remains available.

---

## How the systems work

### Levelling

Total XP to reach level *L* is `100 × (L−1)^1.5`, so the first levels come fast and
later ones need real consistency. Ranks unlock at fixed levels: **E** (1), **D** (10),
**C** (20), **B** (32), **A** (46), **S** (62).

XP comes from things you can predict before you do them — 6 per completed set,
10 per tonne moved, 5 per 10 minutes trained, 60 per personal record, 40 for
finishing at all, then a streak multiplier that caps at ×1.5.

### The six attributes

Each one is driven by a different real signal, so you cannot max the sheet by
only doing what you already enjoy:

| | Fed by |
|---|---|
| **STR** Strength | Estimated 1RM on your best lifts, scaled to bodyweight |
| **VIT** Vitality | Weekly effective sets across all muscles |
| **AGI** Agility | Average daily steps |
| **END** Endurance | Cardio minutes and active calories |
| **INT** Intellect | Days with food logged and protein targets hit |
| **PER** Perception | Sleep duration and resting heart rate |

Attributes rise by at most one point per recalculation and never fall — the
System stops rewarding you, it does not take back what you earned.

### Daily quests

Four core quests (push-ups, sit-ups, squats, steps) plus five support quests.
Targets scale with your level and cap where more would stop being useful — 100
push-ups, 12,000 steps, 8 hours of sleep. Miss the core board and the penalty
grows with consecutive misses, capped at 150 XP and never dropping you below
your current level's floor.

### The muscle heat map

Every exercise maps to muscles through a real join table: a prime mover counts as
one effective set, a synergist as half. Colour is your weekly effective sets
against that muscle's own target — 18 for chest, 8 for adductors, and so on,
following standard hypertrophy volume landmarks. A 30-day view is normalised back
to a weekly rate, so a longer window does not simply look redder.

Tap any muscle to see its numbers. The balance score is the coefficient of
variation across all regions, inverted — 100 means nothing is being neglected.

### The coach

The Coach tab is a rules engine over your own logged data — no black box and no
"the AI thinks". Every insight states what was measured, what it means, and one
concrete thing to do. It covers:

- **Fatigue index (0-9)** — weekly volume, sleep debt, unbroken training weeks,
  stalled lifts and resting-heart-rate drift. Four points triggers a deload call.
- **Plateau detection** — a lift is stalled when its estimated 1RM has not
  improved across three sessions. Two data points is noise; three is a pattern.
- **Weak-point audits** — cold regions on the heat map, press-to-pull ratio above
  1.5, hamstrings under half of quad volume.
- **Diet audits** — protein shortfall in grams, and bodyweight trend versus your
  goal, fitted by least squares over three weeks of weigh-ins rather than
  comparing two readings.

### Auto-regulation

The logger uses double progression: hold the weight until you hit the top of the
rep range on *every* set, then add load and drop back to the bottom. Increments
scale with the lift — 2.5 kg upper body, 5 kg lower, or 2.5% of the bar,
whichever is larger.

### Programmes

Fourteen bundled plans, from Full Body 3× Week through Push/Pull/Legs to the
competitive shelf: the Golden Era six-day split, Yates-style Blood & Guts HIT,
FST-7, German Volume Training, 5×5, powerbuilding, a cutting block, a bro split
and a deload week. Each carries its coaching intent, because a programme without
a "why" is just a list of exercises.

### Meal plans

Eight full-day templates — cutting, high-protein cutting, maintenance, lean bulk,
mass gain, vegetarian, budget *anak kos*, and 16:8 fasting. Each is stored as
food slugs plus gram weights rather than a picture, so the app scales every
portion to *your* calorie and protein target and tells you honestly where the
result lands. Fixed items (one egg, a creatine scoop, a cup of coffee) do not
scale. One tap logs the whole day into your diary.

### Nutrition

Calorie budget is Mifflin-St Jeor × activity multiplier × goal delta. Protein is
set from bodyweight (2.2 g/kg cutting, 1.8 g/kg maintaining), fat takes 25% of
calories with a floor, and carbohydrate takes the remainder. The headline number
is goal − eaten + exercise, where exercise comes from your watch.

The bundled food database is 192 entries weighted toward Indonesian staples —
nasi, tempe, tahu, sate, gado-gado, rendang, plus the warung and rumah makan
dishes people actually order — alongside the usual international entries,
because a tracker that only knows about oatmeal gets abandoned in a week.
Barcode scanning and online search use [Open Food Facts](https://openfoodfacts.org).

---

## Architecture

Single module, Kotlin, Jetpack Compose, Material 3.

```
app/src/main/java/com/sparkgym/
├── core/design/      Theme, colours, the System panel/button/bar components
├── core/util/        Date and formatting helpers
├── data/
│   ├── local/        Room entities, DAOs, converters, database
│   ├── seed/         Bundled exercises, routines and foods
│   ├── remote/       Fitbit OAuth + API, Health Connect, Open Food Facts
│   ├── prefs/        DataStore user profile
│   └── repository/   Workout, nutrition, game, wearable, seed
├── domain/
│   ├── model/        Muscles, hunter, quests
│   ├── engine/       XP, attributes, quests, heat map, coach, progression,
│   │                 meal planning, strength and energy maths
│   └── SystemCoordinator.kt   Where logging, eating and syncing all meet
├── di/               Hand-rolled AppContainer (no annotation processor)
└── ui/               One package per screen, each with its own ViewModel
```

**Dependency injection is hand-rolled** on purpose. A single-module app with six
repositories does not need an annotation processor, and skipping one keeps builds
fast and stack traces readable. `AppContainer` holds lazy singletons;
`sparkViewModelFactory` wires the ViewModels in a dozen lines.

**All game, coaching and nutrition maths lives in `domain/engine`** as pure
functions with no Android dependencies, which is why it is all unit-tested — 141
tests covering the XP curve, quest scaling, heat map normalisation, progression,
deload logic, coach rules, meal-plan scaling, library search ranking, and the
integrity of the bundled content itself (every routine references a real
exercise, every meal plan references a real food, every muscle has at least ten
exercises and a stretch to go with it, every food's macros reconcile with its
calorie figure, and every muscle, group and meal is named in both languages).

Room is the single source of truth; every screen observes `Flow`s, so a set logged
in the workout screen updates the heat map, the quest board and the status window
without any of them knowing about each other.

---

## Community

Contributions are welcome, and the content is the easiest place to start — you
do not need to know Compose to add an exercise or fix a translation.

**Good first contributions**

| What | Where | Notes |
|---|---|---|
| Add an exercise | `data/seed/ExerciseSeed*.kt` | Needs prime movers *and* synergists — the heat map is only as honest as that mapping |
| Add a food | `data/seed/FoodSeed*.kt` | Macros per 100 g; a test checks they reconcile with the calorie figure |
| Add a meal plan | `data/seed/MealPlanSeed.kt` | Food slugs plus grams, and the engine scales it to each user |
| Add a stretch | `data/seed/StretchSeed.kt` | One per muscle, both languages |
| Improve a translation | `core/util/Strings.kt` | Every user-visible string lives in one table |
| Add a programme | `data/seed/RoutineSeedPro.kt` | Write down the coaching intent, not just the sets |

**Before you open a pull request**

```bash
./gradlew test          # the content tests will catch most data mistakes
./gradlew assembleDebug
```

The test suite deliberately guards the content, not just the code: every routine
has to reference a real exercise, every meal plan a real food, every muscle needs
at least ten exercises and a stretch, and every food's macros have to reconcile
with its stated calories. If you add content and a test goes red, the test is
usually right.

**Reporting something**

Open an issue with your device, Android version, and what you expected. For
anything involving the coach or the heat map, the numbers it showed you are more
useful than a screenshot of the screen.

**House style**

Comments explain *why*, never *what*. British spelling in prose. Coaching text
should sound like a coach talking, not a manual — the existing entries are the
reference.

---

## Privacy

Health data stays on the device. The only network calls the app makes are:

- **Fitbit** — only if you link an account, and only to read your own data.
- **Open Food Facts** — only when you tap "Search online" or scan a barcode, and
  only the search term or barcode is sent.

Nothing else leaves the phone. There is no analytics SDK, no crash reporter and no
account system. OAuth tokens are stored encrypted and excluded from backups.

---

## Licence and attribution

Released under the [MIT Licence](LICENSE) — use it, fork it, ship it, sell it,
just keep the copyright notice.

The bundled content is part of that: the 239 exercises, 14 programmes, 192 foods,
8 meal plans and 19 stretches are original write-ups, not scraped from another
app or database, so they carry the same licence as the code.

The Solo Leveling flavour (ranks, the status window, the daily quest and its
penalty) is a homage to the series by Chugong. This is an independent project and
is not affiliated with, endorsed by, or connected to Solo Leveling, Fitbit,
MyFitnessPal, or any of the apps listed at the top.
