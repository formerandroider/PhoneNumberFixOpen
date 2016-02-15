package com.liam_w.phonenumberfix.xposed;

import android.content.Context;
import android.telephony.TelephonyManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Module implements IXposedHookLoadPackage {

    private static final String PACKAGE_NAME = "com.liam_w.phonenumberfix";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {
        if (loadPackageParam.packageName.equals("com.android.phone")) {
            Class hookClass;

            try {
                hookClass = XposedHelpers.findClass("com.android.phone.PhoneGlobals", loadPackageParam.classLoader);
            } catch (XposedHelpers.ClassNotFoundError e) {
                XposedBridge.log("PhoneGlobals class not found! Please contact Liam.");

                return;
            }

            XSharedPreferences prefs;

            prefs = new XSharedPreferences(PACKAGE_NAME, "MainActivity");

            final String newNumber = prefs.getString("newNumber", null);

            if (newNumber == null) {
                XposedBridge.log("Unable to get the new number from preferences.");
                return;
            }

            XposedHelpers.findAndHookMethod(hookClass, "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                    final Object mCM; // CallManager

                    try {
                        mCM = XposedHelpers.getObjectField(param.thisObject, "mCM");
                    } catch (NoSuchFieldError e) {
                        XposedBridge.log("mCM field not found. Not compatible :(");

                        return;
                    }

                    if (mCM == null) {
                        XposedBridge.log("mCM field null. Not compatible :(");

                        return;
                    }

                    final Object phone = XposedHelpers.callMethod(mCM, "getDefaultPhone");

                    if (phone == null) {
                        XposedBridge.log("Default phone null.");

                        return;
                    }

                    final TelephonyManager telephonyManager = (TelephonyManager) ((Context) param.thisObject).getSystemService(Context.TELEPHONY_SERVICE);

                    new Thread(new Runnable() {
                        public void run() {

                            for (int simState = telephonyManager.getSimState(); simState != TelephonyManager.SIM_STATE_READY; simState = telephonyManager.getSimState()) {
                                try {
                                    Thread.sleep(5000); // Wait until the SIM is ready.
                                } catch (InterruptedException ignored) {
                                }
                            }

                            String alphaTag = (String) XposedHelpers.callMethod(phone, "getLine1AlphaTag");

                            if (alphaTag == null || alphaTag.isEmpty() || alphaTag.trim().equals("")) {
                                alphaTag = "Line 1";
                                XposedBridge.log("Tag was empty - 'Line 1' now");
                            }

                            String currentNumber = (String) XposedHelpers.callMethod(phone, "getLine1Number");

                            XposedBridge.log("Current number" + currentNumber);

                            if (newNumber.equals(currentNumber))
                                return;

                            XposedBridge.log("Next line is number writing call...");

                            XposedHelpers.callMethod(phone, "setLine1Number", alphaTag, newNumber, null);
                        }
                    }).start();
                }

            });
        }
    }
}