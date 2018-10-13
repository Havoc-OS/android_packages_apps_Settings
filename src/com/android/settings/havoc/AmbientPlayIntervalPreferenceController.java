/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 The PixelDust Project
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

package com.android.settings.havoc;

import android.content.Context;
import android.provider.Settings;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;

import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

import java.util.List;

import static android.provider.Settings.Secure.AMBIENT_RECOGNITION_INTERVAL;

import static com.android.internal.logging.nano.MetricsProto.MetricsEvent.ACTION_AMBIENT_DISPLAY;

public class AmbientPlayIntervalPreferenceController extends AbstractPreferenceController implements
        PreferenceControllerMixin, Preference.OnPreferenceChangeListener {

    private static final String KEY_AMBIENT_PLAY_INTERVAL = "ambient_display_ambient_play_interval";
    private ListPreference mAmbientPlayInterval;

    private final MetricsFeatureProvider mMetricsFeatureProvider;

    public AmbientPlayIntervalPreferenceController(Context context) {
        super(context);
        mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String getPreferenceKey() {
        return KEY_AMBIENT_PLAY_INTERVAL;
    }

    @Override
    public void updateState(Preference preference) {
        int value = Settings.Secure.getInt(mContext.getContentResolver(), AMBIENT_RECOGNITION_INTERVAL, 120000);
        ((ListPreference) preference).setValue(String.valueOf(value));
        updateSummary(preference, value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int value = Integer.parseInt((String) newValue);
        Settings.Secure.putInt(mContext.getContentResolver(),
                Settings.Secure.AMBIENT_RECOGNITION_INTERVAL, value);
        updateSummary(preference, value);
        return true;
    }

    @Override
    public void updateNonIndexableKeys(List<String> keys) {
        keys.add(getPreferenceKey());
    }

    private void updateSummary(Preference preference, int value) {
        if (value == 60000) {
            ((ListPreference) preference).setSummary(mContext.getResources().getString(R.string.ambient_recognition_interval_summary_one_minute));
        } else if ((value == 80000) || (value == 100000)) {
            ((ListPreference) preference).setSummary(mContext.getResources().getString(R.string.ambient_recognition_interval_summary_one_minute_seconds, (value - 60000) / 1000));
        } else if (value == 120000) {
            ((ListPreference) preference).setSummary(mContext.getResources().getString(R.string.ambient_recognition_interval_summary_two_minutes));
        } else {
            ((ListPreference) preference).setSummary(mContext.getResources().getString(R.string.ambient_recognition_interval_summary_seconds, value / 1000));
        }
    }
}
