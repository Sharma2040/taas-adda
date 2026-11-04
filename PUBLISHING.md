# Launch plan: Google Play + AdMob

This is the full order of work to publish Taas Adda on Google Play and earn from ads. Only you can create the accounts, because they need your Google account, your government ID, a bank card and your bank details. Everything else (code, store text, images, privacy page, ads in the app) is prepared here or done by Claude once you share the IDs.

**Plan on about 4 weeks.** Most of it is waiting: Google requires new personal accounts to run a 14-day closed test, and account checks take a few days each.

| What | Where |
|---|---|
| Store text, rating and data-safety answers | `store/listing.md` |
| App icon 512 × 512 | `store/icon-512.png` |
| Feature graphic 1024 × 500 | `store/feature-graphic-1024x500.png` |
| Phone screenshots (1080 × 2160) | `store/screenshot-*.png` |
| Privacy policy page | `docs/privacy-policy.html` |
| App bundle for Play (`.aab`) | built automatically once step 4 is done |

---

## 0. Decide one thing first: your country

Both Google Play and AdMob ask for the country where you live, and **AdMob does not let you change it later**. Use the country where you legally live **and** have a bank account in your own name, with the same name as on your ID.

- **Living in South Korea (or India):** use that country. It supports both ad payouts and selling in-app items such as "Remove ads".
- **Living in Nepal:** Google Play does **not** support merchant accounts in Nepal, so you could not sell "Remove ads" or cosmetic packs there. Ads can still earn. Check that your bank can receive international wire transfers.

Earnings are taxable income where you live. Ask a local tax adviser once money starts coming in.

## 1. Google Play developer account (week 1, day 1)

1. On a computer, go to **play.google.com/console** and sign in with the Google account that should own the app forever. A Gmail you use personally is fine. Use the same account for AdMob in step 3.
2. Choose **Yourself** (personal account).
3. Pay the one-time **US$25** with a Visa, Mastercard or similar **credit or debit card** in your own name. Prepaid cards are not accepted.
4. Fill in your **legal name and address** exactly as on your government ID. If Google judges the information invalid, the fee is not refunded.
5. **Verify your identity**: upload your ID (passport or national ID) when asked. Google also verifies your contact email and phone.
6. Install the **Google Play Console** app on your Android phone and sign in with the same account. Personal accounts must **verify access to an Android device** this way before publishing.
7. Approval usually takes a few days. Do steps 2 and 3 meanwhile.

> Google is rolling out developer verification for apps on certified Android phones: some countries from September 2026, everywhere in 2027. Your verified Play account covers this.

## 2. Developer website and privacy page (15 minutes)

Google Play needs a privacy-policy link, and AdMob needs a file called `app-ads.txt` at the **root** of your developer website. A free GitHub site does both.

1. On GitHub, create a **new public repository** named exactly **`Sharma2040.github.io`**.
2. Tell Claude when it exists. Claude will add a small home page, the privacy policy and, after step 3, the `app-ads.txt` file.
3. In that repository: **Settings → Pages → Source: Deploy from a branch → main / (root)**. Save.
4. Your site is then **https://sharma2040.github.io**, with the privacy policy at **https://sharma2040.github.io/privacy-policy.html**.

## 3. AdMob account (week 1)

1. Go to **admob.google.com**, choose **Get started**, and sign in with the **same Google account** as Play Console.
2. Select your **country** (from step 0). It can't be changed later.
3. Choose your **time zone** and **currency**, accept the terms, and choose **Start using AdMob**.
4. Verify your phone number with the 6-digit code.
5. Open **Payments** and enter your **payment details**: your legal name, **Individual**, and your full postal address. New accounts aren't approved to show ads until this is done. Verification usually takes up to 24 hours, rarely up to 2 weeks.
6. Add the app: **Apps → Add app → Android → "Is the app listed on a supported app store?" → No**. Name it **Taas Adda**.
7. Create two ad units for the app:
   - **Rewarded**, name `Bonus coins`
   - **Interstitial**, name `Between games`
