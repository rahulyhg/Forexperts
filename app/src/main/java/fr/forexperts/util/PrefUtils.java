/*
 * Copyright 2014 Baptiste Robert All rights reserved.
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

package fr.forexperts.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Utilities and constants related to app preferences.
 */
public class PrefUtils  {

    /* EURUSD */
    public static final String PREF_THRESHOLD_EURUSD = "pref_threshold_eurusd";
    public static final String PREF_STOP_LOSS_EURUSD = "pref_stop_loss_eurusd";
    public static final String PREF_PRICE_0H_EURUSD = "pref_price_0h_eurusd";
    public static final String PREF_PRICE_1H_EURUSD = "pref_price_1h_eurusd";
    public static final String PREF_DIRECTION_EURUSD = "pref_direction_eurusd";
    public static final String PREF_ALERT_SET_FLAG_EURUSD = "pref_alert_set_flag_eurusd";
    public static final String PREF_IS_TRIG_FLAG_EURUSD = "pref_is_trig_flag_eurusd";
    public static final String PREF_TRIG_HOUR_EURUSD = "pref_trig_hour_eurusd";

    /* GBPUSD */
    public static final String PREF_THRESHOLD_GBPUSD = "pref_threshold_gbpusd";
    public static final String PREF_STOP_LOSS_GBPUSD = "pref_stop_loss_gbpusd";
    public static final String PREF_PRICE_0H_GBPUSD = "pref_price_0h_gbpusd";
    public static final String PREF_PRICE_1H_GBPUSD = "pref_price_1h_gbpusd";
    public static final String PREF_DIRECTION_GBPUSD = "pref_direction_gbpusd";
    public static final String PREF_ALERT_SET_FLAG_GBPUSD = "pref_alert_set_flag_gbpusd";
    public static final String PREF_IS_TRIG_FLAG_GBPUSD = "pref_is_trig_flag_gbpusd";
    public static final String PREF_TRIG_HOUR_GBPUSD = "pref_trig_hour_gbpusd";

