# Taas Adda for Android

Eight South Asian card games (Call Break, Kachuful, Teen Patti, Flash, Kitti, Marriage, Rummy and Dhumbal) on a 3D table.

- **Offline:** play against computer players, no internet needed.
- **Play with friends nearby:** phones connect over Bluetooth and Wi‑Fi (Google Nearby Connections). No internet or SIM needed. Up to 4 players; bots fill empty seats. Call Break is available now.
- **Table chat and emoji reactions** while playing nearby.
- **English, Nepali and Hindi.** Change it in Settings → Language.
- **Progress:** levels, coins, a daily bonus, three daily challenges, achievements, and a shop with new tables and card backs. All stored on the phone.
- **Learn mode:** a short guide the first time you open each game, and a 💡 hint button that suggests a move.
- **SOS:** alert everyone at your table, text your emergency contacts with your GPS location, and one‑tap dialing of local emergency numbers (Nepal, India, Korea, USA/Canada, Europe).

> SOS is a way to alert friends. It does not contact the police or an ambulance for you. If you are in danger, call emergency services.

Version 0.5.0 · package `io.github.sharma2040.taasadda` · Android 7.0 (API 24) and newer.

---

## Build the APK without installing anything (GitHub, free)

1. Create a free account at github.com and sign in on a computer.
2. Click **New repository**, name it `taas-adda`, choose **Private**, and create it.
3. On the new repository page, click **uploading an existing file**. Unzip this project on your computer and drag **everything inside the `taas-adda-android` folder** into the page, including the hidden `.github` folder.
   - If your computer hides the `.github` folder, open the **Actions** tab, click **set up a workflow yourself**, and paste the contents of `.github/workflows/build-apk.yml`.
4. Click **Commit changes**. The build starts on its own.
5. Open the **Actions** tab and wait for the green tick (about 5–8 minutes).
6. Open the finished run and download **TaasAdda-apk** under *Artifacts*. Unzip it to get `app-debug.apk`.

## Install on a phone

1. Send `app-debug.apk` to the phone (WhatsApp, Viber, Google Drive, USB…).
2. Tap the file. Android asks to allow installs from that app: allow it.
3. Open **Taas Adda**. Give every friend the same APK.

The first time you host or join a nearby table, Android asks for **Nearby devices** (or **Location** on older phones). Allow it, or phones can't find each other. SOS asks for **Location** the first time you share it.

## Build with Android Studio instead

1. Install Android Studio (free).
2. **File → Open** and choose the `taas-adda-android` folder. Let Gradle sync.
3. Connect your phone with USB debugging on, then press **Run**, or use **Build → Build APK(s)**.

## Playing with friends nearby

1. Everyone opens Taas Adda with Bluetooth on (Wi‑Fi on helps speed), phones within a few metres.
2. One person taps **Play with friends nearby → Host a table**.
3. The others tap **Play with friends nearby → Join a table** and pick the host's table.
4. The host taps **Start game**. Empty seats are played by bots.
5. Use the chat button at the top for messages and emoji.

## Publishing on Google Play

See **PUBLISHING.md** for the full step-by-step guide.

### Summary

You'll need a Google Play developer account (one‑time US$25), a signed **release** build (Android Studio: **Build → Generate Signed Bundle**), a privacy policy (the app uses location only for SOS and nearby play), and store screenshots. Bump `versionCode` in `app/build.gradle` for every upload.

## Project layout

```
app/src/main/assets/www/index.html   the whole game (HTML, CSS, JS)
app/src/main/java/.../MainActivity   full-screen WebView + JavaScript bridge (TaasNative)
app/src/main/java/.../NearbyHub      Bluetooth / Wi‑Fi table connections
.github/workflows/build-apk.yml      cloud build
```

The page talks to Android through `window.TaasNative`: `startHost`, `startDiscovery`, `connect`, `send`, `disconnect`, `stopAll`, `getLocation`, `dial`, `sms`, `share`, `openUrl`, `vibrate`. Android sends events back through `window.__native({type, ...})`.

## Testing nearby play in a desktop browser

Serve the `www` folder and open `index.html?mock` in two tabs of the same browser. The tabs talk to each other as if they were two phones.
