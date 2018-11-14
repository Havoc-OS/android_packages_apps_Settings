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
package com.android.settings.battery;

import android.content.Context;
import android.support.v7.preference.Preference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

import java.io.BufferedReader;
import java.io.FileReader;

public class BatteryTempPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin {

    private static final String KEY_BATTERY_TEMP = "battery_temp";
    private static final String BATTERY_TEMP_PATH = "sys/class/power_supply/battery/temp";
    private int BATTERY_TEMP_DIVIDER = 10;

    public BatteryTempPreferenceController(Context context) {
        super(context);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setSummary(getBatteryTemp());
    }

    private static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return null;
        }
        return line;
    }

    private String getBatteryTemp() {
        String value = readOneLine(BATTERY_TEMP_PATH);
        return String.format("%s", Integer.parseInt(value) / BATTERY_TEMP_DIVIDER) + "\u2103";
    }

    @Override
    public String getPreferenceKey() {
        return KEY_BATTERY_TEMP;
    }
}
