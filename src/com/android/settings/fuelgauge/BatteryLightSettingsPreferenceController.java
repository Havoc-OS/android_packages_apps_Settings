/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;

import com.havoc.support.preferences.SystemSettingMasterSwitchPreference;

public class BatteryLightSettingsPreferenceController extends BasePreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_CHARGING_LED = "battery_light_enabled";

    private SystemSettingMasterSwitchPreference mChargingLed;

    public BatteryLightSettingsPreferenceController(Context context) {
        super(context, KEY_CHARGING_LED);
    }


    @Override
    public int getAvailabilityStatus() {
        boolean hasLED = mContext.getResources().getBoolean(com.android.internal.R.bool.config_intrusiveBatteryLed) ||
                mContext.getResources().getBoolean(com.android.internal.R.bool.config_multiColorBatteryLed);
        return hasLED ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_CHARGING_LED;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        mChargingLed = (SystemSettingMasterSwitchPreference) screen.findPreference(KEY_CHARGING_LED);
        mChargingLed.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, 1) == 1));
        mChargingLed.setOnPreferenceChangeListener(this);

        super.displayPreference(screen);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.BATTERY_LIGHT_ENABLED, value ? 1 : 0);
        return true;
    }
}
