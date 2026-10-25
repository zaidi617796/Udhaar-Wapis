package com.udhaarwapas.app.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

/**
 * Zaban (EN/UR) ka simple system: SharedPreferences me choice, har Activity
 * attachBaseContext me wrap karti hai. Default = Urdu (target user dukaan-daar hai).
 */
public class Lang {

    public static String current(Context ctx) {
        SharedPreferences p = ctx.getSharedPreferences("UdhaarWapas", Context.MODE_PRIVATE);
        String lang = p.getString("lang", null);
        return lang != null ? lang : "ur";
    }

    public static Context wrap(Context base) {
        Locale locale = new Locale(current(base));
        Locale.setDefault(locale);
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.setLocale(locale);
        config.setLayoutDirection(locale);
        return base.createConfigurationContext(config);
    }
}
