package com.udhaarwapas.app.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Ek sale bill. customerId 0 = walk-in (aam gahak).
 * customerName snapshot rakha hai taake customer delete ho jaye to bhi bill sahi dikhe.
 * total = subtotal - discount (discount subtotal se bara nahi ho sakta).
 */
public class Bill {
    public long id;
    public long number;          // Bill #1, #2, ...
    public long customerId;
    public String customerName;
    public long date;            // epoch millis
    public List<BillItem> items = new ArrayList<>();
    public long discount;
    public long total;
    public boolean onUdhaar;

    public Bill(long id, long number, long customerId, String customerName, long date,
                long discount, long total, boolean onUdhaar) {
        this.id = id;
        this.number = number;
        this.customerId = customerId;
        this.customerName = customerName;
        this.date = date;
        this.discount = discount;
        this.total = total;
        this.onUdhaar = onUdhaar;
    }

    public long subtotal() {
        long s = 0;
        for (BillItem it : items) s += it.amount();
        return s;
    }
}
