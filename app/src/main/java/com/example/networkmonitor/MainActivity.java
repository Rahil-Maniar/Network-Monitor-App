package com.example.networkmonitor;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1;
    private boolean isMonitoring = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView monitoringStatusTextView = findViewById(R.id.monitoringStatusTextView);
        Button startMonitoringButton = findViewById(R.id.startMonitoringButton);
        Button stopMonitoringButton = findViewById(R.id.stopMonitoringButton);
        Button openHotspotSettingsButton = findViewById(R.id.openHotspotSettingsButton);
        Button force5GButton = findViewById(R.id.force5GButton);

        startMonitoringButton.setOnClickListener(v -> {
            if (isMobileDataEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                    } else {
                        startService(new Intent(this, NetworkMonitorService.class));
                        isMonitoring = true;
                        monitoringStatusTextView.setText("Monitoring Started");
                    }
                } else {
                    startService(new Intent(this, NetworkMonitorService.class));
                    isMonitoring = true;
                    monitoringStatusTextView.setText("Monitoring Started");
                }
            } else {
                Toast.makeText(this, "Please turn on mobile data.", Toast.LENGTH_SHORT).show();
            }
        });

        stopMonitoringButton.setOnClickListener(v -> {
            stopService(new Intent(this, NetworkMonitorService.class));
            isMonitoring = false;
            monitoringStatusTextView.setText("Monitoring Stopped");
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startService(new Intent(this, NetworkMonitorService.class));
                isMonitoring = true;
                TextView monitoringStatusTextView = findViewById(R.id.monitoringStatusTextView);
                monitoringStatusTextView.setText("Monitoring Started");
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
        builder.setTitle("Enable 5G")
                .setMessage("To force 5G only mode, go to Settings > Network & internet > Mobile network > Preferred network type and select 5G.")
                .setPositiveButton("Open Settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                    startActivity(intent);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
