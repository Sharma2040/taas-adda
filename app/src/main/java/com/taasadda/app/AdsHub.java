package com.taasadda.app;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

import org.json.JSONObject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Google AdMob for Taas Adda.
 * - Asks for consent first where the law requires it (Google's User Messaging Platform).
 * - Ads are capped at Teen content, and never shown during a hand: the page decides when.
 * - Rewarded ads are always the player's choice.
 */
class AdsHub {
    private final MainActivity act;
    private final ConsentInformation consent;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private RewardedAd rewarded;
    private InterstitialAd interstitial;
    private boolean loadingRewarded, loadingInterstitial;

    AdsHub(MainActivity activity) {
        act = activity;
        consent = UserMessagingPlatform.getConsentInformation(activity);
    }

    /** Called once from onCreate: consent first, then the ads SDK. */
    void start() {
        ConsentRequestParameters params = new ConsentRequestParameters.Builder().build();
        consent.requestConsentInfoUpdate(act, params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(act, formError -> {
                    if (consent.canRequestAds()) initAds();
                    report();
                }),
                requestError -> {
                    if (consent.canRequestAds()) initAds();
                    report();
                });
        if (consent.canRequestAds()) initAds();
    }

    private void initAds() {
        if (started.getAndSet(true)) return;
        RequestConfiguration cfg = new RequestConfiguration.Builder()
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_T)
                .build();
        MobileAds.setRequestConfiguration(cfg);
        new Thread(() -> MobileAds.initialize(act, status -> act.runOnUiThread(() -> {
            loadRewarded();
            loadInterstitial();
        }))).start();
    }

    private void loadRewarded() {
        if (rewarded != null || loadingRewarded || !started.get()) return;
        loadingRewarded = true;
        RewardedAd.load(act, BuildConfig.ADMOB_REWARDED_ID, new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewarded = ad;
                loadingRewarded = false;
                report();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                rewarded = null;
                loadingRewarded = false;
                report();
            }
        });
    }

    private void loadInterstitial() {
        if (interstitial != null || loadingInterstitial || !started.get()) return;
        loadingInterstitial = true;
        InterstitialAd.load(act, BuildConfig.ADMOB_INTERSTITIAL_ID, new AdRequest.Builder().build(), new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitial = ad;
                loadingInterstitial = false;
                report();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError error) {
                interstitial = null;
                loadingInterstitial = false;
                report();
            }
        });
    }

    void showRewarded(String placement) {
        RewardedAd ad = rewarded;
        if (ad == null) {
            emit("rewarded", placement, false, "not_ready");
            loadRewarded();
            return;
        }
        rewarded = null;
        final boolean[] earned = {false};
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                emit("rewarded", placement, earned[0], "closed");
                loadRewarded();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                emit("rewarded", placement, false, "failed");
                loadRewarded();
            }
        });
        ad.show(act, reward -> earned[0] = true);
    }

    void showInterstitial() {
        InterstitialAd ad = interstitial;
        if (ad == null) {
            loadInterstitial();
            return;
        }
        interstitial = null;
        ad.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                emit("interstitial", "", false, "closed");
                loadInterstitial();
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError error) {
                loadInterstitial();
            }
        });
        ad.show(act);
    }

    void showPrivacyOptions() {
        UserMessagingPlatform.showPrivacyOptionsForm(act, formError -> report());
    }

    /** Tells the page which ads are ready and whether a privacy choices button is needed. */
    void report() {
        try {
            JSONObject o = new JSONObject();
            o.put("type", "ads");
            o.put("rewarded", rewarded != null);
            o.put("interstitial", interstitial != null);
            o.put("privacy", consent.getPrivacyOptionsRequirementStatus()
                    == ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED);
            act.emit(o);
        } catch (Exception ignored) {
        }
    }

    private void emit(String format, String placement, boolean earned, String event) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", "ad");
            o.put("format", format);
            o.put("placement", placement);
            o.put("earned", earned);
            o.put("event", event);
            act.emit(o);
        } catch (Exception ignored) {
        }
        report();
    }
}
