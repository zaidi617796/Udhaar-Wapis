package com.udhaarwapas.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.udhaarwapas.app.data.Customer;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Home screen: roz ka khulasa (total udhaar, aaj ki collection),
 * sab se bare baqaya, 30+ din wale customer, aur poori customer list.
 */
public class MainActivity extends AppCompatActivity {

    private KhataStore store;
    private LinearLayout content;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Lang.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new KhataStore(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(Ui.header(this, getString(R.string.app_name), getString(R.string.settings),
                v -> startActivity(new Intent(this, SettingsActivity.class))));

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 14);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);
        setContentView(root);
    }

    @Override
    protected void onResume() {
        super.onResume();
        store = new KhataStore(this);
        refresh();
    }

    private void refresh() {
        content.removeAllViews();

        // --- Hero: gradient header, dukaan ka naam + total udhaar + aaj ki wasooli ---
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setBackground(Ui.gradientBg(this, 18));
        hero.setPadding(Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18), Ui.dp(this, 18));
        String shop = store.getShopName();
        TextView shopTv = new TextView(this);
        shopTv.setText(shop.isEmpty() ? getString(R.string.app_name) : shop);
        shopTv.setTextColor(Ui.WHITE);
        shopTv.setTextSize(19);
        shopTv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hero.addView(shopTv);
        TextView tl = new TextView(this);
        tl.setText(getString(R.string.home_total_outstanding));
        tl.setTextColor(0xFFD9EBDF);
        tl.setTextSize(14);
        hero.addView(tl, Ui.matchWrap(this, 10, 0));
        TextView tv = new TextView(this);
        tv.setText(Fmt.rs(store.totalOutstanding()));
        tv.setTextColor(Ui.WHITE);
        tv.setTextSize(34);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        hero.addView(tv);
        TextView todayTv = new TextView(this);
        todayTv.setText(getString(R.string.home_today_collection) + ": "
                + Fmt.rs(store.todaysCollection()));
        todayTv.setTextColor(0xFFD9EBDF);
        todayTv.setTextSize(14);
        hero.addView(todayTv, Ui.matchWrap(this, 5, 0));
        content.addView(hero, Ui.matchWrap(this, 0, 16));

        // --- KHATA tiles (rangeen dashboard, DigiKhata tarz — rang hamare apne) ---
        content.addView(sectionTitle(getString(R.string.dash_khata)));
        content.addView(tileRow(new View[]{
                tile("👥", getString(R.string.tile_parties), Ui.GREEN,
                        v -> startActivity(new Intent(this, PartiesActivity.class))),
                tile("💰", getString(R.string.tile_cashbook), Ui.TILE_BLUE,
                        v -> startActivity(new Intent(this, CashBookActivity.class))),
                tile("🧾", getString(R.string.tile_bills), Ui.TILE_PURPLE,
                        v -> startActivity(new Intent(this, BillsActivity.class))),
        }));
        content.addView(tileRow(new View[]{
                tile("📦", getString(R.string.tile_stock), Ui.TILE_TEAL,
                        v -> startActivity(new Intent(this, StockActivity.class))),
                tile("💸", getString(R.string.tile_expense), Ui.TILE_BROWN,
                        v -> startActivity(new Intent(this, ExpenseActivity.class))),
                spacerTile(),
        }));
        content.addView(spacer(16));

        // --- Sab se bare baqaya ---
        List<Customer> debtors = new ArrayList<>();
        for (Customer c : store.getCustomers()) {
            if (store.balanceOf(c.id) > 0) debtors.add(c);
        }
        java.util.Collections.sort(debtors, (a, b) -> Long.compare(store.balanceOf(b.id), store.balanceOf(a.id)));
        if (!debtors.isEmpty()) {
            content.addView(sectionTitle(getString(R.string.home_big_debtors)));
            int n = Math.min(5, debtors.size());
            for (int i = 0; i < n; i++) {
                content.addView(customerRow(debtors.get(i), false));
            }
            content.addView(spacer(14));
        }

        // --- 30+ din se payment nahi ---
        List<Customer> overdue = new ArrayList<>();
        for (Customer c : store.getCustomers()) {
            if (store.isOverdue30(c.id)) overdue.add(c);
        }
        if (!overdue.isEmpty()) {
            content.addView(sectionTitle(getString(R.string.home_overdue_title)));
            for (Customer c : overdue) {
                content.addView(customerRow(c, true));
            }
            content.addView(spacer(14));
        }

        // --- Saare customers ---
        content.addView(sectionTitle(getString(R.string.home_customers)));
        android.widget.Button add = Ui.bigButton(this, getString(R.string.home_add_customer), Ui.GREEN);
        add.setOnClickListener(v -> showCustomerDialog(null));
        content.addView(add, Ui.matchWrap(this, 2, 10));

        if (store.getCustomers().isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.home_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 16, 0));
        } else {
            List<Customer> all = new ArrayList<>(store.getCustomers());
            java.util.Collections.sort(all, (a, b) -> a.name.compareToIgnoreCase(b.name));
            for (Customer c : all) {
                content.addView(customerRow(c, store.isOverdue30(c.id)));
            }
        }
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(Ui.cardBg(this));
        card.setPadding(Ui.dp(this, 16), Ui.dp(this, 12), Ui.dp(this, 16), Ui.dp(this, 12));
        return card;
    }

    /** Dashboard ki ek rangeen tile: safed card, rang-daar icon, bold label. */
    private View tile(String glyph, String label, int accent, View.OnClickListener onClick) {
        LinearLayout t = new LinearLayout(this);
        t.setOrientation(LinearLayout.VERTICAL);
        t.setGravity(Gravity.CENTER);
        t.setBackground(Ui.cardBg(this));
        t.setPadding(Ui.dp(this, 8), Ui.dp(this, 14), Ui.dp(this, 8), Ui.dp(this, 14));

        TextView icon = new TextView(this);
        icon.setText(glyph);
        icon.setTextSize(26);
        icon.setGravity(Gravity.CENTER);
        icon.setBackground(Ui.softBg(this, accent, 14));
        int box = Ui.dp(this, 52);
        icon.setLayoutParams(new LinearLayout.LayoutParams(box, box));
        t.addView(icon);

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(14);
        lbl.setTextColor(Ui.TEXT);
        lbl.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        lbl.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        llp.topMargin = Ui.dp(this, 8);
        t.addView(lbl, llp);

        t.setOnClickListener(onClick);
        return t;
    }

    /** Tiles ki ek kataar (3 tiles, barabar wazan, darmiyan fasla). */
    private View tileRow(View[] tiles) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < tiles.length; i++) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            if (i > 0) lp.setMarginStart(Ui.dp(this, 5));
            if (i < tiles.length - 1) lp.setMarginEnd(Ui.dp(this, 5));
            row.addView(tiles[i], lp);
        }
        LinearLayout.LayoutParams rlp = Ui.matchWrap(this, 0, 10);
        row.setLayoutParams(rlp);
        return row;
    }

    /** Dusri kataar me tile ka size barabar rakhne ke liye khaali jagah. */
    private View spacerTile() {
        View v = new View(this);
        v.setVisibility(View.INVISIBLE);
        return v;
    }

    private TextView sectionTitle(String text) {
        TextView t = new TextView(this);
        t.setText(text);
        t.setTextSize(16);
        t.setTextColor(Ui.TEXT);
        t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 8));
        return t;
    }

    private View spacer(int hDp) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(this, hDp)));
        return v;
    }

    /** Ek customer ki row: naam + balance (lene hain laal, dene hain hara, barabar muted) + overdue chip. */
    private View customerRow(Customer c, boolean overdueChip) {
        LinearLayout row = card();
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView name = new TextView(this);
        name.setText(c.name);
        name.setTextSize(17);
        name.setTextColor(Ui.TEXT);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        left.addView(name);
        if (!c.phone.isEmpty()) {
            TextView ph = new TextView(this);
            ph.setText(c.phone);
            ph.setTextSize(13);
            ph.setTextColor(Ui.MUTED);
            left.addView(ph);
        }
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);
        long bal = store.balanceOf(c.id);
        TextView amt = new TextView(this);
        amt.setText(Fmt.rs(Math.abs(bal)));
        amt.setTextSize(17);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        TextView word = new TextView(this);
        word.setTextSize(12);
        if (bal > 0) {
            amt.setTextColor(Ui.RED);
            word.setText(getString(R.string.balance_lene));
            word.setTextColor(Ui.RED);
        } else if (bal < 0) {
            amt.setTextColor(Ui.GREEN_DARK);
            word.setText(getString(R.string.balance_dene));
            word.setTextColor(Ui.GREEN_DARK);
        } else {
            amt.setTextColor(Ui.MUTED);
            word.setText(getString(R.string.balance_clear));
            word.setTextColor(Ui.MUTED);
        }
        right.addView(amt);
        right.addView(word);
        if (overdueChip) {
            TextView chip = new TextView(this);
            chip.setText(getString(R.string.chip_overdue));
            chip.setTextSize(11);
            chip.setTextColor(Ui.WHITE);
            chip.setBackground(Ui.roundBg(this, Ui.RED, 6));
            chip.setPadding(Ui.dp(this, 6), Ui.dp(this, 2), Ui.dp(this, 6), Ui.dp(this, 2));
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            clp.topMargin = Ui.dp(this, 4);
            right.addView(chip, clp);
        }
        row.addView(right);

        row.setOnClickListener(v -> {
            Intent i = new Intent(this, CustomerActivity.class);
            i.putExtra("customer_id", c.id);
            startActivity(i);
        });
        row.setOnLongClickListener(v -> {
            showCustomerOptions(c);
            return true;
        });

        LinearLayout.LayoutParams lp = Ui.matchWrap(this, 0, 8);
        row.setLayoutParams(lp);
        return row;
    }

    private void showCustomerOptions(Customer c) {
        new AlertDialog.Builder(this)
                .setTitle(c.name)
                .setItems(new CharSequence[]{getString(R.string.edit), getString(R.string.delete)},
                        (d, which) -> {
                            if (which == 0) showCustomerDialog(c);
                            else confirmDeleteCustomer(c);
                        })
                .show();
    }

    private void confirmDeleteCustomer(Customer c) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cust_delete_title))
                .setMessage(getString(R.string.cust_delete_msg, c.name))
                .setPositiveButton(getString(R.string.delete), (d, w) -> {
                    store.deleteCustomer(c.id);
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Add ya edit customer — ek hi form. existing null ho to naya. */
    public void showCustomerDialog(Customer existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        EditText nameIn = new EditText(this);
        nameIn.setHint(getString(R.string.cust_name_hint));
        nameIn.setTextSize(16);
        EditText phoneIn = new EditText(this);
        phoneIn.setHint(getString(R.string.cust_phone_hint));
        phoneIn.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneIn.setTextSize(16);
        if (existing != null) {
            nameIn.setText(existing.name);
            phoneIn.setText(existing.phone);
        }
        form.addView(Ui.label(this, getString(R.string.cust_name_label)));
        form.addView(nameIn);
        form.addView(Ui.label(this, getString(R.string.cust_phone_label)), Ui.matchWrap(this, 10, 0));
        form.addView(phoneIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(existing == null ? R.string.cust_add_title : R.string.cust_edit_title))
                .setView(form)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    String name = nameIn.getText().toString().trim();
                    String phone = phoneIn.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.err_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String digits = phone.replaceAll("[^0-9]", "");
                    if (!phone.isEmpty() && digits.length() < 10) {
                        Toast.makeText(this, R.string.err_phone, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (existing == null) store.addCustomer(name, phone);
                    else store.updateCustomer(existing, name, phone);
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
