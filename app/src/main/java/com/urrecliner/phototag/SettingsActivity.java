package com.urrecliner.phototag;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import static com.urrecliner.phototag.Vars.sharedAlpha;
import static com.urrecliner.phototag.Vars.sharedAutoLoad;
import static com.urrecliner.phototag.Vars.sharedRadius;
import static com.urrecliner.phototag.Vars.sharedSort;
import static com.urrecliner.phototag.Vars.sharedSpan;
import static com.urrecliner.phototag.Vars.utils;

public class SettingsActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        final String RADIUS = "radius", SORT = "sort", AUTO_LOAD = "autoLoad", SPAN = "span", ALPHA = "alpha";
        Preference pRadius, pSort, pAutoLoad, pSpan, pAlpha;

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.set_preferences, rootKey);

            final String fRadius = "반경 %s m내의 장소를 검색합니다";
            pRadius = findPreference(RADIUS);
            pRadius.setSummary(String.format(fRadius, sharedRadius));
            pRadius.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedRadius = newValue.toString();
                pRadius.setSummary(String.format(fRadius, sharedRadius));
                return true;
            });

            pSort = findPreference(SORT);
            pSort.setSummary((sharedSort.equals("none")) ? "정렬하지 않고 보여 줍니다":sharedSort + " 순으로 보여 줍니다");
            pSort.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedSort = newValue.toString();
                pSort.setSummary((sharedSort.equals("none")) ? "정렬하지 않고 보여 줍니다":sharedSort + " 순으로 보여 줍니다");
                return true;
            });

            pAutoLoad = findPreference(AUTO_LOAD);
            pAutoLoad.setSummary(sharedAutoLoad ? "사진을 선택하면 장소를 자동으로 검색" : "사진을 선택해도 장소 목록은 버튼을 눌러야 보임");
            pAutoLoad.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedAutoLoad = Boolean.parseBoolean(newValue.toString());
                pAutoLoad.setSummary(sharedAutoLoad ? "사진을 선택하면 장소를 자동으로 검색" : "사진을 선택해도 장소 목록은 버튼을 눌러야 보임");
                return true;
            });

            pSpan = findPreference(SPAN);
            pSpan.setSummary("한 줄에 "+(sharedSpan.equals("2") ? "두": "세")+" 장의 사진을 보여줍니다");
            pSpan.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedSpan = newValue.toString();
                pSpan.setSummary("한 줄에 "+(sharedSpan.equals("2") ? "두": "세")+" 장의 사진을 보여줍니다");
                return true;
            });

            pAlpha = findPreference(ALPHA);
            pAlpha.setOnPreferenceChangeListener((preference, newValue) -> {
                sharedAlpha = newValue.toString();
                return true;
            });
        }
    }

    @Override
    protected void onDestroy() {
        utils.getPreference();
        MainActivity.prepareCards();
        super.onDestroy();
    }

}