package com.liam_w.phonenumberfix;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.telephony.TelephonyManager;

public class BootReceiver extends BroadcastReceiver {

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
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
                notification.setContentTitle(context.getString(R.string.mobile_number_set));
                notification.setContentText(context.getString(R.string.mobile_number_successfully_changed));
                notification.setContentIntent(PendingIntent.getBroadcast(context, 10, new Intent(context, DeleteNotificationReceiver.class), 0));
                notification.setSmallIcon(R.mipmap.ic_launcher);
                notification.addAction(android.R.drawable.btn_star_big_on, context.getString(R.string.rate_app), PendingIntent.getActivity(context, 1, new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        ((Build.VERSION.SDK_INT >= 21) ? Intent.FLAG_ACTIVITY_NEW_DOCUMENT : Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET) |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK), 0));

                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1000, notification.build());

                preferences.edit().putBoolean("rebootRequired", false).apply();
            }
        }
    }
}
