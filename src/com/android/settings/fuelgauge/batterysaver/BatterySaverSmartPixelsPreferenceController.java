/*
 * Copyright (C) 2021 WaveOS
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

package com.android.settings.fuelgauge.batterysaver;

import static android.provider.Settings.System.SMART_PIXELS_ON_POWER_SAVE;

import android.content.Context;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;

public class BatterySaverSmartPixelsPreferenceController extends TogglePreferenceController {

    public BatterySaverSmartPixelsPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public boolean isChecked() {
        int val = Settings.System.getInt(mContext.getContentResolver(), SMART_PIXELS_ON_POWER_SAVE, 0);
        return val == 1;
    }

    @Override
    public boolean setChecked(boolean isChecked) {
        int val = isChecked ? 1 : 0;
        return Settings.System.putInt(mContext.getContentResolver(), SMART_PIXELS_ON_POWER_SAVE, val);
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getResources().getBoolean(com.android.internal.R.bool.config_enableSmartPixels)
                        ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }
}
