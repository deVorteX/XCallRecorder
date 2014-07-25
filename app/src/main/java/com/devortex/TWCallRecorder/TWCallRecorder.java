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

        final boolean enabled = prefs.getBoolean("enable", false);
        if (loadPackageParam.packageName.equals("com.android.incallui")) {
            findAndHookMethod("com.android.services.telephony.common.PhoneFeature", loadPackageParam.classLoader, "makeFeatureForCommon", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    doMod(loadPackageParam, enabled);
                }
            });
        } else if (loadPackageParam.packageName.equals("com.android.phone")) {
            findAndHookMethod("com.android.phone.PhoneFeature", loadPackageParam.classLoader, "makeFeatureForCommon", new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    doMod(loadPackageParam, enabled);
                }
            });
        }
    }

    private void doMod(LoadPackageParam loadPackageParam, boolean enabled) {
        Class<?> phoneFeature = XposedHelpers.findClass("com.android.services.telephony.common.PhoneFeature", loadPackageParam.classLoader);
        HashMap<String, Boolean> mFeatureList = (HashMap<String, Boolean>) XposedHelpers.getStaticObjectField(phoneFeature, "mFeatureList");
        if (mFeatureList != null) {
            mFeatureList.put("voice_call_recording", enabled && prefs.getBoolean("show_button", false));
            mFeatureList.put("voice_call_recording_menu", enabled && prefs.getBoolean("show_menu", false));
        } else {
            XposedBridge.log("TWCallRecorder: param is null");
        }
    }

    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {
        String packageName = TWCallRecorder.class.getPackage().getName();

        prefs = new XSharedPreferences(packageName);
        prefs.makeWorldReadable();
    }
}
