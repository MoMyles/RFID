package com.pugongying.uhf.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.jar.Attributes;

public class PrefsUtil {
    private static final String NAME = "RFID_PRES";

    public static void set(Context context, String key, String value) {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String get(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getString(key, "");
    }

    public static String get(Context context, String key, String def) {
        SharedPreferences sp = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
        return sp.getString(key, def);
    }
}
