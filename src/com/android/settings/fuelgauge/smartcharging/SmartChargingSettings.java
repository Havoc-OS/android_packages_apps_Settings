/*
 * Copyright (C) 2019 RevengeOS
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

package com.android.settings.fuelgauge.smartcharging;

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

import com.android.internal.logging.nano.MetricsProto;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import com.android.settingslib.core.lifecycle.Lifecycle;

import com.havoc.support.preferences.CustomSeekBarPreference;
import com.havoc.support.preferences.SystemSettingSwitchPreference;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings screen for Smart charging
 */
public class SmartChargingSettings extends DashboardFragment implements
        OnPreferenceChangeListener, CompoundButton.OnCheckedChangeListener {

    private static final String TAG = "SmartChargingSettings";
    private static final String KEY_SMART_CHARGING_LEVEL = "smart_charging_level";
    private static final String KEY_SMART_CHARGING_RESUME_LEVEL = "smart_charging_resume_level";
    private CustomSeekBarPreference mSmartChargingLevel;
    private CustomSeekBarPreference mSmartChargingResumeLevel;
    private SystemSettingSwitchPreference mResetStats;

    private int mSmartChargingLevelDefaultConfig;
    private int mSmartChargingResumeLevelDefaultConfig;

    private TextView mTextView;
    private View mSwitchBar;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        mSmartChargingLevelDefaultConfig = getResources().getInteger(
                com.android.internal.R.integer.config_smartChargingBatteryLevel);

        mSmartChargingResumeLevelDefaultConfig = getResources().getInteger(
                com.android.internal.R.integer.config_smartChargingBatteryResumeLevel);

        mSmartChargingLevel = (CustomSeekBarPreference) findPreference(KEY_SMART_CHARGING_LEVEL);
        int currentLevel = Settings.System.getInt(getContentResolver(),
            Settings.System.SMART_CHARGING_LEVEL, mSmartChargingLevelDefaultConfig);
        mSmartChargingLevel.setValue(currentLevel);
        mSmartChargingLevel.setOnPreferenceChangeListener(this);

        mSmartChargingResumeLevel = (CustomSeekBarPreference) findPreference(KEY_SMART_CHARGING_RESUME_LEVEL);
        int currentResumeLevel = Settings.System.getInt(getContentResolver(),
            Settings.System.SMART_CHARGING_RESUME_LEVEL, mSmartChargingResumeLevelDefaultConfig);
        mSmartChargingResumeLevel.setMax(currentLevel - 1);
        if (currentResumeLevel >= currentLevel) currentResumeLevel = currentLevel -1;
        mSmartChargingResumeLevel.setValue(currentResumeLevel);
        mSmartChargingResumeLevel.setOnPreferenceChangeListener(this);

        mResetStats = (SystemSettingSwitchPreference) findPreference("smart_charging_reset_stats");

        //mFooterPreferenceMixin.createFooterPreference().setTitle(R.string.smart_charging_footer);
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
                Settings.System.SMART_CHARGING, 0) == 1;

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

        mSmartChargingLevel.setEnabled(enabled);
        mSmartChargingResumeLevel.setEnabled(enabled);
        mResetStats.setEnabled(enabled);
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.System.putInt(getContentResolver(),
                Settings.System.SMART_CHARGING, isChecked ? 1 : 0);
        mTextView.setText(getString(isChecked ? R.string.switch_on_text : R.string.switch_off_text));
        mSwitchBar.setActivated(isChecked);

        mSmartChargingLevel.setEnabled(isChecked);
        mSmartChargingResumeLevel.setEnabled(isChecked);
        mResetStats.setEnabled(isChecked);
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.smart_charging;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HAVOC_SETTINGS;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        if (preference == mSmartChargingLevel) {
            int smartChargingLevel = (Integer) objValue;
            int mChargingResumeLevel = Settings.System.getInt(getContentResolver(),
                     Settings.System.SMART_CHARGING_RESUME_LEVEL, mSmartChargingResumeLevelDefaultConfig);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SMART_CHARGING_LEVEL, smartChargingLevel);
                mSmartChargingResumeLevel.setMax(smartChargingLevel - 1);
            if (smartChargingLevel <= mChargingResumeLevel) {
                mSmartChargingResumeLevel.setValue(smartChargingLevel - 1);
                Settings.System.putInt(getContentResolver(),
                    Settings.System.SMART_CHARGING_RESUME_LEVEL, smartChargingLevel - 1);
            }
            return true;
        } else if (preference == mSmartChargingResumeLevel) {
            int smartChargingResumeLevel = (Integer) objValue;
            int mChargingLevel = Settings.System.getInt(getContentResolver(),
                     Settings.System.SMART_CHARGING_LEVEL, mSmartChargingLevelDefaultConfig);
                mSmartChargingResumeLevel.setMax(mChargingLevel - 1);
               Settings.System.putInt(getContentResolver(),
                    Settings.System.SMART_CHARGING_RESUME_LEVEL, smartChargingResumeLevel);
            return true;
        } else {
            return false;
        }
    }
}
