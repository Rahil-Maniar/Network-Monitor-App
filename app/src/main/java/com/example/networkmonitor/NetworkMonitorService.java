package com.example.networkmonitor;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.lang.reflect.Method;

public class NetworkMonitorService extends Service {

    private static final String CHANNEL_ID = "networkMonitorChannel";
    private static final String PREFS_NAME = "NetworkMonitorPrefs";
    private static final String LAST_NETWORK_TYPE = "lastNetworkType";
    private static final String LAST_HOTSPOT_STATE = "lastHotspotState";
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        monitorNetwork();
        initializeLastKnownState();
    }

    private void initializeLastKnownState() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastNetworkType = prefs.getInt(LAST_NETWORK_TYPE, TelephonyManager.NETWORK_TYPE_UNKNOWN);
        boolean lastHotspotState = prefs.getBoolean(LAST_HOTSPOT_STATE, false);

        if (lastNetworkType == TelephonyManager.NETWORK_TYPE_NR && !lastHotspotState) {
            notifyUser("Please turn on the hotspot.");
        } else if (is4G(lastNetworkType) && lastHotspotState) {
            notifyUser("Please turn off the hotspot.");
        }
    }

    private void monitorNetwork() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                updateNotificationBasedOnNetworkState();
            }

            @Override
            public void onLost(Network network) {
                updateNotificationBasedOnNetworkState();
            }
        };
        cm.registerDefaultNetworkCallback(networkCallback);
    }

    private void updateNotificationBasedOnNetworkState() {
        int currentNetworkType = getNetworkType();
        boolean is5G = currentNetworkType == TelephonyManager.NETWORK_TYPE_NR;
        boolean is4G = is4G(currentNetworkType);
        boolean isHotspotOn = isHotspotEnabled();

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int lastNetworkType = prefs.getInt(LAST_NETWORK_TYPE, -1);
        boolean lastHotspotState = prefs.getBoolean(LAST_HOTSPOT_STATE, false);

        if (is5G && !isHotspotOn && (lastNetworkType != currentNetworkType || lastHotspotState != isHotspotOn)) {
            notifyUser("Please turn on the hotspot.");
            saveLastKnownState(currentNetworkType, isHotspotOn);
        } else if (is4G && isHotspotOn && (lastNetworkType != currentNetworkType || lastHotspotState != isHotspotOn)) {
            notifyUser("Please turn off the hotspot.");
            saveLastKnownState(currentNetworkType, isHotspotOn);
        } else {
            clearNotification();
        }
    }

    private int getNetworkType() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            stopSelf(); // Stop the service if permission is not granted
            return TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
        return telephonyManager.getNetworkType();
    }

    private boolean isHotspotEnabled() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        try {
            Method method = wifiManager.getClass().getDeclaredMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean is4G(int networkType) {
        return networkType == TelephonyManager.NETWORK_TYPE_LTE
                || networkType == TelephonyManager.NETWORK_TYPE_EVDO_0
                || networkType == TelephonyManager.NETWORK_TYPE_EVDO_A
                || networkType == TelephonyManager.NETWORK_TYPE_EVDO_B
                || networkType == TelephonyManager.NETWORK_TYPE_EHRPD;
    }

    private void notifyUser(String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Network Monitor")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            // Permission should already be granted at this point, but if not, do nothing
            return;
        }
        notificationManager.notify(1, builder.build());
    }

    private void clearNotification() {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(1); // Cancel the notification with id 1
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Network Monitor Channel";
            String description = "Channel for Network Monitor notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void saveLastKnownState(int networkType, boolean hotspotState) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_NETWORK_TYPE, networkType);
        editor.putBoolean(LAST_HOTSPOT_STATE, hotspotState);
        editor.apply();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        cm.unregisterNetworkCallback(networkCallback);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
