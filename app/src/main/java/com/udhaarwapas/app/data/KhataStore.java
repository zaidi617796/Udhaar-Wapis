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
 * Keys: customers, entries, shop_name, owner_name, reminder_template, lang, next_id.
 */
public class KhataStore {

    private static final String PREFS = "UdhaarWapas";

    private final SharedPreferences prefs;
    private final List<Customer> customers = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();
    private long nextId;

    public KhataStore(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    // ---------- load / save ----------

    private void load() {
        customers.clear();
        entries.clear();
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
        } catch (Exception ignored) {
            // kharab data par app crash na ho — khaali khata se shuru
        }
        nextId = prefs.getLong("next_id", 1);
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
            prefs.edit()
                    .putString("customers", ca.toString())
                    .putString("entries", ea.toString())
                    .putLong("next_id", nextId)
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
