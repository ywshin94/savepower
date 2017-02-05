package com.voidpointer.selfgauge;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;

/**
 * Created by SHIN on 2016-08-22.
 */
public class SettingActivity extends PreferenceActivity {
    private static final String TAG = "ywshin";
    void _log(String log){
        Log.v(TAG, log);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MyPreferenceFragment() )
                .commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        _log("SettingActivity... onOptionsItemSelected");
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        _log("SettingActivity... onBackPressed");
        super.onBackPressed();
    }

    // PreferenceFragment 클래스 사용
    public static class MyPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener  {

        private static final String TAG = "ywshin";
        void _log(String log){
            Log.v(TAG, log);
        }

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            _log( "MyPreferenceFragment... onCreate" );
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_setting);
        }

        @Override
        public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
            String mKey = preference.getKey();
            if(mKey.equals("exportDB")){
                _log("exportDB");
            }
            else if(mKey.equals("importDB")){
                _log("importDB");
            }
            else if(mKey.equals("deleteDB")){
                _log("deleteDB");
            }
            else if(mKey.equals("sendMail")){
                _log("sendMail");
            }

            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            Preference preference = findPreference(s);
            if (s.equals("powerType")) {
                preference.setSummary(((ListPreference) preference).getEntry());
            }else if (s.equals("checkDay")) {
                preference.setSummary(((ListPreference) preference).getEntry());
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            _log( "MyPreferenceFragment... onResume" );
            getPreferenceManager()
                    .getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            _log( "MyPreferenceFragment... onPause" );
            getPreferenceManager()
                    .getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }
}
