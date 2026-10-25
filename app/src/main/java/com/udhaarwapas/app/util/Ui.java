package com.udhaarwapas.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Teeno screens ke shared UI tukre: hara header, safed card, bare buttons. Green/white theme. */
public class Ui {

    public static final int GREEN = Color.parseColor("#1B7A43");
    public static final int GREEN_DARK = Color.parseColor("#145C32");
    public static final int GREEN_SOFT = Color.parseColor("#E7F4EC");
    public static final int WHITE = Color.WHITE;
    public static final int TEXT = Color.parseColor("#14231A");
    public static final int MUTED = Color.parseColor("#5B6B60");
    public static final int RED = Color.parseColor("#C0392B");
    public static final int CARD_BORDER = Color.parseColor("#D8E6DC");

    public static int dp(Context c, int v) {
        return (int) (v * c.getResources().getDisplayMetrics().density + 0.5f);
    }

    public static GradientDrawable roundBg(Context c, int color, int radiusDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radiusDp));
        return g;
    }

    public static GradientDrawable cardBg(Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(WHITE);
        g.setCornerRadius(dp(c, 14));
        g.setStroke(dp(c, 1), CARD_BORDER);
        return g;
    }

    /** Upar wala hara header: title left, chhota sa action text right (clickable). */
    public static LinearLayout header(Context c, String title, String actionText,
                                      android.view.View.OnClickListener action) {
        LinearLayout bar = new LinearLayout(c);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundColor(GREEN);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(dp(c, 16), dp(c, 14), dp(c, 16), dp(c, 14));

        TextView t = new TextView(c);
        t.setText(title);
        t.setTextSize(20);
        t.setTextColor(WHITE);
        t.setTypeface(t.getTypeface(), android.graphics.Typeface.BOLD);
        bar.addView(t, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        if (actionText != null) {
            TextView a = new TextView(c);
            a.setText(actionText);
            a.setTextSize(15);
            a.setTextColor(WHITE);
            a.setPadding(dp(c, 10), dp(c, 6), dp(c, 10), dp(c, 6));
            a.setBackground(roundBg(c, GREEN_DARK, 8));
            a.setOnClickListener(action);
            bar.addView(a);
        }
        return bar;
    }

    public static Button bigButton(Context c, String text, int bgColor) {
        Button b = new Button(c);
        b.setText(text);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(WHITE);
        b.setBackground(roundBg(c, bgColor, 10));
        b.setPadding(dp(c, 12), dp(c, 12), dp(c, 12), dp(c, 12));
        return b;
    }

    public static Button outlineButton(Context c, String text, int color) {
        Button b = new Button(c);
        b.setText(text);
        b.setTextSize(15);
        b.setAllCaps(false);
        b.setTextColor(color);
        GradientDrawable g = new GradientDrawable();
        g.setColor(WHITE);
        g.setCornerRadius(dp(c, 10));
        g.setStroke(dp(c, 1), color);
        b.setBackground(g);
        b.setPadding(dp(c, 12), dp(c, 10), dp(c, 12), dp(c, 10));
        return b;
    }

    public static TextView label(Context c, String text) {
        TextView t = new TextView(c);
        t.setText(text);
        t.setTextSize(14);
        t.setTextColor(MUTED);
        return t;
    }

    public static LinearLayout.LayoutParams matchWrap(Context c, int topDp, int bottomDp) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = dp(c, topDp);
        lp.bottomMargin = dp(c, bottomDp);
        return lp;
    }
}
