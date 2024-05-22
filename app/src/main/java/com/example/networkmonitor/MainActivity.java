package com.example.networkmonitor;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isMonitoring = false;
    private AnimationDrawable monitoringAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView monitoringAnimationView = findViewById(R.id.monitoringAnimationView);
        Button startMonitoringButton = findViewById(R.id.startMonitoringButton);
        Button stopMonitoringButton = findViewById(R.id.stopMonitoringButton);
        Button openHotspotSettingsButton = findViewById(R.id.openHotspotSettingsButton);
        Button force5GButton = findViewById(R.id.force5GButton);

        monitoringAnimationView.setBackgroundResource(R.drawable.monitoring_animation);
        monitoringAnimation = (AnimationDrawable) monitoringAnimationView.getBackground();

        startMonitoringButton.setOnClickListener(v -> {
            if (isMobileDataEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                    } else {
                        startMonitoring(monitoringAnimationView);
                    }
                } else {
                    startMonitoring(monitoringAnimationView);
                }
            } else {
                Toast.makeText(this, "Please turn on mobile data.", Toast.LENGTH_SHORT).show();
            }
        });

        stopMonitoringButton.setOnClickListener(v -> {
            stopService(new Intent(this, NetworkMonitorService.class));
            isMonitoring = false;
            monitoringAnimationView.setVisibility(ImageView.GONE);
            monitoringAnimation.stop();
        });

        openHotspotSettingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
            startActivity(intent);
        });

        force5GButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 34) {
                show5GSettingsInstructions();
            } else {
                if (!force5GOnly()) {
                    show5GSettingsInstructions();
                }
            }
        });
    }

    private void startMonitoring(ImageView monitoringAnimationView) {
        startService(new Intent(this, NetworkMonitorService.class));
        isMonitoring = true;
        monitoringAnimationView.setVisibility(ImageView.VISIBLE);
        monitoringAnimation.start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImageView monitoringAnimationView = findViewById(R.id.monitoringAnimationView);
                startMonitoring(monitoringAnimationView);
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isMobileDataEnabled() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            return cm.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_MOBILE;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean force5GOnly() {
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Method getITelephonyMethod = telephonyManager.getClass().getDeclaredMethod("getITelephony");
            getITelephonyMethod.setAccessible(true);
            Object iTelephony = getITelephonyMethod.invoke(telephonyManager);

            Method setPreferredNetworkTypeMethod = iTelephony.getClass().getDeclaredMethod("setPreferredNetworkType", int.class);
            setPreferredNetworkTypeMethod.setAccessible(true);
            setPreferredNetworkTypeMethod.invoke(iTelephony, 20); // Assuming 20 is the constant for 5G NR_ONLY

            Toast.makeText(this, "Network set to 5G only", Toast.LENGTH_SHORT).show();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void show5GSettingsInstructions() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set 5G Network Manually");
        builder.setMessage("Your device does not support programmatic switching to 5G networks. " +
                "Please follow these steps to manually set your network mode to 5G:\n\n" +
                "1. Open the Settings app on your device.\n" +
                "2. Navigate to 'Network & Internet' or 'Connections'.\n" +
                "3. Select 'Mobile Network'.\n" +
                "4. Choose 'Preferred network type'.\n" +
                "5. Select '5G' or '5G/LTE/3G/2G'.\n\n" +
                "If you cannot find these options, please refer to your device's user manual or contact the device manufacturer for assistance.");
        builder.setPositiveButton("Open Settings", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
            startActivity(intent);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
