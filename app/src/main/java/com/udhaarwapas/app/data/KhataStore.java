package com.udhaarwapas.app.data;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Saara data phone par hi — SharedPreferences me JSON.
 * Koi account nahi, koi server nahi, internet ki zaroorat nahi.
 * Keys: customers, entries, cash_entries, expenses, bills, stock_items,
 * shop_name, owner_name, reminder_template, lang, next_id, next_bill_no.
 */
public class KhataStore {

    private static final String PREFS = "UdhaarWapas";

    private final SharedPreferences prefs;
    private final List<Customer> customers = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();
    private final List<CashEntry> cashEntries = new ArrayList<>();
    private final List<Expense> expenses = new ArrayList<>();
    private final List<Bill> bills = new ArrayList<>();
    private final List<StockItem> stockItems = new ArrayList<>();
    private long nextId;
    private long nextBillNo;

    public KhataStore(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    // ---------- load / save ----------

    private void load() {
        customers.clear();
        entries.clear();
        cashEntries.clear();
        expenses.clear();
        bills.clear();
        stockItems.clear();
        try {
            JSONArray ca = new JSONArray(prefs.getString("customers", "[]"));
            for (int i = 0; i < ca.length(); i++) {
                JSONObject o = ca.getJSONObject(i);
                customers.add(new Customer(
                        o.getLong("id"),
                        o.optString("name", ""),
                        o.optString("phone", ""),
                        o.optLong("created", 0)));
            }
            JSONArray ea = new JSONArray(prefs.getString("entries", "[]"));
            for (int i = 0; i < ea.length(); i++) {
                JSONObject o = ea.getJSONObject(i);
                entries.add(new Entry(
                        o.getLong("id"),
                        o.getLong("cid"),
                        o.getInt("type"),
                        o.getLong("amt"),
                        o.getLong("date"),
                        o.optString("note", "")));
            }
            JSONArray ce = new JSONArray(prefs.getString("cash_entries", "[]"));
            for (int i = 0; i < ce.length(); i++) {
                JSONObject o = ce.getJSONObject(i);
                cashEntries.add(new CashEntry(
                        o.getLong("id"),
                        o.getInt("type"),
                        o.getLong("amt"),
                        o.getLong("date"),
                        o.optString("note", "")));
            }
            JSONArray xa = new JSONArray(prefs.getString("expenses", "[]"));
            for (int i = 0; i < xa.length(); i++) {
                JSONObject o = xa.getJSONObject(i);
                expenses.add(new Expense(
                        o.getLong("id"),
                        o.getInt("cat"),
                        o.getLong("amt"),
                        o.getLong("date"),
                        o.optString("note", "")));
            }
            JSONArray ba = new JSONArray(prefs.getString("bills", "[]"));
            for (int i = 0; i < ba.length(); i++) {
                JSONObject o = ba.getJSONObject(i);
                Bill b = new Bill(
                        o.getLong("id"),
                        o.getLong("no"),
                        o.getLong("cid"),
                        o.optString("cname", ""),
                        o.getLong("date"),
                        o.optLong("disc", 0),
                        o.optLong("total", 0),
                        o.optBoolean("udhaar", false));
                JSONArray ia = o.optJSONArray("items");
                if (ia != null) {
                    for (int j = 0; j < ia.length(); j++) {
                        JSONObject io = ia.getJSONObject(j);
                        b.items.add(new BillItem(
                                io.optString("name", ""),
                                io.optDouble("qty", 0),
                                io.optLong("rate", 0)));
                    }
                }
                bills.add(b);
            }
            JSONArray sa = new JSONArray(prefs.getString("stock_items", "[]"));
            for (int i = 0; i < sa.length(); i++) {
                JSONObject o = sa.getJSONObject(i);
                stockItems.add(new StockItem(
                        o.getLong("id"),
                        o.optString("name", ""),
                        o.optString("unit", ""),
                        o.optDouble("qty", 0),
                        o.optDouble("low", 5)));
            }
        } catch (Exception ignored) {
            // kharab data par app crash na ho — khaali khata se shuru
        }
        nextId = prefs.getLong("next_id", 1);
        nextBillNo = prefs.getLong("next_bill_no", 1);
    }

    private void save() {
        try {
            JSONArray ca = new JSONArray();
            for (Customer c : customers) {
                JSONObject o = new JSONObject();
                o.put("id", c.id);
                o.put("name", c.name);
                o.put("phone", c.phone);
                o.put("created", c.createdAt);
                ca.put(o);
            }
            JSONArray ea = new JSONArray();
            for (Entry e : entries) {
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("cid", e.customerId);
                o.put("type", e.type);
                o.put("amt", e.amount);
                o.put("date", e.date);
                o.put("note", e.note);
                ea.put(o);
            }
            JSONArray ce = new JSONArray();
            for (CashEntry e : cashEntries) {
                JSONObject o = new JSONObject();
                o.put("id", e.id);
                o.put("type", e.type);
                o.put("amt", e.amount);
                o.put("date", e.date);
                o.put("note", e.note);
                ce.put(o);
            }
            JSONArray xa = new JSONArray();
            for (Expense x : expenses) {
                JSONObject o = new JSONObject();
                o.put("id", x.id);
                o.put("cat", x.category);
                o.put("amt", x.amount);
                o.put("date", x.date);
                o.put("note", x.note);
                xa.put(o);
            }
            JSONArray ba = new JSONArray();
            for (Bill b : bills) {
                JSONObject o = new JSONObject();
                o.put("id", b.id);
                o.put("no", b.number);
                o.put("cid", b.customerId);
                o.put("cname", b.customerName);
                o.put("date", b.date);
                o.put("disc", b.discount);
                o.put("total", b.total);
                o.put("udhaar", b.onUdhaar);
                JSONArray ia = new JSONArray();
                for (BillItem it : b.items) {
                    JSONObject io = new JSONObject();
                    io.put("name", it.name);
                    io.put("qty", it.qty);
                    io.put("rate", it.rate);
                    ia.put(io);
                }
                o.put("items", ia);
                ba.put(o);
            }
            JSONArray sa = new JSONArray();
            for (StockItem s : stockItems) {
                JSONObject o = new JSONObject();
                o.put("id", s.id);
                o.put("name", s.name);
                o.put("unit", s.unit);
                o.put("qty", s.qty);
                o.put("low", s.lowThreshold);
                sa.put(o);
            }
            prefs.edit()
                    .putString("customers", ca.toString())
                    .putString("entries", ea.toString())
                    .putString("cash_entries", ce.toString())
                    .putString("expenses", xa.toString())
                    .putString("bills", ba.toString())
                    .putString("stock_items", sa.toString())
                    .putLong("next_id", nextId)
                    .putLong("next_bill_no", nextBillNo)
                    .apply();
        } catch (Exception ignored) {
        }
    }

    private long newId() {
        return nextId++;
    }

    // ---------- customers ----------

    public List<Customer> getCustomers() {
        return customers;
    }

    public Customer getCustomer(long id) {
        for (Customer c : customers) if (c.id == id) return c;
        return null;
    }

    public Customer addCustomer(String name, String phone) {
        Customer c = new Customer(newId(), name.trim(), phone.trim(), System.currentTimeMillis());
        customers.add(c);
        save();
        return c;
    }

    public void updateCustomer(Customer c, String name, String phone) {
        c.name = name.trim();
        c.phone = phone.trim();
        save();
    }

    /** Customer delete = us ki saari entries bhi delete. Pehle UI confirm kare gi. */
    public void deleteCustomer(long id) {
        for (int i = customers.size() - 1; i >= 0; i--) {
            if (customers.get(i).id == id) customers.remove(i);
        }
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).customerId == id) entries.remove(i);
        }
        save();
    }

    // ---------- entries ----------

    /** Ek customer ki entries, nayi pehle (date descending). */
    public List<Entry> getEntries(long customerId) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : entries) if (e.customerId == customerId) out.add(e);
        java.util.Collections.sort(out, (a, b) -> Long.compare(b.date, a.date));
        return out;
    }

    public Entry addEntry(long customerId, int type, long amount, long date, String note) {
        Entry e = new Entry(newId(), customerId, type, amount, date, note == null ? "" : note.trim());
        entries.add(e);
        save();
        return e;
    }

    public void deleteEntry(long entryId) {
        for (int i = entries.size() - 1; i >= 0; i--) {
            if (entries.get(i).id == entryId) entries.remove(i);
        }
        save();
    }

    // ---------- hisaab (balances) ----------

    /** Balance > 0 matlab customer ke zimme lene hain; < 0 matlab dene hain. */
    public long balanceOf(long customerId) {
        long bal = 0;
        for (Entry e : entries) {
            if (e.customerId != customerId) continue;
            if (e.type == Entry.TYPE_UDHAAR) bal += e.amount;
            else bal -= e.amount;
        }
        return bal;
    }

    public long totalOutstanding() {
        long total = 0;
        for (Customer c : customers) {
            long b = balanceOf(c.id);
            if (b > 0) total += b;
        }
        return total;
    }

    /** Aaj (calendar day) mili hui payments ka total. */
    public long todaysCollection() {
        Calendar now = Calendar.getInstance();
        long sum = 0;
        for (Entry e : entries) {
            if (e.type != Entry.TYPE_PAYMENT) continue;
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(e.date);
            if (d.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && d.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                sum += e.amount;
            }
        }
        return sum;
    }

    /** Aakhri dafa is customer ne kab hiljul ki: latest payment, warna latest entry, warna add date. */
    public long lastActivityAt(long customerId) {
        long lastPay = 0;
        long lastAny = 0;
        for (Entry e : entries) {
            if (e.customerId != customerId) continue;
            if (e.date > lastAny) lastAny = e.date;
            if (e.type == Entry.TYPE_PAYMENT && e.date > lastPay) lastPay = e.date;
        }
        if (lastPay > 0) return lastPay;
        if (lastAny > 0) return lastAny;
        Customer c = getCustomer(customerId);
        return c != null ? c.createdAt : 0;
    }

    /** Balance baaki hai aur 30+ din se koi payment nahi aayi. */
    public boolean isOverdue30(long customerId) {
        if (balanceOf(customerId) <= 0) return false;
        long last = lastActivityAt(customerId);
        long days = (System.currentTimeMillis() - last) / (24L * 60 * 60 * 1000);
        return days >= 30;
    }

    // ---------- cash book ----------

    private static boolean sameDay(long a, long b) {
        Calendar x = Calendar.getInstance();
        x.setTimeInMillis(a);
        Calendar y = Calendar.getInstance();
        y.setTimeInMillis(b);
        return x.get(Calendar.YEAR) == y.get(Calendar.YEAR)
                && x.get(Calendar.DAY_OF_YEAR) == y.get(Calendar.DAY_OF_YEAR);
    }

    /** Cash entries, nayi pehle (date descending). */
    public List<CashEntry> getCashEntries() {
        List<CashEntry> out = new ArrayList<>(cashEntries);
        java.util.Collections.sort(out, (a, b) -> Long.compare(b.date, a.date));
        return out;
    }

    public CashEntry addCashEntry(int type, long amount, long date, String note) {
        CashEntry e = new CashEntry(newId(), type, amount, date, note == null ? "" : note.trim());
        cashEntries.add(e);
        save();
        return e;
    }

    public void deleteCashEntry(long id) {
        for (int i = cashEntries.size() - 1; i >= 0; i--) {
            if (cashEntries.get(i).id == id) cashEntries.remove(i);
        }
        save();
    }

    /** Haath me cash: saare IN minus saare OUT. */
    public long cashBalance() {
        long bal = 0;
        for (CashEntry e : cashEntries) {
            if (e.type == CashEntry.TYPE_IN) bal += e.amount;
            else bal -= e.amount;
        }
        return bal;
    }

    public long cashTodayIn() {
        long sum = 0;
        long now = System.currentTimeMillis();
        for (CashEntry e : cashEntries) {
            if (e.type == CashEntry.TYPE_IN && sameDay(e.date, now)) sum += e.amount;
        }
        return sum;
    }

    public long cashTodayOut() {
        long sum = 0;
        long now = System.currentTimeMillis();
        for (CashEntry e : cashEntries) {
            if (e.type == CashEntry.TYPE_OUT && sameDay(e.date, now)) sum += e.amount;
        }
        return sum;
    }

    // ---------- expenses ----------

    /** Kharchay, naye pehle (date descending). */
    public List<Expense> getExpenses() {
        List<Expense> out = new ArrayList<>(expenses);
        java.util.Collections.sort(out, (a, b) -> Long.compare(b.date, a.date));
        return out;
    }

    public Expense addExpense(int category, long amount, long date, String note) {
        Expense x = new Expense(newId(), category, amount, date, note == null ? "" : note.trim());
        expenses.add(x);
        save();
        return x;
    }

    public void deleteExpense(long id) {
        for (int i = expenses.size() - 1; i >= 0; i--) {
            if (expenses.get(i).id == id) expenses.remove(i);
        }
        save();
    }

    /** Isi calendar month ke kharchon ka total. */
    public long expenseThisMonth() {
        Calendar now = Calendar.getInstance();
        long sum = 0;
        for (Expense x : expenses) {
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(x.date);
            if (d.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && d.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                sum += x.amount;
            }
        }
        return sum;
    }

    /** Is month ki expenses (list screen ke liye). */
    public List<Expense> expensesThisMonth() {
        Calendar now = Calendar.getInstance();
        List<Expense> out = new ArrayList<>();
        for (Expense x : expenses) {
            Calendar d = Calendar.getInstance();
            d.setTimeInMillis(x.date);
            if (d.get(Calendar.YEAR) == now.get(Calendar.YEAR)
                    && d.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
                out.add(x);
            }
        }
        java.util.Collections.sort(out, (a, b) -> Long.compare(b.date, a.date));
        return out;
    }

    // ---------- bills ----------

    /** Bills, naye pehle (date descending). */
    public List<Bill> getBills() {
        List<Bill> out = new ArrayList<>(bills);
        java.util.Collections.sort(out, (a, b) -> Long.compare(b.date, a.date));
        return out;
    }

    public Bill getBill(long id) {
        for (Bill b : bills) if (b.id == id) return b;
        return null;
    }

    /** Naya bill: number khud assign hota hai (1, 2, 3...). Discount subtotal par capped. */
    public Bill addBill(long customerId, String customerName, List<BillItem> items,
                        long discount, boolean onUdhaar, long date) {
        Bill b = new Bill(newId(), nextBillNo++, customerId,
                customerName == null ? "" : customerName.trim(), date, 0, 0, onUdhaar);
        b.items.addAll(items);
        long sub = b.subtotal();
        b.discount = Math.max(0, Math.min(discount, sub));
        b.total = sub - b.discount;
        bills.add(b);
        save();
        return b;
    }

    // ---------- stock ----------

    public List<StockItem> getStockItems() {
        List<StockItem> out = new ArrayList<>(stockItems);
        java.util.Collections.sort(out, (a, b) -> a.name.compareToIgnoreCase(b.name));
        return out;
    }

    public StockItem getStockItem(long id) {
        for (StockItem s : stockItems) if (s.id == id) return s;
        return null;
    }

    public StockItem addStockItem(String name, String unit, double qty, double lowThreshold) {
        StockItem s = new StockItem(newId(), name.trim(), unit == null ? "" : unit.trim(),
                qty, lowThreshold);
        stockItems.add(s);
        save();
        return s;
    }

    public void updateStockItem(StockItem s, String name, String unit, double qty, double lowThreshold) {
        s.name = name.trim();
        s.unit = unit == null ? "" : unit.trim();
        s.qty = qty;
        s.lowThreshold = lowThreshold;
        save();
    }

    /** Stock in/out: delta positive = aaya, negative = gaya. Nateeja 0 se neeche nahi jata. */
    public void adjustStock(long id, double delta) {
        StockItem s = getStockItem(id);
        if (s == null) return;
        s.qty = Math.max(0, s.qty + delta);
        save();
    }

    public void deleteStockItem(long id) {
        for (int i = stockItems.size() - 1; i >= 0; i--) {
            if (stockItems.get(i).id == id) stockItems.remove(i);
        }
        save();
    }

    // ---------- settings ----------

    public String getShopName() {
        return prefs.getString("shop_name", "");
    }

    public void setShopName(String v) {
        prefs.edit().putString("shop_name", v.trim()).apply();
    }

    public String getOwnerName() {
        return prefs.getString("owner_name", "");
    }

    public void setOwnerName(String v) {
        prefs.edit().putString("owner_name", v.trim()).apply();
    }

    public String getReminderTemplate() {
        return prefs.getString("reminder_template", "");
    }

    public void setReminderTemplate(String v) {
        prefs.edit().putString("reminder_template", v).apply();
    }

    /** "en" ya "ur". Null matlab abhi tak choose nahi hui (default urdu). */
    public String getLang() {
        return prefs.getString("lang", null);
    }

    public void setLang(String v) {
        prefs.edit().putString("lang", v).apply();
    }
}
