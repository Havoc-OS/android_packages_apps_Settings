/*
 * Copyright (C) 2022 Havoc-OS
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

package com.android.settings.deviceinfo.aboutphone;

import android.content.Context;
import android.os.SystemProperties;
import android.widget.TextView;
import androidx.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.widget.LayoutPreference;

public class HavocHeaderPreferenceController extends BasePreferenceController {

    private static final String KEY_VERSION_PROP = "ro.havoc.version";
    private static final String KEY_BUILD_DATE_PROP = "ro.havoc.build.date";

    public HavocHeaderPreferenceController(Context context, String str) {
        super(context, str);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        String summary = mContext.getString(R.string.havoc_version_summary);
        String version = SystemProperties.get(KEY_VERSION_PROP, mContext.getString(R.string.unknown));
        String date = SystemProperties.get(KEY_BUILD_DATE_PROP, mContext.getString(R.string.unknown));
        LayoutPreference layoutPreference = (LayoutPreference) preference;
        ((TextView) layoutPreference.findViewById(R.id.status_summary)).setText(String.format(summary, version, date));
    }
}