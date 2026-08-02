# Spark Gym — Privacy Policy

_Last updated: 2 August 2026_

Spark Gym is a training, nutrition and body-composition tracker for Android.
This policy describes what it does with your information. It is short because
the app does very little with it.

## The short version

**Everything you log stays on your phone.** There is no Spark Gym account, no
Spark Gym server, and no analytics or advertising SDK in the app. Nobody —
including the developer — can see your workouts, your weight, your photos or
what you ate.

## What the app stores, and where

All of the following is written to the app's private storage on your device and
nowhere else:

| Data | Why |
|---|---|
| Workouts, sets, weights, reps, personal records | The training log and progress charts |
| Bodyweight, body-fat and waist measurements | Progress tracking and calorie targets |
| Progress photos | Visual comparison over time |
| Food and water entries | The nutrition diary and macro budget |
| Your name, age, height, sex, activity level, goal | Calculating calorie and protein targets |
| Profile photo | Shown in the app |
| Levels, XP, quests, achievements | The gamification features |
| Steps, heart rate, sleep and calories read from Health Connect or Fitbit | Daily summaries and attribute scores |

Private app storage is not readable by other apps. Uninstalling Spark Gym
deletes all of it.

## Health and fitness data

If you grant access, Spark Gym reads steps, heart rate, sleep, calories, exercise
sessions and weight from **Health Connect**, and can write completed workouts and
active calories back so the rest of your phone stays in step.

- Access is optional. The app works fully without it.
- Health data is read into the app's own local database and used only to show
  you your own summaries.
- **Health data is never transmitted anywhere, never sold, and never used for
  advertising.**
- You can revoke access at any time in Health Connect, and delete the app's copy
  by clearing its data or uninstalling.

## When the app uses the network

Spark Gym is offline-first. It reaches the internet in exactly three situations,
all of which you initiate:

1. **Barcode and online food search** — the barcode or search term you typed is
   sent to [Open Food Facts](https://world.openfoodfacts.org), an open food
   database, to look up nutrition facts. No identifier of you is attached.
2. **Linking a Fitbit account** — if you choose this instead of Health Connect,
   the app talks to Fitbit's API to fetch your own activity data. The connection
   uses OAuth 2.0 with PKCE; access tokens are stored encrypted on your device
   and are never sent anywhere except Fitbit.
3. **Opening a link** — tapping a link in the licences screen hands it to your
   browser.

Nothing else leaves the phone. There is no telemetry, no crash reporting and no
usage tracking.

## Sharing a workout

The "share" button renders a summary image into the app's own cache and passes it
to whichever app you pick from the Android share sheet. Where it goes after that
is governed by the app you chose. Nothing is uploaded by Spark Gym.

## Permissions

| Permission | Why |
|---|---|
| Internet, network state | Food lookup and the optional Fitbit link |
| Health Connect read permissions | Steps, heart rate, sleep, calories, exercise, weight |
| Health Connect write permissions | Writing finished workouts and active calories back |

The app requests nothing else. It does not ask for contacts, location,
microphone, camera or precise identifiers.

## Children

Spark Gym is not directed at children under 13 and collects nothing from anyone.

## Your control

- **See your data** — it is all in the app.
- **Delete your data** — delete individual entries in the app, or clear the app's
  storage, or uninstall. There is no copy anywhere else to request deletion of.
- **Revoke health access** — Health Connect settings, or unlink Fitbit in the app,
  which also deletes the stored tokens.

## Third-party services

- **Open Food Facts** — receives only the barcode or search text you enter.
  <https://world.openfoodfacts.org/privacy>
- **Fitbit (Google)** — only if you link an account.
  <https://www.fitbit.com/global/us/legal/privacy-policy>

Spark Gym has no other third-party dependencies that transmit data.

## Changes

If this policy changes, the new version will be published here and the date at
the top updated.

## Contact

Questions about this policy: open an issue at
<https://github.com/slaks37/spark-gym/issues>.
