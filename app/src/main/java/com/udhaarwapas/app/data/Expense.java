package com.udhaarwapas.app.data;

import com.udhaarwapas.app.R;

/** Kharcha entry: category + amount + date + note. Categories saadi si rakhi hain. */
public class Expense {
    public static final int CAT_UTILITY = 0;
    public static final int CAT_RENT = 1;
    public static final int CAT_CHAI = 2;
    public static final int CAT_TRANSPORT = 3;
    public static final int CAT_OTHER = 4;
    public static final int CAT_COUNT = 5;

    public long id;
    public int category;
    public long amount;
    public long date;      // epoch millis
    public String note;

    public Expense(long id, int category, long amount, long date, String note) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }

    /** Category ka string resource id (UI is se localized naam leti hai). */
    public static int nameRes(int category) {
        switch (category) {
            case CAT_UTILITY: return R.string.exp_cat_utility;
            case CAT_RENT: return R.string.exp_cat_rent;
            case CAT_CHAI: return R.string.exp_cat_chai;
            case CAT_TRANSPORT: return R.string.exp_cat_transport;
            default: return R.string.exp_cat_other;
        }
    }
}
