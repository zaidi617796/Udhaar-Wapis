package com.udhaarwapas.app.data;

/** Cash book entry: ki cash aaya (IN) ya cash gaya (OUT). Amount pure rupees (long). */
public class CashEntry {
    public static final int TYPE_IN = 1;
    public static final int TYPE_OUT = 2;

    public long id;
    public int type;
    public long amount;
    public long date;      // epoch millis
    public String note;

    public CashEntry(long id, int type, long amount, long date, String note) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }
}
