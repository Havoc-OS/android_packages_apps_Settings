/*
 * Copyright (C) 2018 CypherOS
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
 * limitations under the License
 */
package com.android.settings.havoc.wifi;

import android.annotation.DrawableRes;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.net.NetworkBadging;
import android.net.wifi.WifiConfiguration;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.settingslib.wifi.AccessPoint;


/* Preference that handles current signal level of the connected AP */
public class ConnectedAPSignalPreference extends Preference {

    private static final int[] STATE_SECURED = {
            com.android.settingslib.R.attr.state_encrypted
    };

    private static final int[] STATE_METERED = {
            com.android.settingslib.R.attr.state_metered
    };

    private static final int[] FRICTION_ATTRS = {
            com.android.settingslib.R.attr.wifi_friction
    };

    private final StateListDrawable mFrictionSld;
    private TextView mTitleView;
	
    private AccessPoint mAccessPoint;
    private int mLevel;

    private String[] mSignalStr;

    public static String generatePreferenceKey(AccessPoint accessPoint) {
        StringBuilder builder = new StringBuilder();

        if (TextUtils.isEmpty(accessPoint.getSsidStr())) {
            builder.append(accessPoint.getBssid());
        } else {
            builder.append(accessPoint.getSsidStr());
        }

        builder.append(',').append(accessPoint.getSecurity());
        return builder.toString();
    }

    public ConnectedAPSignalPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mFrictionSld = null;
    }

    public ConnectedAPSignalPreference(AccessPoint accessPoint, Context context) {
        super(context);
        setWidgetLayoutResource(com.android.settingslib.R.layout.access_point_friction_widget);
        mAccessPoint = accessPoint;
        mLevel = -1;

        TypedArray frictionSld;
        try {
            frictionSld = context.getTheme().obtainStyledAttributes(FRICTION_ATTRS);
        } catch (Resources.NotFoundException e) {
            frictionSld = null;
        }
        mFrictionSld = frictionSld != null ? (StateListDrawable) frictionSld.getDrawable(0) : null;

        mSignalStr = context.getResources().getStringArray(R.array.wifi_signal);
        refreshMeter();
    }

    public AccessPoint getAccessPoint() {
        return mAccessPoint;
    }

    @Override
    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        if (mAccessPoint == null) {
            return;
        }
        Drawable drawable = getIcon();
        if (drawable != null) {
            drawable.setLevel(mLevel);
        }

        mTitleView = (TextView) view.findViewById(android.R.id.title);
        ImageView frictionImageView = (ImageView) view.findViewById(com.android.settingslib.R.id.friction_icon);
        bindFrictionImage(frictionImageView);
    }

    protected void updateIcon(int level, Context context) {
        Drawable drawable = getContext().getResources().getDrawable(getMeter(level));
        if (drawable != null) {
            drawable.setTint(getContext().getResources().getColor(getMeterColor(level)));
            setIcon(drawable);
        }
    }

    /**
     * Binds the friction icon drawable using a StateListDrawable.
     *
     * <p>Friction icons will be rebound when notifyChange() is called, and therefore
     * do not need to be managed in refreshMeter()</p>.
     */
    private void bindFrictionImage(ImageView frictionImageView) {
        if (frictionImageView == null || mFrictionSld == null) {
            return;
        }
        if (mAccessPoint.getSecurity() != AccessPoint.SECURITY_NONE) {
            mFrictionSld.setState(STATE_SECURED);
        } else if (mAccessPoint.isMetered()) {
            mFrictionSld.setState(STATE_METERED);
        }
        Drawable drawable = mFrictionSld.getCurrent();
        frictionImageView.setImageDrawable(drawable);
    }

    public void refreshMeter() {
        setTitle(this, mAccessPoint);
        final Context context = getContext();
        int level = mAccessPoint.getLevel();
        if (level != mLevel) {
            mLevel = level;
            updateIcon(mLevel, context);
            notifyChanged();
        }

        setSummary(getContext().getResources().getString(R.string.connected_signal_pref_summary));
    }

    @Override
    protected void notifyChanged() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            postNotifyChanged();
        } else {
            super.notifyChanged();
        }
    }

    @VisibleForTesting
    public void setTitle(ConnectedAPSignalPreference preference, AccessPoint ap) {
        int summarySignalLevel = ap.getLevel();
        preference.setTitle(String.format(getContext().getResources().getString(
                   R.string.connected_signal_pref_title), mSignalStr[summarySignalLevel]));
    }

    public void onLevelChanged() {
        postNotifyChanged();
    }

    private void postNotifyChanged() {
        if (mTitleView != null) {
            mTitleView.post(mNotifyChanged);
		}
    }

    private final Runnable mNotifyChanged = new Runnable() {
        @Override
        public void run() {
            notifyChanged();
        }
    };

    @DrawableRes
    private int getMeter(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return R.drawable.ic_settings_wifi_meter_poor_fair; // Poor 0
            case 1:
                return R.drawable.ic_settings_wifi_meter_poor_fair; // Poor 1
            case 2:
                return R.drawable.ic_settings_wifi_meter_poor_fair; // Fair 2
            case 3:
                return R.drawable.ic_settings_wifi_meter_good_excellent; // Good 3
            case 4:
                return R.drawable.ic_settings_wifi_meter_good_excellent; // Excellent 4
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }

    @DrawableRes
    private int getMeterColor(int signalLevel) {
        switch (signalLevel) {
            case 0:
                return R.color.ic_signal_meter_poor; // Poor 0
            case 1:
                return R.color.ic_signal_meter_poor; // Poor 1
            case 2:
                return R.color.ic_signal_meter_fair; // Fair 2
            case 3:
                return R.color.ic_signal_meter_good_excellent; // Good 3
            case 4:
                return R.color.ic_signal_meter_good_excellent; // Excellent 4
            default:
                throw new IllegalArgumentException("Invalid signal level: " + signalLevel);
        }
    }
}