    public static void setThreshold(final Context context, String cross, float threshold) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putFloat(PREF_THRESHOLD_GBPUSD, threshold).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putFloat(PREF_THRESHOLD_EURUSD, threshold).apply();
        }
    }

    public static void setStopLoss(final Context context, String cross, float stopLoss) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putFloat(PREF_STOP_LOSS_GBPUSD, stopLoss).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putFloat(PREF_STOP_LOSS_EURUSD, stopLoss).apply();
        }
    }

    public static void setPrice0h(final Context context, String cross, float price0h) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putFloat(PREF_PRICE_0H_GBPUSD, price0h).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putFloat(PREF_PRICE_0H_EURUSD, price0h).apply();
        }
    }

    public static void setPrice1h(final Context context, String cross, float price1h) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putFloat(PREF_PRICE_1H_GBPUSD, price1h).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putFloat(PREF_PRICE_1H_EURUSD, price1h).apply();
        }
    }

    public static void setDirection(final Context context, String cross, int direction) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putInt(PREF_DIRECTION_GBPUSD, direction).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putInt(PREF_DIRECTION_EURUSD, direction).apply();
        }
    }

    public static void setAlertSetFlag(final Context context, String cross, boolean alertSetFlag) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putBoolean(PREF_ALERT_SET_FLAG_GBPUSD, alertSetFlag).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putBoolean(PREF_ALERT_SET_FLAG_EURUSD, alertSetFlag).apply();
        }
    }

    public static void setIsTrigFlag(final Context context, String cross, boolean isTrigFlag) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putBoolean(PREF_IS_TRIG_FLAG_GBPUSD, isTrigFlag).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putBoolean(PREF_IS_TRIG_FLAG_EURUSD, isTrigFlag).apply();
        }
    }

    public static void setTrigHour(final Context context, String cross, int hour) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        if (cross.equals("GBPUSD=X")) {
            sp.edit().putInt(PREF_TRIG_HOUR_GBPUSD, hour).apply();
        } else if (cross.equals("EURUSD=X")) {
            sp.edit().putInt(PREF_TRIG_HOUR_EURUSD, hour).apply();
        }
    }

    public static float getThreshold(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        float threshold = 0f;
        if (cross.equals("GBPUSD=X")) {
            threshold = sp.getFloat(PREF_THRESHOLD_GBPUSD, 0f);
        } else if (cross.equals("EURUSD=X")) {
            threshold = sp.getFloat(PREF_THRESHOLD_EURUSD, 0f);
        }
        return threshold;
    }

    public static float getStopLoss(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        float stoploss = 0f;
        if (cross.equals("GBPUSD=X")) {
            stoploss = sp.getFloat(PREF_STOP_LOSS_GBPUSD, 0f);
        } else if (cross.equals("EURUSD=X")) {
            stoploss = sp.getFloat(PREF_STOP_LOSS_EURUSD, 0f);
        }
        return stoploss;
    }

    public static float getPrice0h(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        float price0h = 0f;
        if (cross.equals("GBPUSD=X")) {
            price0h = sp.getFloat(PREF_PRICE_0H_GBPUSD, 0f);
        } else if (cross.equals("EURUSD=X")) {
            price0h = sp.getFloat(PREF_PRICE_0H_EURUSD, 0f);
        }
        return price0h;
    }

    public static int getDirection(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int direction = 0;
        if (cross.equals("GBPUSD=X")) {
            direction =  sp.getInt(PREF_DIRECTION_GBPUSD, 0);
        } else if (cross.equals("EURUSD=X")) {
            direction =  sp.getInt(PREF_DIRECTION_EURUSD, 0);
        }
        return direction;
    }

    public static boolean getAlertSetFlag(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean alertSetFlag = false;
        if (cross.equals("GBPUSD=X")) {
            alertSetFlag = sp.getBoolean(PREF_ALERT_SET_FLAG_GBPUSD, false);
        } else if (cross.equals("EURUSD=X")) {
            alertSetFlag = sp.getBoolean(PREF_ALERT_SET_FLAG_EURUSD, false);
        }
        return alertSetFlag;
    }

    public static boolean getIsTrigFlag(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isTrigFlag = false;
        if (cross.equals("GBPUSD=X")) {
            isTrigFlag = sp.getBoolean(PREF_IS_TRIG_FLAG_GBPUSD, false);
        } else if (cross.equals("EURUSD=X")) {
            isTrigFlag = sp.getBoolean(PREF_IS_TRIG_FLAG_EURUSD, false);
        }
        return isTrigFlag;
    }

    public static int getTrigHour(final Context context, String cross) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        int trigHour = 0;
        if (cross.equals("GBPUSD=X")) {
            trigHour = sp.getInt(PREF_TRIG_HOUR_GBPUSD, 0);
        } else if (cross.equals("EURUSD=X")) {
            trigHour = sp.getInt(PREF_TRIG_HOUR_EURUSD, 0);
        }
        return trigHour;
    }

    public static void clearData(final Context context, String cross) {
        setAlertSetFlag(context, cross, false);
        setIsTrigFlag(context, cross, false);
        setPrice0h(context, cross, 0f);
        setPrice1h(context, cross, 0f);
        setDirection(context, cross, 0);
        setThreshold(context, cross, 0f);
        setStopLoss(context, cross, 0f);
    }

    public static void setUpAlert(final Context context, float price0h, float price1h, String cross) {
        if (price1h - price0h < 0) {
            PrefUtils.setDirection(context, cross, 1);
            PrefUtils.setThreshold(context, cross, UIUtils.round(price0h + 0.002f, 4));
            PrefUtils.setStopLoss(context, cross, UIUtils.round(price0h - 0.002f, 4));
        } else {
            PrefUtils.setDirection(context, cross, 0);
            PrefUtils.setThreshold(context, cross, UIUtils.round(price0h - 0.002f, 4));
            PrefUtils.setStopLoss(context, cross, UIUtils.round(price0h + 0.002f, 4));
        }
        PrefUtils.setAlertSetFlag(context, cross, true);
    }
}
