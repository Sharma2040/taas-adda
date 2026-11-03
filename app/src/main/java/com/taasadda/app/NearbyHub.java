package com.taasadda.app;

import androidx.annotation.NonNull;

import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Phone-to-phone link using Google Nearby Connections.
 * It picks Bluetooth, BLE or Wi-Fi automatically and needs no internet.
 * One phone hosts (advertises), the others discover and join (star topology).
 * Every event is forwarded to the page as {type: ..., ...}.
 */
class NearbyHub {
    static final String SERVICE_ID = "com.taasadda.app.table";

    private final MainActivity act;
    private final ConnectionsClient client;
    private final Map<String, String> names = new HashMap<>();

    NearbyHub(MainActivity activity) {
        act = activity;
        client = Nearby.getConnectionsClient(activity);
    }

    private void emit(String type, String... keyValues) {
        try {
            JSONObject o = new JSONObject();
            o.put("type", type);
            for (int i = 0; i + 1 < keyValues.length; i += 2) o.put(keyValues[i], keyValues[i + 1]);
            act.emit(o);
        } catch (Exception ignored) {
        }
    }

    private final PayloadCallback payloads = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            if (payload.getType() == Payload.Type.BYTES) {
                byte[] b = payload.asBytes();
                if (b != null) emit("message", "id", endpointId, "data", new String(b, StandardCharsets.UTF_8));
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String endpointId, @NonNull PayloadTransferUpdate update) {
        }
    };

    private final ConnectionLifecycleCallback lifecycle = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo info) {
            names.put(endpointId, info.getEndpointName());
            client.acceptConnection(endpointId, payloads);
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                String n = names.get(endpointId);
                emit("connected", "id", endpointId, "name", n != null ? n : "Player");
            } else {
                emit("connectFailed", "id", endpointId, "msg", String.valueOf(result.getStatus().getStatusMessage()));
            }
        }

        @Override
        public void onDisconnected(@NonNull String endpointId) {
            names.remove(endpointId);
            emit("disconnected", "id", endpointId);
        }
    };

    private final EndpointDiscoveryCallback discovery = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo info) {
            if (SERVICE_ID.equals(info.getServiceId())) emit("found", "id", endpointId, "name", info.getEndpointName());
        }

        @Override
        public void onEndpointLost(@NonNull String endpointId) {
            emit("lost", "id", endpointId);
        }
    };

    void host(String tableName) {
        AdvertisingOptions o = new AdvertisingOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        client.startAdvertising(tableName, SERVICE_ID, lifecycle, o)
                .addOnSuccessListener(v -> emit("hosting"))
                .addOnFailureListener(e -> emit("error", "msg", String.valueOf(e.getMessage())));
    }

    void discover() {
        DiscoveryOptions o = new DiscoveryOptions.Builder().setStrategy(Strategy.P2P_STAR).build();
        client.startDiscovery(SERVICE_ID, discovery, o)
                .addOnSuccessListener(v -> emit("discovering"))
                .addOnFailureListener(e -> emit("error", "msg", String.valueOf(e.getMessage())));
    }

    void stopDiscovery() {
        client.stopDiscovery();
    }

    void connect(String endpointId, String playerName) {
        client.requestConnection(playerName, endpointId, lifecycle)
                .addOnFailureListener(e -> emit("connectFailed", "id", endpointId, "msg", String.valueOf(e.getMessage())));
    }

    void send(String endpointId, String data) {
        client.sendPayload(endpointId, Payload.fromBytes(data.getBytes(StandardCharsets.UTF_8)));
    }

    void disconnect(String endpointId) {
        client.disconnectFromEndpoint(endpointId);
    }

    void stopAll() {
        client.stopAdvertising();
        client.stopDiscovery();
        client.stopAllEndpoints();
        names.clear();
    }
}
