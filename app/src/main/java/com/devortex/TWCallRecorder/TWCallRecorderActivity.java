package com.devortex.TWCallRecorder;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;

import com.devortex.TWCallRecorder.utils.NotificationUtils;
import com.devortex.TWCallRecorder.utils.RootUtil;


public class TWCallRecorderActivity extends Activity {
    private static TWCallRecorderActivity mInstance = null;
    public static String TAG = "TWCallRecorderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RootUtil ru = new RootUtil();
        ru.startShell();
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new PrefsFragment())
                    .commit();
        }
        mInstance = this;
    }


    public static class PrefsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }

        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            NotificationUtils nu = new NotificationUtils();
            nu.notifyRestart();
        }
    }

    public static TWCallRecorderActivity getInstance() {
        return mInstance;
    }

}
