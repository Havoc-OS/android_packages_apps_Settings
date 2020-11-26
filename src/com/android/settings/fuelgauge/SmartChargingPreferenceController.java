/*
 * Copyright (C) 2020 Havoc-OS
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.android.settings.fuelgauge;

import android.content.Context;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.havoc.support.preferences.SystemSettingMasterSwitchPreference;

public class SmartChargingPreferenceController extends AbstractPreferenceController implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_SMART_CHARGING = "smart_charging";

    private SystemSettingMasterSwitchPreference mSmartCharging;

    public SmartChargingPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_SMART_CHARGING;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        mSmartCharging = (SystemSettingMasterSwitchPreference) screen.findPreference(KEY_SMART_CHARGING);
        mSmartCharging.setChecked((Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.SMART_CHARGING, 0) == 1));
        mSmartCharging.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean value = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(),
                Settings.System.SMART_CHARGING, value ? 1 : 0);
        return true;
    }
}