8. **Send Claude** these, as they appear in AdMob. They are not secret; they ship inside the app.
   - App ID: `ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY`
   - Rewarded ad unit ID: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`
   - Interstitial ad unit ID: `ca-app-pub-XXXXXXXXXXXXXXXX/ZZZZZZZZZZ`
   - Publisher ID: `pub-XXXXXXXXXXXXXXXX` (for `app-ads.txt`)

Claude then adds the ads to the app: optional rewarded ads for bonus coins and chips, and short ads only between full games, never during a hand. It also adds the privacy consent message that Google requires for players in Europe and the UK.

**Rules that get AdMob accounts closed. Never break them:**
- Never tap your own ads, and never ask friends or testers to tap them. Use test devices (Claude sets this up).
- Never offer rewards for tapping ads, and don't place ads where they can be tapped by accident.
- Don't show ads to children. The app is rated 13+ and isn't in *Designed for Families*.

**Getting paid:** at **US$10** you may be asked to verify your identity. AdMob then posts a **PIN letter** to your address (2–3 weeks) that you enter in AdMob. Choose your payment method (bank transfer). Payments are sent around the **21st** of each month once your balance passes **US$100**. Smaller amounts roll over.

The app can't pass AdMob's **app review** until it is published on Google Play (step 8). Until then, ads only show to test devices.

## 4. Turn on signed builds for Play (5 minutes)

Play needs an **App Bundle (.aab)** signed with your private upload key, from `taas-adda-upload-key.zip` (sent in the chat, **not** in this repository).

1. Back up that zip in two safe places (Google Drive and a USB stick). Never upload it to GitHub; this repository is public.
2. In **this** repository: **Settings → Secrets and variables → Actions → New repository secret**. Add:
   - `TAAS_KEYSTORE_BASE64` = the whole contents of `TAAS_KEYSTORE_BASE64.txt`
   - `TAAS_KEYSTORE_PASSWORD` = the contents of `TAAS_KEYSTORE_PASSWORD.txt`
3. Open **Actions → Build Taas Adda → Run workflow**. When it finishes, download **TaasAdda-play-bundle** from the run's *Artifacts*. Inside is `TaasAdda-play.aab`.

## 5. Create the app in Play Console (week 1–2)

1. **Create app** → name **Taas Adda: Nepali Card Games**, default language English, **Game**, **Free**. Accept the declarations.
2. Work through **Set up your app** on the dashboard, using `store/listing.md`:
   - **Privacy policy**: `https://sharma2040.github.io/privacy-policy.html`
   - **App access**: all features available without login.
   - **Ads**: **Yes, my app contains ads** (once AdMob is added; answer No if you upload a build without ads first, and change it later).
   - **Content rating**: answer honestly. **Yes** to simulated gambling (pretend chips in Teen Patti, Flash, Kitti), **yes** to users interacting (nearby chat), **yes** to sharing location (SOS, only when the user chooses), **no** real-money gambling. Expect a Teen / 12+ rating.
   - **Target audience**: 13 and over. Do **not** opt into *Designed for Families*.
   - **Data safety**: use the suggested answers in `store/listing.md`. With AdMob added, also declare that the app shares **device or other IDs** with Google for **advertising** (Claude will update `store/listing.md` when ads are added).
   - **Government app, financial features, health**: No.
3. **Store settings → contact details**: your email, and website **`https://sharma2040.github.io`** (AdMob reads `app-ads.txt` from here).
4. **Main store listing**: name, short and full description, icon, feature graphic and the six screenshots from `store/`. Category **Card**. Add **Nepali** and **Hindi** translations of the listing too (*Manage translations*).
5. **Setup → Payments profile**: create a merchant profile if you want to sell "Remove ads" later (not available if your account is in Nepal; see step 0).

## 6. Closed test: 12+ testers for 14 days (weeks 2–4)

Google requires new personal accounts to run a closed test with **at least 12 testers who stay opted in for 14 days in a row** before you can apply for production.

1. Create a **Google Group** (groups.google.com), for example `taas-adda-testers`, and add 15–20 friends with Android phones and Gmail addresses. Extra testers cover anyone who drops out; someone who leaves and rejoins restarts their own 14 days.
2. **Testing → Closed testing → Create track**. Add the Google Group as testers. Upload `TaasAdda-play.aab`, and when asked, **use Play App Signing**.
3. Send testers the **opt-in link** from the track page. Each opens it, taps **Become a tester**, then installs Taas Adda **from the Play Store**, not from the APK file.
4. Ask them to really play: all games, nearby play, language switching, SOS. Collect feedback in a WhatsApp or Viber group. Claude fixes issues, and you upload new `.aab` builds to the same track.
5. Ask testers **not to tap ads**. Test ads are shown on test builds.

## 7. Apply for production (end of week 4)

1. After 14 full days, the dashboard shows **Apply for production**. Answer honestly: how many testers, what they tested, what feedback you got and what you changed. Specific answers help approval.
2. Approval usually takes a few days.

## 8. Release

1. **Production → Create release**, upload the latest `.aab`, pick countries (Nepal, India, South Korea, UAE, Qatar, Saudi Arabia, Malaysia, UK, USA, Australia and others with Nepali or Indian communities), and submit.
2. Review takes from a few hours to about a week.
3. Once the app is live, open AdMob → **Apps → Taas Adda → Link to app store**, find it on Google Play and confirm. AdMob then reviews the app (usually 2–3 days). After approval, real ads start earning.
4. Check that `app-ads.txt` shows as verified in AdMob (it can take 24 hours).

## Keep it accepted

- **Target Android version**: Google Play requires new apps and updates to target Android 16 (API 36). This app does.
- **Every update**: push to `main`. GitHub builds a new APK (release page) and a new `.aab` (artifact) with a higher version code. Upload the `.aab` in Play Console.
- **Permissions**: location is only used in the foreground and only for SOS and nearby play. Don't add background location or SMS-sending permissions; they need special approval.
- **No real money, ever**: coins are only earned by playing and can't be bought or cashed out. India bans real-money games and their advertising, and Play has strict gambling rules. Sell looks (themes, card backs) and "Remove ads", never chips or prizes.
