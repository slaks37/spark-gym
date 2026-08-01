# Spark Gym

An Android training app that puts four things most people juggle across four apps into one:

| Inspiration | What it contributes here |
|---|---|
| Muscle Monster / home-gym planners | 14 bundled programmes, a 171-exercise library, home-friendly filters |
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

## Build it

Requirements: **JDK 17**, **Android Studio Ladybug or newer** (or just the Android SDK with
`compileSdk 35` installed).

```bash
git clone <this repo>
cd spark-gym
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/
./gradlew installDebug           # straight onto a connected device
./gradlew test                   # unit tests for the XP, quest, heat map and nutrition maths
```

Opening the folder in Android Studio and pressing Run works too — no extra setup
is needed for everything except the direct Fitbit link (below).

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
functions with no Android dependencies, which is why it is all unit-tested — 60+
tests covering the XP curve, quest scaling, heat map normalisation, progression,
deload logic, coach rules, meal-plan scaling, and the integrity of the bundled
content itself (every routine references a real exercise, every meal plan
references a real food, every muscle has at least three exercises, and every
food's macros reconcile with its calorie figure).

Room is the single source of truth; every screen observes `Flow`s, so a set logged
in the workout screen updates the heat map, the quest board and the status window
without any of them knowing about each other.

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

The Solo Leveling flavour (ranks, the status window, the daily quest and its
penalty) is a homage to the series by Chugong. This is an independent project and
is not affiliated with, endorsed by, or connected to Solo Leveling, Fitbit,
MyFitnessPal, or any of the apps listed at the top.
