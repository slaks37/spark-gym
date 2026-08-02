# Getting Spark Gym onto Google Play

Everything in this file is a step only you can take — an account, a form, or a
decision. The code side is done.

---

## 1. What is already handled in the repo

- **App Bundle (.aab)** — Play does not accept APKs for new apps. The release
  workflow builds one.
- **Gradle signing** — reads the key from the environment; no key material in
  the repository.
- **In-app licences screen** — Profile → *Open source licences*. This is a legal
  obligation: the anatomical model is CC BY-SA 4.0 and Open Food Facts is ODbL,
  and both require credit visible to the user.
- **Privacy policy text** — `PRIVACY.md`. You still have to host it (below).
- **targetSdk 35**, minSdk 26.

---

## 2. Signing: upload key vs app-signing key

Use **Play App Signing** (the default). You upload a bundle signed with your
*upload key*; Google re-signs it with the *app signing key* it holds.

That matters because it is the one mistake you cannot undo: if you opt out and
lose your key, you can never update the app again. With Play App Signing, a lost
upload key can be reset by support.

Create the upload key once:

```bash
keytool -genkeypair -v -keystore spark-gym-upload.jks \
  -alias spark-gym -keyalg RSA -keysize 4096 -validity 10000
```

Then add the four repository secrets (Settings → Secrets and variables →
Actions): `KEYSTORE_BASE64` (`base64 -w0 spark-gym-upload.jks`),
`KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.

Run the **Build** workflow with *Also build a signed release APK + Play bundle*
ticked, then download the **spark-gym-play-bundle** artifact.

---

## 3. Host the privacy policy

Play will not publish a listing without a public URL, and this app touches health
data, so it will be checked.

Easiest route: enable GitHub Pages on this repo and point the listing at the
rendered `PRIVACY.md`. Any public URL works.

---

## 4. Health Connect needs a declaration — plan for this

This is the step most likely to delay you. Reading Health Connect data requires
a **Health Apps declaration form** in Play Console, and Google reviews it against
an approved use case. Expect questions about:

- which permissions you request and why *each* is needed;
- that health data is not used for advertising or sold (it is not — see
  `PRIVACY.md`);
- a demo video or account showing the feature working.

If approval stalls, the app still functions without Health Connect — the wearable
sync is optional. Shipping the first version with those permissions removed and
adding them in an update is a legitimate fallback.

---

## 5. Data safety form

Declare, matching `PRIVACY.md`:

- **Collected / sent off device:** none.
- **Stored on device only:** health and fitness, photos, personal info.
- **Encrypted in transit:** yes (HTTPS for food lookup and Fitbit).
- **Deletion:** users can delete data in-app or by uninstalling.
- **Third parties:** Open Food Facts receives only the barcode or search text;
  Fitbit only if the user links an account.

---

## 6. Store listing

Needed, and none of it exists yet:

- App icon 512×512 PNG
- Feature graphic 1024×500
- At least 2 phone screenshots (8 is better — Status, heat map, logger, coach,
  nutrition, quests, library, achievements)
- Short description (80 chars) and full description (4000)
- Content rating questionnaire
- Category: Health & Fitness

---

## 7. Two risks worth deciding on before you publish

**The Solo Leveling framing.** Hunter, Gate, Monarch, "the System", "AWAKENED",
E–S ranks and the daily-quest-with-penalty are recognisably from Solo Leveling.
Homage is not a defence against a trademark complaint, and Play removes apps on
rights-holder request. Renaming the vocabulary — the mechanics are yours and work
just as well under different names — removes the risk entirely. Your call, but
make it deliberately rather than by accident.

**The share-alike licences.** The muscle model (CC BY-SA 4.0) and Open Food Facts
data (ODbL) oblige derivatives to carry the same terms. That is fine for a free
app. If you ever want to sell it or close the source, replace the model with a
royalty-free one first.

---

## 8. Before you press publish

The app has never been run on a physical device by the author of these notes.
Install the debug APK and check, at minimum:

1. It launches and onboarding completes
2. The 3D heat map renders and stays responsive — it loads a 5.3 MB model in a
   WebView, and mid-range GPU performance is untested
3. Tapping a muscle opens the exercise panel with the right content
4. Logging a set, finishing a session, then XP / badges / heat map all update
5. Profile photo and progress photos stay separate
6. Switching to Indonesian across every tab
7. Health Connect grant and sync, if you are shipping with it
