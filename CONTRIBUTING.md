# Contributing to Spark Gym

The content is the app. Most of what makes Spark Gym useful — the exercises,
the programmes, the food database, the coaching text — is plain Kotlin data, so
you can improve it meaningfully without touching a single Composable.

## Setting up

Requirements: **JDK 17** and **Android Studio Ladybug or newer** (or the Android
SDK with `compileSdk 35`).

```bash
git clone <this repo>
cd spark-gym
./gradlew test            # fast, no device needed
./gradlew assembleDebug
```

No Fitbit credentials are needed to build or run — that path degrades to
"not configured" and Health Connect keeps working.

## Where things live

```
data/seed/      the content: exercises, routines, foods, meal plans, stretches
domain/engine/  the maths: XP, quests, heat map, coach, progression, nutrition
domain/model/   enums and value types shared by everything
ui/             one package per screen, each with its own ViewModel
core/util/      Strings.kt is the entire bilingual table
```

Anything in `domain/` is pure Kotlin with no Android dependencies, which is why
it is all unit-tested. Keep it that way — if a change to an engine needs an
Android import, the logic probably belongs in a repository instead.

## Adding an exercise

```kotlin
SeedExercise(
    "cable-y-raise", "Cable Y-Raise", Equipment.CABLE, Force.PULL,
    Diff.INTERMEDIATE, Track.WEIGHT_REPS,
    listOf(Muscle.TRAPS),                          // prime movers
    listOf(Muscle.REAR_DELTS, Muscle.SIDE_DELTS),  // synergists
    "Cables crossed low, arms sweeping up into a Y. Lower traps do the work — keep the shoulders down."
)
```

Two things matter more than the rest:

- **Get the muscles right.** The heat map, the coach's weak-point audit and the
  Vitality attribute all read this mapping. An exercise that claims chest alone
  leaves triceps permanently cold on somebody's map.
- **Write the cue like a coach.** Say what the movement is *for* and the one
  thing people get wrong. "Keep your back straight" is not worth the line.

Slugs are permanent identifiers — they key the seeding, so never rename one.

## Adding a food

Macros are **per 100 g**, always. `CoachTest` checks that protein, carbohydrate,
fat and fibre reconcile with the stated calories using Atwater factors, so a
typo fails the build rather than quietly skewing somebody's cut.

## Translating

Every user-visible string is in `core/util/Strings.kt` as a `pick(en, id)` pair.
Adding a language means adding an entry to `AppLanguage` and a column to `pick`.

Indonesian should read like a person, not a dictionary — `Otot utama`, not
`Otot primer`.

## Tests

```bash
./gradlew test
```

The suite guards the content as well as the code:

- every routine references a real exercise
- every meal plan references a real food
- every muscle has at least ten exercises, and a stretch
- no exercise lists a muscle as both primary and secondary
- every food's macros reconcile with its calories
- every achievement has badge art

If you add content and a test goes red, the test is usually right.

## Style

- Comments explain **why**, never what. Delete a comment that restates the code.
- British spelling in prose and comments.
- No new dependency without a reason that survives being said out loud.
