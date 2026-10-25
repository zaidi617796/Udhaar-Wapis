package com.udhaarwapas.app.data;

/** Bill ki ek line: shay ka naam, miqdaar (qty — kg ke liye decimal ho sakti hai), rate. */
public class BillItem {
    public String name;
    public double qty;
    public long rate;

    public BillItem(String name, double qty, long rate) {
        this.name = name;
        this.qty = qty;
        this.rate = rate;
    }

    public long amount() {
        return Math.round(qty * rate);
    }
}
