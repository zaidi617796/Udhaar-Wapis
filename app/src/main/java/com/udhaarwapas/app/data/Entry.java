package com.udhaarwapas.app.data;

/** Khata entry: ya udhaar diya (credit) ya payment mili (payment). Amount hamesha pure rupees (long). */
public class Entry {
    public static final int TYPE_UDHAAR = 1;
    public static final int TYPE_PAYMENT = 2;

    public long id;
    public long customerId;
    public int type;
    public long amount;
    public long date;      // epoch millis
    public String note;

    public Entry(long id, long customerId, int type, long amount, long date, String note) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }
}
