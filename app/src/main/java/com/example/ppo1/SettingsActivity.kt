package com.example.ppo1

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat.recreate
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import kotlinx.android.synthetic.main.settings_activity.*
import java.util.*
import android.app.ActivityManager
import android.content.SharedPreferences

import android.os.Build.VERSION

import android.os.Build.VERSION_CODES
import androidx.core.view.children
import com.example.ppo1.util.PrefUtil
import kotlinx.android.synthetic.main.mylayout.*


class SettingsActivity : BaseActivity() {
    companion object {
        var isRecreating = false
        var goBack = false
        var isLanguageChanged = false
        var isFontChanged = false
        var b2 = false
        var b3 = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        clearAppDataB.setOnClickListener {
            PrefUtil.removeData(this)
        }

        if (isRecreating) {
            isRecreating = false
            goBack = true
        }

    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val theme = findPreference<SwitchPreferenceCompat>("theme")

            theme?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue as Boolean) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                isRecreating = true

                recreate(context as Activity)
                true
            }

            val language = findPreference<ListPreference>("language")
            language?.setOnPreferenceChangeListener { preference, newValue ->

                isLanguageChanged = true
                if (newValue == "en" || newValue == "ru") {
                    dLocale = Locale(newValue as String)
                }
                recreate(context as Activity)
                true
            }

            val fontSize = findPreference<ListPreference>("fontSize")
            fontSize?.setOnPreferenceChangeListener { preference, newValue ->
                isFontChanged = true
                dFontSize = newValue as String
                recreate(context as Activity)
                true
            }
        }

        /*
            public void setLocale(String lang) {

                Locale myLocale = new Locale(lang);
                Resources res = getResources();
                DisplayMetrics dm = res.getDisplayMetrics();
                Configuration conf = res.getConfiguration();
                conf.locale = myLocale;
                res.updateConfiguration(conf, dm);
                Locale.setDefault(myLocale);
                onConfigurationChanged(conf);
            }

            private static void setLanguagePref(Context mContext, String localeKey) {
                AppPreference appPreference = new AppPreference(mContext);
                appPreference.setLanguage(localeKey);
            }

            public static String getLanguagePref(Context mContext) {
                AppPreference appPreference = new AppPreference(mContext);
                return appPreference.getLanguage();
            }
        */
        /*
            @Override
            protected void attachBaseContext(Context base) {
                super.attachBaseContext(base);
            }

            @Override
            public void onConfigurationChanged(Configuration newConfig) {
                //set Text Again of All layouts
                //Prefer to make a function and then set All Text

                super.onConfigurationChanged(newConfig);
            }

            @Override
            protected void onResume() {

                setLocale(getLanguagePref(getApplicationContext()));
                super.onResume();
            }
        */
        /*
            //Now set the Language on Any Event like Onclick

            setLocale("ar");
            setLanguagePref(MainActivity.this, "ar");
        */
    }
}