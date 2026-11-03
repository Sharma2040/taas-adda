package com.taasadda.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.webkit.WebViewAssetLoader;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Taas Adda: a full-screen WebView that runs the card games from assets/www,
 * plus a small JavaScript bridge ("TaasNative") for things a web page can't do:
 * nearby play over Bluetooth / Wi-Fi, GPS location, SMS, phone dialer and vibration.
 */
public class MainActivity extends Activity {

    static final String START_URL = "https://appassets.androidplatform.net/assets/www/index.html";
    static final int REQ_PERMS = 41;
    static final int ROOM = Color.rgb(13, 10, 8);

    WebView web;
    NearbyHub nearby;
    private Runnable afterGrant;
    private Runnable afterDeny;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(ROOM);
        web = new WebView(this);
        web.setBackgroundColor(ROOM);
        root.addView(web, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(root);

        // Keep the game clear of the status bar, navigation bar and camera cut-out.
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets i = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                v.setPadding(i.left, i.top, i.right, i.bottom);
            } else {
                v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets;
        });

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if ("appassets.androidplatform.net".equals(u.getHost())) return false;
                openExternal(u);
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());

        nearby = new NearbyHub(this);
        web.addJavascriptInterface(new Bridge(), "TaasNative");
        web.loadUrl(START_URL);
    }

    @Override
    protected void onDestroy() {
        if (nearby != null) nearby.stopAll();
        super.onDestroy();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        // Let the game close dialogs first; exit only when it says so.
        web.evaluateJavascript("(window.__back&&window.__back())?'1':'0'", value -> {
            if (!"\"1\"".equals(value)) finish();
        });
    }

    // ---------------------------------------------------------------- helpers

    void emit(JSONObject o) {
        final String js = "window.__native&&window.__native(" + o.toString() + ")";
        runOnUiThread(() -> web.evaluateJavascript(js, null));
    }

    void emitSimple(String type, String key, String value) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            if (key != null) o.put(key, value);
            emit(o);
        } catch (Exception ignored) {
        }
    }

    void openExternal(Uri u) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, u);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        } catch (Exception ignored) {
        }
    }

    void withPermissions(String[] perms, Runnable granted, Runnable denied) {
        List<String> need = new ArrayList<>();
        for (String p : perms) {
            if (checkSelfPermission(p) != PackageManager.PERMISSION_GRANTED) need.add(p);
        }
        if (need.isEmpty()) {
            granted.run();
            return;
        }
        afterGrant = granted;
        afterDeny = denied;
        requestPermissions(need.toArray(new String[0]), REQ_PERMS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_PERMS) return;
        boolean all = grantResults.length > 0;
        for (int r : grantResults) if (r != PackageManager.PERMISSION_GRANTED) all = false;
        Runnable ok = afterGrant, no = afterDeny;
        afterGrant = null;
        afterDeny = null;
        if (all && ok != null) ok.run();
        else if (!all && no != null) no.run();
    }

    String[] nearbyPermissions() {
        List<String> l = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 31) {
            l.add(Manifest.permission.BLUETOOTH_SCAN);
            l.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            l.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (Build.VERSION.SDK_INT >= 33) {
            l.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        } else {
            l.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        return l.toArray(new String[0]);
    }

    String[] locationPermissions() {
        return new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
    }

    @SuppressLint("MissingPermission")
    void fetchLocation() {
        LocationServices.getFusedLocationProviderClient(this)
                .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(loc -> {
                    try {
                        JSONObject o = new JSONObject();
                        o.put("type", "location");
                        if (loc != null) {
                            o.put("lat", loc.getLatitude());
                            o.put("lng", loc.getLongitude());
                            o.put("acc", (double) loc.getAccuracy());
                        }
                        emit(o);
                    } catch (Exception ignored) {
                    }
                })
                .addOnFailureListener(e -> emitSimple("location", "error", String.valueOf(e.getMessage())));
    }

    // ---------------------------------------------------------------- bridge

    /** Methods callable from the page as window.TaasNative.xxx(). */
    class Bridge {
        @JavascriptInterface
        public void startHost(final String name) {
            runOnUiThread(() -> withPermissions(nearbyPermissions(),
                    () -> nearby.host(name),
                    () -> emitSimple("error", "msg", "permission denied")));
        }

        @JavascriptInterface
        public void startDiscovery(final String name) {
            runOnUiThread(() -> withPermissions(nearbyPermissions(),
                    () -> nearby.discover(),
                    () -> emitSimple("error", "msg", "permission denied")));
        }

        @JavascriptInterface
        public void stopDiscovery() {
            runOnUiThread(() -> nearby.stopDiscovery());
        }

        @JavascriptInterface
        public void connect(final String endpointId, final String name) {
            runOnUiThread(() -> nearby.connect(endpointId, name));
        }

        @JavascriptInterface
        public void send(final String endpointId, final String data) {
            runOnUiThread(() -> nearby.send(endpointId, data));
        }

        @JavascriptInterface
        public void disconnect(final String endpointId) {
            runOnUiThread(() -> nearby.disconnect(endpointId));
        }

        @JavascriptInterface
        public void stopAll() {
            runOnUiThread(() -> nearby.stopAll());
        }

        @JavascriptInterface
        public void getLocation() {
            runOnUiThread(() -> withPermissions(locationPermissions(),
                    MainActivity.this::fetchLocation,
                    () -> emitSimple("location", "error", "permission denied")));
        }

        /** Opens the phone app with the number filled in. The person presses call. */
        @JavascriptInterface
        public void dial(final String number) {
            runOnUiThread(() -> {
                try {
                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                    startActivity(i);
                } catch (Exception ignored) {
                }
            });
        }

        /** Opens the messaging app with recipients and text filled in. The person presses send. */
        @JavascriptInterface
        public void sms(final String numbers, final String body) {
            runOnUiThread(() -> {
                try {
                    String sep = "samsung".equalsIgnoreCase(Build.MANUFACTURER) ? "," : ";";
                    Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + numbers.replace(",", sep)));
                    i.putExtra("sms_body", body);
                    startActivity(i);
                } catch (Exception e) {
                    share(body);
                }
            });
        }

        @JavascriptInterface
        public void share(final String text) {
            runOnUiThread(() -> {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, text);
                startActivity(Intent.createChooser(i, "Send SOS"));
            });
        }

        @JavascriptInterface
        public void openUrl(final String url) {
            runOnUiThread(() -> openExternal(Uri.parse(url)));
        }

        @JavascriptInterface
        public void vibrate(final int ms) {
            Vibrator v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            if (v == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                v.vibrate(VibrationEffect.createWaveform(new long[]{0, 500, 200, 500, 200, Math.max(200, ms - 1400)}, -1));
            } else {
                v.vibrate(ms);
            }
        }
    }
}
