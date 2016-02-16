package com.liam_w.phonenumberfix;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("MainActivity", Context.MODE_WORLD_READABLE);

        if (preferences.getBoolean("rebootRequired", false) && (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED)) {
            String wantedNumber = preferences.getString("newNumber", "");

            if (wantedNumber.trim().isEmpty()) {
                return;
            }

            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String currentNumber = telephonyManager.getLine1Number();

            if (wantedNumber.equals(currentNumber)) {
                NotificationCompat.Builder notification = new NotificationCompat.Builder(context);
                notification.setContentTitle("Mobile Number Set!");
                notification.setContentText("The SIM mobile number value has been successfully changed.");
                notification.setAutoCancel(true);
                notification.setSmallIcon(R.mipmap.ic_launcher);

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1000, notification.build());
            }

            preferences.edit().putBoolean("rebootRequired", false).apply();
        }
    }
}
