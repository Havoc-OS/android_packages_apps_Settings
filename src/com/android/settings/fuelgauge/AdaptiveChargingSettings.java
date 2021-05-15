/*
 * Copyright (C) 2021 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceCategory;

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.havoc.support.preferences.CustomSeekBarPreference;
import com.havoc.support.preferences.SystemSettingListPreference;
import com.havoc.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for Adaptive charging
 */
public class AdaptiveChargingSettings extends DashboardFragment implements
        OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "AdaptiveChargingSettings";
    private static final String KEY_ADAPTIVE_CHARGING_MODE = "adaptive_charging_mode";
    private static final String KEY_ADAPTIVE_CHARGING_CUTOFF_LEVEL = "adaptive_charging_cutoff_level";
    private static final String KEY_ADAPTIVE_CHARGING_RESUME_LEVEL = "adaptive_charging_resume_level";
    private static final String KEY_ADAPTIVE_CHARGING_CUTOFF_TEMPERATURE = "adaptive_charging_cutoff_temperature";
    private static final String KEY_ADAPTIVE_CHARGING_RESUME_TEMPERATURE = "adaptive_charging_resume_temperature";
    private static final String KEY_ADAPTIVE_CHARGING_RESET_STATS = "adaptive_charging_reset_stats";
    private static final String KEY_ADAPTIVE_CHARGING_LEVEL_CATEGORY = "adaptive_charging_mode_level";
    private static final String KEY_ADAPTIVE_CHARGING_TEMPERATURE_CATEGORY = "adaptive_charging_mode_temperature";

    private PreferenceCategory mAdaptiveChargingLevel;
    private PreferenceCategory mAdaptiveChargingTemperature;
    private CustomSeekBarPreference mAdaptiveChargingCutoffLevel;
    private CustomSeekBarPreference mAdaptiveChargingResumeLevel;
    private CustomSeekBarPreference mAdaptiveChargingCutoffTemperature;
    private CustomSeekBarPreference mAdaptiveChargingResumeTemperature;
    private SystemSettingListPreference mAdaptiveChargingMode;
    private SystemSettingSwitchPreference mResetStats;

    private int mAdaptiveChargingCutoffLevelConfig;
    private int mAdaptiveChargingResumeLevelConfig;
    private int mAdaptiveChargingCutoffTemperatureConfig;
    private int mAdaptiveChargingResumeTemperatureConfig;

    private TextView mTextView;
    private View mSwitchBar;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mAdaptiveChargingCutoffLevelConfig = getResources().getInteger(
                com.android.internal.R.integer.config_adaptiveChargingCutoffLevel);
        mAdaptiveChargingResumeLevelConfig = getResources().getInteger(
                com.android.internal.R.integer.config_adaptiveChargingResumeLevel);
        mAdaptiveChargingCutoffTemperatureConfig = getResources().getInteger(
                com.android.internal.R.integer.config_adaptiveChargingCutoffTemperature);
        mAdaptiveChargingResumeTemperatureConfig = getResources().getInteger(
                com.android.internal.R.integer.config_adaptiveChargingResumeTemperature);

        mAdaptiveChargingLevel = (PreferenceCategory) findPreference(KEY_ADAPTIVE_CHARGING_LEVEL_CATEGORY);
        mAdaptiveChargingTemperature = (PreferenceCategory) findPreference(KEY_ADAPTIVE_CHARGING_TEMPERATURE_CATEGORY);

        mAdaptiveChargingMode = (SystemSettingListPreference) findPreference(KEY_ADAPTIVE_CHARGING_MODE);
        int adaptiveChargingMode = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING_MODE, 0);
        mAdaptiveChargingMode.setValue(String.valueOf(adaptiveChargingMode));
        mAdaptiveChargingMode.setSummary(mAdaptiveChargingMode.getEntry());
        mAdaptiveChargingMode.setOnPreferenceChangeListener(this);
        updateChargingPrefs(adaptiveChargingMode);

        mAdaptiveChargingCutoffLevel = (CustomSeekBarPreference) findPreference(KEY_ADAPTIVE_CHARGING_CUTOFF_LEVEL);
        int currentCutoffLevel = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING_CUTOFF_LEVEL, mAdaptiveChargingCutoffLevelConfig);
        mAdaptiveChargingCutoffLevel.setValue(currentCutoffLevel);
        mAdaptiveChargingCutoffLevel.setOnPreferenceChangeListener(this);

        mAdaptiveChargingResumeLevel = (CustomSeekBarPreference) findPreference(KEY_ADAPTIVE_CHARGING_RESUME_LEVEL);
        int currentResumeLevel = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING_RESUME_LEVEL, mAdaptiveChargingResumeLevelConfig);
        mAdaptiveChargingResumeLevel.setMax(currentCutoffLevel - 1);
        if (currentResumeLevel >= currentCutoffLevel) currentResumeLevel = currentCutoffLevel -1;
        mAdaptiveChargingResumeLevel.setValue(currentResumeLevel);
        mAdaptiveChargingResumeLevel.setOnPreferenceChangeListener(this);

        mAdaptiveChargingCutoffTemperature = (CustomSeekBarPreference) findPreference(KEY_ADAPTIVE_CHARGING_CUTOFF_TEMPERATURE);
        int currentCutoffTemperature = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING_CUTOFF_TEMPERATURE, mAdaptiveChargingCutoffTemperatureConfig);
        mAdaptiveChargingCutoffTemperature.setValue(currentCutoffTemperature);
        mAdaptiveChargingCutoffTemperature.setOnPreferenceChangeListener(this);

        mAdaptiveChargingResumeTemperature = (CustomSeekBarPreference) findPreference(KEY_ADAPTIVE_CHARGING_RESUME_TEMPERATURE);
        int currentResumeTemperature = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING_RESUME_TEMPERATURE, mAdaptiveChargingResumeTemperatureConfig);
        mAdaptiveChargingResumeTemperature.setMax(currentCutoffTemperature - 1);
        if (currentResumeTemperature >= currentCutoffTemperature) currentResumeTemperature = currentCutoffTemperature -1;
        mAdaptiveChargingResumeTemperature.setValue(currentResumeTemperature);
        mAdaptiveChargingResumeTemperature.setOnPreferenceChangeListener(this);

        mResetStats = (SystemSettingSwitchPreference) findPreference(KEY_ADAPTIVE_CHARGING_RESET_STATS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.master_setting_switch, container, false);
        ((ViewGroup) view).addView(super.onCreateView(inflater, container, savedInstanceState));
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING, 0) == 1;

        mTextView = view.findViewById(R.id.switch_text);
        mTextView.setText(getString(enabled ?
                R.string.switch_on_text : R.string.switch_off_text));

        mSwitchBar = view.findViewById(R.id.switch_bar);
        Switch switchWidget = mSwitchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(enabled);
        switchWidget.setOnCheckedChangeListener(this);
        mSwitchBar.setActivated(enabled);
        mSwitchBar.setOnClickListener(v -> {
            switchWidget.setChecked(!switchWidget.isChecked());
            mSwitchBar.setActivated(switchWidget.isChecked());
        });

        mAdaptiveChargingMode.setEnabled(enabled);
        mAdaptiveChargingCutoffLevel.setEnabled(enabled);
        mAdaptiveChargingResumeLevel.setEnabled(enabled);
        mAdaptiveChargingCutoffTemperature.setEnabled(enabled);
        mAdaptiveChargingResumeTemperature.setEnabled(enabled);
        mResetStats.setEnabled(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.System.putInt(getContentResolver(),
                Settings.System.ADAPTIVE_CHARGING, isChecked ? 1 : 0);
        mTextView.setText(getString(isChecked ? R.string.switch_on_text : R.string.switch_off_text));
        mSwitchBar.setActivated(isChecked);

        mAdaptiveChargingMode.setEnabled(isChecked);
        mAdaptiveChargingCutoffLevel.setEnabled(isChecked);
        mAdaptiveChargingResumeLevel.setEnabled(isChecked);
        mAdaptiveChargingCutoffTemperature.setEnabled(isChecked);
        mAdaptiveChargingResumeTemperature.setEnabled(isChecked);
        mResetStats.setEnabled(isChecked);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.adaptive_charging;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mAdaptiveChargingMode) {
            int adaptiveChargingMode = Integer.valueOf((String) objValue);
            int index = mAdaptiveChargingMode.findIndexOfValue((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_MODE, adaptiveChargingMode);
            mAdaptiveChargingMode.setSummary(mAdaptiveChargingMode.getEntries()[index]);
            updateChargingPrefs(adaptiveChargingMode);
            return true;
        } else if (preference == mAdaptiveChargingCutoffLevel) {
            int adaptiveChargingCutoffLevel = (Integer) objValue;
            int adaptiveChargingResumeLevel = Settings.System.getInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_RESUME_LEVEL, mAdaptiveChargingResumeLevelConfig);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_CUTOFF_LEVEL, adaptiveChargingCutoffLevel);
            mAdaptiveChargingResumeLevel.setMax(adaptiveChargingCutoffLevel - 1);
            if (adaptiveChargingCutoffLevel <= adaptiveChargingResumeLevel) {
                mAdaptiveChargingResumeLevel.setValue(adaptiveChargingCutoffLevel - 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.ADAPTIVE_CHARGING_RESUME_LEVEL, adaptiveChargingCutoffLevel - 1);
            }
            return true;
        } else if (preference == mAdaptiveChargingResumeLevel) {
            int adaptiveChargingResumeLevel = (Integer) objValue;
            int adaptiveChargingCutoffLevel = Settings.System.getInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_CUTOFF_LEVEL, mAdaptiveChargingCutoffLevelConfig);
            mAdaptiveChargingResumeLevel.setMax(adaptiveChargingCutoffLevel - 1);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_RESUME_LEVEL, adaptiveChargingResumeLevel);
            return true;
        } else if (preference == mAdaptiveChargingCutoffTemperature) {
            int adaptiveChargingCutoffTemperature = (Integer) objValue;
            int adaptiveChargingResumeTemperature = Settings.System.getInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_RESUME_TEMPERATURE, mAdaptiveChargingResumeTemperatureConfig);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_CUTOFF_TEMPERATURE, adaptiveChargingCutoffTemperature);
            mAdaptiveChargingResumeTemperature.setMax(adaptiveChargingCutoffTemperature - 1);
            if (adaptiveChargingCutoffTemperature <= adaptiveChargingResumeTemperature) {
                mAdaptiveChargingResumeTemperature.setValue(adaptiveChargingCutoffTemperature - 1);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.ADAPTIVE_CHARGING_RESUME_TEMPERATURE, adaptiveChargingCutoffTemperature - 1);
            }
            return true;
        } else if (preference == mAdaptiveChargingResumeTemperature) {
            int adaptiveChargingResumeTemperature = (Integer) objValue;
            int adaptiveChargingCutoffTemperature = Settings.System.getInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_CUTOFF_TEMPERATURE, mAdaptiveChargingCutoffTemperatureConfig);
            mAdaptiveChargingResumeTemperature.setMax(adaptiveChargingCutoffTemperature - 1);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.ADAPTIVE_CHARGING_RESUME_TEMPERATURE, adaptiveChargingResumeTemperature);
            return true;
        } else {
            return false;
        }
    }

    private void updateChargingPrefs(int adaptiveChargingMode) {
        switch (adaptiveChargingMode) {
            default:
            case 0:
                mAdaptiveChargingLevel.setEnabled(true);
                mAdaptiveChargingTemperature.setEnabled(false);
                break;
            case 1:
                mAdaptiveChargingLevel.setEnabled(false);
                mAdaptiveChargingTemperature.setEnabled(true);
                break;
            case 2:
                mAdaptiveChargingLevel.setEnabled(true);
                mAdaptiveChargingTemperature.setEnabled(true);
                break;
        }
    }
}
