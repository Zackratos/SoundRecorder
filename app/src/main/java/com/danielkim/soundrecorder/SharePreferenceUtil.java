package com.danielkim.soundrecorder;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by IVY on 2017/2/9.
 */

public class SharePreferenceUtil {
    private static SharedPreferences getSharePreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharePreferences(context).edit();
    }

    public static int getBootCount(Context context) {
        return getSharePreferences(context).getInt("bootCount", 0);
    }

    public static void putBootCount(Context context, int bootCount) {
        getEditor(context).putInt("bootCount", bootCount).apply();
    }

    public static int getCurrent(Context context) {
        return getSharePreferences(context).getInt("current", 1);
    }

    public static void putCurrent(Context context, int current) {
        getEditor(context).putInt("current", current).apply();
    }
}
