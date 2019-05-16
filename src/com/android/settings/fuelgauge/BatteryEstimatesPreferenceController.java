/*
 * Copyright (C) 2019 Havoc-OS
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
import android.content.Intent;
import android.provider.Settings;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;

import com.android.internal.statusbar.IStatusBarService;
import com.android.settings.DisplaySettings;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

import com.havoc.settings.Utils;

import static android.provider.Settings.System.SHOW_BATTERY_ESTIMATE;

public class BatteryEstimatesPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_BATTERY_ESTIMATE = "show_battery_estimate";
    private IStatusBarService mStatusBarService;

    public BatteryEstimatesPreferenceController(Context context) {
        super(context);
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BATTERY_ESTIMATE;
    }

    @Override
    public void updateState(Preference preference) {
        int batteryEstimateValue = Settings.System.getIntForUser(mContext.getContentResolver(),
                SHOW_BATTERY_ESTIMATE, 1, UserHandle.USER_CURRENT);
        ((SwitchPreference) preference).setChecked(batteryEstimateValue != 0);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean batteryEstimateValue = (Boolean) newValue;
        Settings.System.putInt(mContext.getContentResolver(), SHOW_BATTERY_ESTIMATE, batteryEstimateValue ? 1 : 0);
        IStatusBarService statusBarService = IStatusBarService.Stub.asInterface(ServiceManager.checkService(Context.STATUS_BAR_SERVICE));
        if (statusBarService != null) {
            try {
                statusBarService.restartUI();
            } catch (RemoteException e) {
                // do nothing.
            }
        }
        return true;
    }
}
