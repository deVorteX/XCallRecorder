package com.devortex.TWCallRecorder;

/**
 * Created by Patrick.Lower on 7/18/2014.
 */

import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class TWCallRecorder implements IXposedHookZygoteInit, IXposedHookLoadPackage {
    private static XSharedPreferences prefs;

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.android.incallui") && !loadPackageParam.packageName.equals("com.android.phone"))
            return;

        if (prefs.getBoolean("enable", false)) {
            String featureClass = "com.android.services.telephony.common.PhoneFeature";
            if (loadPackageParam.packageName.equals("com.android.phone")) {
                featureClass = "com.android.phone.PhoneFeature";
            }
            findAndHookMethod(featureClass, loadPackageParam.classLoader, "hasFeature", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    doMod(param);
                }
            });
        }
    }

    private void doMod(XC_MethodHook.MethodHookParam param) {
        if ("voice_call_recording".equals(param.args[0]) && prefs.getBoolean("show_button", false)) {
            param.setResult(Boolean.TRUE);
        }
        if ("voice_call_recording_menu".equals(param.args[0]) && prefs.getBoolean("show_menu", false)) {
            param.setResult(Boolean.TRUE);
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        String packageName = TWCallRecorder.class.getPackage().getName();

        prefs = new XSharedPreferences(packageName);
        prefs.makeWorldReadable();
    }
}
