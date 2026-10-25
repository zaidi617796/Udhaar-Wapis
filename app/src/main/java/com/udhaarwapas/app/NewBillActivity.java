package com.udhaarwapas.app;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.udhaarwapas.app.data.Bill;
import com.udhaarwapas.app.data.BillItem;
import com.udhaarwapas.app.data.Customer;
import com.udhaarwapas.app.data.Entry;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

/**
 * Naya bill banana: customer chuno (ya walk-in), line items joro,
 * discount lagao, save karo. Udhaar wala bill customer ke khate me
 * khud-ba-khud "Bill #n" entry ban jata hai.
 */
public class NewBillActivity extends AppCompatActivity {

    private KhataStore store;
    private Customer selected;                       // null = walk-in
    private final List<BillItem> items = new ArrayList<>();

    private Button customerBtn;
    private LinearLayout itemsBox;
    private EditText discountIn;
    private TextView subtotalTv;
    private TextView totalTv;
    private CheckBox udhaarCheck;

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
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.bills_new), null, null);
        header.setOnClickListener(v -> finish());
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 14);
        form.setPadding(pad, pad, pad, pad);
        scroll.addView(form, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        // Customer picker
        form.addView(Ui.label(this, getString(R.string.bills_customer_label)));
        customerBtn = Ui.outlineButton(this, getString(R.string.bills_walkin), Ui.GREEN_DARK);
        customerBtn.setOnClickListener(v -> pickCustomer());
        form.addView(customerBtn, Ui.matchWrap(this, 4, 0));

        // Items
        form.addView(Ui.sectionTitle(this, getString(R.string.bills_items)), Ui.matchWrap(this, 14, 0));
        itemsBox = new LinearLayout(this);
        itemsBox.setOrientation(LinearLayout.VERTICAL);
        form.addView(itemsBox);
        Button addItem = Ui.outlineButton(this, getString(R.string.bills_add_item), Ui.TILE_PURPLE);
        addItem.setOnClickListener(v -> showItemDialog());
        form.addView(addItem, Ui.matchWrap(this, 4, 0));

        // Discount + totals
        form.addView(Ui.label(this, getString(R.string.bills_discount)), Ui.matchWrap(this, 16, 0));
        discountIn = new EditText(this);
        discountIn.setHint("0");
        discountIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        discountIn.setTextSize(17);
        discountIn.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void afterTextChanged(Editable s) { updateTotals(); }
        });
        form.addView(discountIn);

        subtotalTv = totalLine();
        form.addView(subtotalTv, Ui.matchWrap(this, 12, 0));
        totalTv = totalLine();
        totalTv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        totalTv.setTextColor(Ui.GREEN_DARK);
        totalTv.setTextSize(19);
        form.addView(totalTv, Ui.matchWrap(this, 2, 0));

        // Udhaar checkbox (sirf customer ho to)
        udhaarCheck = new CheckBox(this);
        udhaarCheck.setText(getString(R.string.bills_on_udhaar));
        udhaarCheck.setTextSize(16);
        udhaarCheck.setEnabled(false);
        form.addView(udhaarCheck, Ui.matchWrap(this, 12, 0));

        Button save = Ui.bigButton(this, getString(R.string.bills_save), Ui.GREEN);
        save.setOnClickListener(v -> saveBill());
        form.addView(save, Ui.matchWrap(this, 16, 0));

        setContentView(root);
        refreshItems();
    }

    private TextView totalLine() {
        TextView t = new TextView(this);
        t.setTextSize(16);
        t.setTextColor(Ui.TEXT);
        return t;
    }

    private void pickCustomer() {
        List<Customer> customers = store.getCustomers();
        CharSequence[] names = new CharSequence[customers.size() + 1];
        names[0] = getString(R.string.bills_walkin);
        for (int i = 0; i < customers.size(); i++) names[i + 1] = customers.get(i).name;
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bills_select_customer))
                .setItems(names, (d, which) -> {
                    selected = which == 0 ? null : customers.get(which - 1);
                    customerBtn.setText(selected == null
                            ? getString(R.string.bills_walkin) : selected.name);
                    udhaarCheck.setEnabled(selected != null);
                    if (selected == null) udhaarCheck.setChecked(false);
                })
                .show();
    }

    private void refreshItems() {
        itemsBox.removeAllViews();
        if (items.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.bills_err_items));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(14);
            itemsBox.addView(empty, Ui.matchWrap(this, 2, 6));
        } else {
            for (BillItem it : items) itemsBox.addView(itemRow(it));
        }
        updateTotals();
    }

    private View itemRow(BillItem it) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView nm = new TextView(this);
        nm.setText(it.name);
        nm.setTextSize(15);
        nm.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nm.setTextColor(Ui.TEXT);
        left.addView(nm);
        TextView sub = new TextView(this);
        sub.setText(Fmt.qty(it.qty) + " × " + Fmt.rs(it.rate));
        sub.setTextSize(13);
        sub.setTextColor(Ui.MUTED);
        left.addView(sub);
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView amt = new TextView(this);
        amt.setText(Fmt.rs(it.amount()));
        amt.setTextSize(16);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amt.setTextColor(Ui.GREEN_DARK);
        row.addView(amt);

        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.entry_delete_title))
                    .setPositiveButton(getString(R.string.delete), (d, w) -> {
                        items.remove(it);
                        refreshItems();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    private void showItemDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        EditText nameIn = new EditText(this);
        nameIn.setHint(getString(R.string.bills_item_name_hint));
        nameIn.setTextSize(16);
        EditText qtyIn = new EditText(this);
        qtyIn.setHint("1");
        qtyIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qtyIn.setTextSize(16);
        EditText rateIn = new EditText(this);
        rateIn.setHint(getString(R.string.entry_amount_hint));
        rateIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        rateIn.setTextSize(16);

        form.addView(Ui.label(this, getString(R.string.bills_item_name)));
        form.addView(nameIn);
        form.addView(Ui.label(this, getString(R.string.bills_qty)), Ui.matchWrap(this, 10, 0));
        form.addView(qtyIn);
        form.addView(Ui.label(this, getString(R.string.bills_rate)), Ui.matchWrap(this, 10, 0));
        form.addView(rateIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bills_add_item))
                .setView(form)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    String name = nameIn.getText().toString().trim();
                    double qty;
                    long rate;
                    try {
                        qty = Double.parseDouble(qtyIn.getText().toString().trim());
                    } catch (Exception ex) {
                        qty = 0;
                    }
                    try {
                        rate = Long.parseLong(rateIn.getText().toString().trim());
                    } catch (Exception ex) {
                        rate = -1;
                    }
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.err_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (qty <= 0) {
                        Toast.makeText(this, R.string.bills_err_qty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (rate < 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    items.add(new BillItem(name, qty, rate));
                    refreshItems();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private long subtotal() {
        long s = 0;
        for (BillItem it : items) s += it.amount();
        return s;
    }

    private long discount() {
        try {
            return Long.parseLong(discountIn.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }

    private void updateTotals() {
        long sub = subtotal();
        long disc = Math.max(0, Math.min(discount(), sub));
        subtotalTv.setText(getString(R.string.bills_subtotal) + ": " + Fmt.rs(sub));
        totalTv.setText(getString(R.string.bills_total) + ": " + Fmt.rs(sub - disc));
    }

    private void saveBill() {
        if (items.isEmpty()) {
            Toast.makeText(this, R.string.bills_err_items, Toast.LENGTH_SHORT).show();
            return;
        }
        boolean udhaar = selected != null && udhaarCheck.isChecked();
        Bill bill = store.addBill(
                selected == null ? 0 : selected.id,
                selected == null ? "" : selected.name,
                items, discount(), udhaar, System.currentTimeMillis());
        if (udhaar) {
            // Khata integration: customer ke khate me udhaar entry "Bill #n"
            store.addEntry(selected.id, Entry.TYPE_UDHAAR, bill.total, bill.date,
                    getString(R.string.bills_bill_no, bill.number));
        }
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }
}
