/*
 * Copyright (C) 2018 Havoc-OS
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

package com.android.settings.deviceinfo.havoc;

import android.content.Context;
import android.os.SystemProperties;
import android.os.UserManager;

import com.android.settings.R;

public class HavocDeviceDialogController {

    private static final String HAVOC_PROPERTY = "ro.havoc.device";
    private static final int HAVOC_DEVICE_VALUE_ID = R.id.havoc_device_value;
    private static final int HAVOC_DEVICE_LABEL_ID = R.id.havoc_device_label;

    private final HavocInfoDialogFragment mDialog;
    private final Context mContext;
    private final UserManager mUserManager;

    public HavocDeviceDialogController(HavocInfoDialogFragment dialog) {
        mDialog = dialog;
        mContext = dialog.getContext();
        mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
    }

    public void initialize() {

        mDialog.setText(HAVOC_DEVICE_VALUE_ID, SystemProperties.get(HAVOC_PROPERTY,
                mContext.getResources().getString(R.string.device_info_default)));
    }
}
