package com.udhaarwapas.app.data;

/** Stock ki ek shay: naam, unit (piece/kg...), maujooda miqdaar, kam-stock ki had. */
public class StockItem {
    public long id;
    public String name;
    public String unit;
    public double qty;
    public double lowThreshold;   // is se kam ho to "low stock" (default 5)

    public StockItem(long id, String name, String unit, double qty, double lowThreshold) {
        this.id = id;
        this.name = name;
        this.unit = unit;
        this.qty = qty;
        this.lowThreshold = lowThreshold;
    }

    public boolean isLow() {
        return qty <= lowThreshold;
    }
}
