package com.devortex.xcallrecorder;

/**
 * Created by Patrick.Lower on 7/18/2014.
 */

import java.lang.reflect.Field;
import java.util.HashMap;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

public class main implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(final LoadPackageParam loadPackageParam) throws Throwable {
        if (!loadPackageParam.packageName.equals("com.android.incallui"))
            return;

        XposedBridge.log("Devortex: Entered Mod!");
        findAndHookMethod("com.android.services.telephony.common.PhoneFeature", loadPackageParam.classLoader, "makeFeatureForCommon", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("Devortex: inside hook");
                Class<?> phoneFeature = XposedHelpers.findClass("com.android.services.telephony.common.PhoneFeature", loadPackageParam.classLoader);
                HashMap<String, Boolean> mFeatureList = (HashMap<String, Boolean>) XposedHelpers.getStaticObjectField(phoneFeature, "mFeatureList");
                if (mFeatureList != null) {
                    mFeatureList.put("voice_call_recording", false); //disable record button in case its enabled
                    mFeatureList.put("voice_call_recording_menu", true); //enable record in menu
                } else {
                    XposedBridge.log("Devortex: param is null");
                }
            }
        });

        XposedBridge.log("Devortex: Finished Mod!");
    }
}
