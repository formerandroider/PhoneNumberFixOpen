package com.liam_w.phonenumberfix;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;

    SharedPreferences prefs;

    TelephonyManager telephonyManager;

    TextView oldNumber;
    EditText newNumber;
    Button saveNumber;

    PhoneNumberUtil phoneUtil;

    boolean permissionsChecked = false;

    private static final int PERM_REQUEST_READ_PHONE_STATE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        oldNumber = (TextView) findViewById(R.id.tvOldNumber);
        newNumber = (EditText) findViewById(R.id.etNewNumber);
        saveNumber = (Button) findViewById(R.id.btnSaveNumber);
        saveNumber.setOnClickListener(this);

        //noinspection deprecation
        prefs = getPreferences(Context.MODE_WORLD_READABLE);

        newNumber.setText(prefs.getString("newNumber", ""));

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        phoneUtil = PhoneNumberUtil.getInstance();

        updateCurrentNumber();
    }

    private void updateCurrentNumber() {

        if (checkPermissions()) {

            String currentNumber = telephonyManager.getLine1Number();
            if (currentNumber == null || currentNumber.trim().isEmpty()) {
                oldNumber.setText(R.string.current_number_unknown);
            } else {
                oldNumber.setText(getString(R.string.current_number_x, currentNumber));
            }

        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            if (!permissionsChecked)
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PERM_REQUEST_READ_PHONE_STATE);

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERM_REQUEST_READ_PHONE_STATE:
                permissionsChecked = true;

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateCurrentNumber();
                } else {
                    oldNumber.setText(getString(R.string.current_number_unavailable_no_permission));
                }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveNumber:

                Phonenumber.PhoneNumber phoneNumber;

                try {
                    phoneNumber = phoneUtil.parse(newNumber.getText().toString(), Locale.getDefault().getCountry());
                } catch (NumberParseException e) {
                    Toast.makeText(this, R.string.invalid_number_not_saving, Toast.LENGTH_SHORT).show();

                    return;
                }

                if (!phoneUtil.isValidNumberForRegion(phoneNumber, Locale.getDefault().getCountry())) {
                    Toast.makeText(this, R.string.invalid_number_not_saving, Toast.LENGTH_SHORT).show();

                    return;
                }


                if (prefs.edit().putString("newNumber", newNumber.getText().toString()).commit()) {
                    Toast.makeText(context, R.string.saved_successfully, Toast.LENGTH_SHORT).show();

                    (new AlertDialog.Builder(context)).setTitle(R.string.alert_reboot_required_title).setMessage(R.string.alert_reboot_required_message).setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).setNegativeButton(R.string.check_xposed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent xposedIntent = getPackageManager().getLaunchIntentForPackage("de.robv.android.xposed.installer");

                            if (xposedIntent == null) {
                                Toast.makeText(context, R.string.xposed_installer_isnt_installed, Toast.LENGTH_LONG).show();
                            } else {
                                startActivity(xposedIntent);
                            }
                        }
                    }).setCancelable(true).show();
                } else {
                    Toast.makeText(context, R.string.unable_to_save, Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }
}
