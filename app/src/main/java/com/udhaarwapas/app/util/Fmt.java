package com.udhaarwapas.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Rs formatting, date formatting aur WhatsApp ke liye phone number saaf karna. */
public class Fmt {

    /** Rs 12,500 — thousands separator ke saath, decimals nahi. */
    public static String rs(long amount) {
        return String.format(Locale.US, "Rs %,d", amount);
    }

    /** Stock/bill quantity: 5 -> "5", 2.5 -> "2.5", 2.25 -> "2.25". */
    public static String qty(double q) {
        if (q == Math.floor(q) && !Double.isInfinite(q)) {
            return String.format(Locale.US, "%,d", (long) q);
        }
        String s = String.format(Locale.US, "%.2f", q);
        while (s.contains(".") && (s.endsWith("0") || s.endsWith("."))) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public static String date(long millis) {
        return new SimpleDateFormat("dd MMM yyyy", Locale.US).format(new Date(millis));
    }

    /**
     * Pakistani number ko wa.me format me badlo: 03xx-xxxxxxx -> 923xxxxxxxxx.
     * Pehle se 92 wala ho to waisa hi; pehchan na sake to sirf digits wapas.
     */
    public static String waNumber(String raw) {
        if (raw == null) return "";
        String d = raw.replaceAll("[^0-9]", "");
        if (d.startsWith("0092")) d = d.substring(2);
        if (d.startsWith("0")) d = "92" + d.substring(1);
        if (d.length() == 10 && d.startsWith("3")) d = "92" + d;
        return d;
    }
}
