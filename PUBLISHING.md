# Publishing Taas Adda on Google Play

Everything the Play Console asks for is already in this repository. This guide walks through the order to do things in. Plan on **about 3 weeks**, mostly waiting: new personal accounts must run a 14-day closed test before Google allows a public release.

| What | Where |
|---|---|
| Store text, rating and data-safety answers | `store/listing.md` |
| App icon 512 × 512 | `store/icon-512.png` |
| Feature graphic 1024 × 500 | `store/feature-graphic-1024x500.png` |
| Phone screenshots (1080 × 2160) | `store/screenshot-*.png` |
| Privacy policy page | `docs/privacy-policy.html` (published in step 2) |
| App bundle for upload (`.aab`) | built automatically once step 3 is done |

---

## 1. Create a Google Play developer account (day 1)

1. Go to **play.google.com/console** and sign in with the Google account you want to own the app.
2. Choose **Yourself** (personal account). Pay the one-time **US$25** fee.
3. Complete **identity verification** with a government ID. Google also verifies a phone number and email, and asks whether you have used Android devices; answer honestly.
4. Verification can take a few days. You can do steps 2–3 below meanwhile.

> Google is rolling out developer verification for all apps on certified Android phones: some countries from September 2026, everywhere in 2027. Your verified Play account covers apps installed from Play. Before 2027, the APK you share directly should also be signed with your registered key; that is a small change to the build when the time comes.

## 2. Publish the privacy policy page (5 minutes)

1. In this GitHub repository: **Settings → Pages**.
2. Under *Build and deployment*, Source: **Deploy from a branch**; Branch: **main**, folder **/docs**. Save.
3. After a minute or two the policy is live at
   **https://sharma2040.github.io/taas-adda/privacy-policy.html**. Open it once to check.

## 3. Turn on signed builds for Play (5 minutes)

Play needs an **App Bundle (.aab)** signed with your private upload key. The key was created for you in `taas-adda-upload-key.zip` (sent in the chat, **not** in this repository).

1. Back up that zip somewhere safe (Google Drive plus a USB stick). Never commit it here; this repository is public.
2. In this repository: **Settings → Secrets and variables → Actions → New repository secret**. Add two secrets:
   - `TAAS_KEYSTORE_BASE64` = the whole contents of `TAAS_KEYSTORE_BASE64.txt`
   - `TAAS_KEYSTORE_PASSWORD` = the contents of `TAAS_KEYSTORE_PASSWORD.txt`
3. Open **Actions → Build Taas Adda → Run workflow**. When it finishes, download **TaasAdda-play-bundle** from the run's *Artifacts*. Inside is `TaasAdda-play.aab`.

Each build number becomes the app's version code automatically, so every new build can be uploaded.

## 4. Create the app in Play Console

1. **Create app** → name **Taas Adda: Nepali Card Games**, language English, **Game**, **Free**. Accept the declarations.
2. Work through **Set up your app** on the dashboard. Use `store/listing.md` for every answer:
   - **Privacy policy**: the URL from step 2.
   - **App access**: all features are available without login.
   - **Ads**: no ads.
   - **Content rating**: fill in the questionnaire. Say **yes to simulated gambling** (pretend chips in Teen Patti, Flash and Kitti), yes to users interacting (nearby chat), yes to sharing location (SOS, only when the user chooses), no real-money gambling, no purchases.
   - **Target audience**: 13 and over. Don't select *Designed for Families*.
   - **Data safety**: see the suggested answers in `store/listing.md`.
   - **Government app / financial features / health**: no.
3. **Main store listing**: paste the name, short and full descriptions, upload the icon, feature graphic and the six screenshots. Category **Card**.

## 5. Closed test with 12 testers for 14 days (required)

Google requires new personal accounts to run a closed test with at least 12 testers who stay opted in for 14 days in a row before you can apply for production.

1. **Testing → Closed testing → Create track**. Upload `TaasAdda-play.aab`. When asked, **use Play App Signing** (recommended).
2. Add testers by email (a Google Group is easiest). You need **at least 12**, and they must stay opted in for **14 continuous days**. Someone who leaves and rejoins restarts their own 14 days, so recruit 15–20 to be safe.
3. Send testers the **opt-in link** from the track page. Each must open it, tap *Become a tester*, then install from Play.
4. Ask them to actually play, including nearby play, and send feedback. Upload new builds to the same track as fixes land.

## 6. Apply for production and release

1. After 14 days, the dashboard shows **Apply for production**. Answer the questions about your test (what testers found, what you changed).
2. Once approved (usually a few days), create a **Production** release with the latest `.aab`, choose countries (for example Nepal, India, South Korea and others), and submit for review.
3. Review takes from a few hours to about a week. After that, the app is public.

## Keep it accepted

- **Target Android version**: Google Play requires new apps and updates to target Android 16 (API 36). This app already does.
- **Every update**: just push to `main`. GitHub builds a new APK (release page) and a new `.aab` (artifact) with a higher version code. Upload the `.aab` in Play Console.
- **Location and Bluetooth**: the app explains why it asks, and only uses location in the foreground. Don't add background location or SMS permissions; those need special Play approval.
