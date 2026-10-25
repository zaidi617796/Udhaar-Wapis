package com.udhaarwapas.app;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.data.StockItem;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

import java.util.List;

/**
 * Stock screen: saada inventory. Item par tap = stock in/out,
 * kam stock wali shay laal nishaan ke saath. Long-press = edit/delete.
 */
public class StockActivity extends AppCompatActivity {

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
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.tile_stock), null, null);
        header.setOnClickListener(v -> finish());
        root.addView(header);

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

        Button add = Ui.bigButton(this, getString(R.string.bills_add_item), Ui.TILE_TEAL);
        add.setOnClickListener(v -> showItemDialog(null));
        content.addView(add, Ui.matchWrap(this, 2, 12));

        List<StockItem> list = store.getStockItems();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.stock_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 10, 0));
        } else {
            for (StockItem s : list) content.addView(stockRow(s));
        }
    }

    private View stockRow(StockItem s) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView nm = new TextView(this);
        nm.setText(s.name);
        nm.setTextSize(16);
        nm.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nm.setTextColor(Ui.TEXT);
        left.addView(nm);
        if (s.isLow()) {
            TextView low = new TextView(this);
            low.setText(getString(R.string.stock_low));
            low.setTextSize(12);
            low.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            low.setTextColor(Ui.RED);
            left.addView(low);
        }
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView qty = new TextView(this);
        qty.setText(Fmt.qty(s.qty) + (s.unit.isEmpty() ? "" : " " + s.unit));
        qty.setTextSize(18);
        qty.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        qty.setTextColor(s.isLow() ? Ui.RED : Ui.GREEN_DARK);
        row.addView(qty);

        row.setOnClickListener(v -> showAdjustDialog(s));
        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(s.name)
                    .setItems(new CharSequence[]{getString(R.string.edit), getString(R.string.delete)},
                            (d, which) -> {
                                if (which == 0) showItemDialog(s);
                                else confirmDelete(s);
                            })
                    .show();
            return true;
        });
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    private void confirmDelete(StockItem s) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.stock_delete_title))
                .setMessage(getString(R.string.stock_delete_msg, s.name))
                .setPositiveButton(getString(R.string.delete), (d, w) -> {
                    store.deleteStockItem(s.id);
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Stock in/out: raqam likho, In ya Out dabao. */
    private void showAdjustDialog(StockItem s) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        TextView cur = new TextView(this);
        cur.setText(getString(R.string.stock_qty_label) + ": " + Fmt.qty(s.qty)
                + (s.unit.isEmpty() ? "" : " " + s.unit));
        cur.setTextSize(15);
        cur.setTextColor(Ui.MUTED);
        form.addView(cur);

        EditText amtIn = new EditText(this);
        amtIn.setHint("0");
        amtIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        amtIn.setTextSize(18);
        form.addView(Ui.label(this, getString(R.string.bills_qty)), Ui.matchWrap(this, 10, 0));
        form.addView(amtIn);

        new AlertDialog.Builder(this)
                .setTitle(s.name)
                .setView(form)
                .setPositiveButton(getString(R.string.stock_in), (d, w) -> {
                    double amt = parseQty(amtIn);
                    if (amt <= 0) {
                        Toast.makeText(this, R.string.bills_err_qty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.adjustStock(s.id, amt);
                    refresh();
                })
                .setNegativeButton(getString(R.string.stock_out), (d, w) -> {
                    double amt = parseQty(amtIn);
                    if (amt <= 0) {
                        Toast.makeText(this, R.string.bills_err_qty, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    StockItem fresh = store.getStockItem(s.id);
                    if (fresh != null && amt > fresh.qty) {
                        Toast.makeText(this, R.string.err_stock_out, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.adjustStock(s.id, -amt);
                    refresh();
                })
                .setNeutralButton(getString(R.string.cancel), null)
                .show();
    }

    /** Add ya edit item: naam, unit, miqdaar, low-stock had. */
    private void showItemDialog(StockItem existing) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        EditText nameIn = new EditText(this);
        nameIn.setHint(getString(R.string.bills_item_name_hint));
        nameIn.setTextSize(16);
        EditText unitIn = new EditText(this);
        unitIn.setHint(getString(R.string.stock_unit_hint));
        unitIn.setTextSize(16);
        EditText qtyIn = new EditText(this);
        qtyIn.setHint("0");
        qtyIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qtyIn.setTextSize(16);
        EditText lowIn = new EditText(this);
        lowIn.setHint("5");
        lowIn.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        lowIn.setTextSize(16);

        if (existing != null) {
            nameIn.setText(existing.name);
            unitIn.setText(existing.unit);
            qtyIn.setText(Fmt.qty(existing.qty));
            lowIn.setText(Fmt.qty(existing.lowThreshold));
        }

        form.addView(Ui.label(this, getString(R.string.bills_item_name)));
        form.addView(nameIn);
        form.addView(Ui.label(this, getString(R.string.stock_unit_label)), Ui.matchWrap(this, 10, 0));
        form.addView(unitIn);
        form.addView(Ui.label(this, getString(R.string.stock_qty_label)), Ui.matchWrap(this, 10, 0));
        form.addView(qtyIn);
        form.addView(Ui.label(this, getString(R.string.stock_threshold_label)), Ui.matchWrap(this, 10, 0));
        form.addView(lowIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(existing == null ? R.string.bills_add_item : R.string.stock_edit_title))
                .setView(form)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    String name = nameIn.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(this, R.string.err_name, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    double qty = parseQty(qtyIn);
                    double low = lowIn.getText().toString().trim().isEmpty() ? 5 : parseQty(lowIn);
                    if (existing == null) {
                        store.addStockItem(name, unitIn.getText().toString(), Math.max(0, qty), low);
                    } else {
                        store.updateStockItem(existing, name, unitIn.getText().toString(),
                                Math.max(0, qty), low);
                    }
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private double parseQty(EditText in) {
        try {
            return Double.parseDouble(in.getText().toString().trim());
        } catch (Exception ex) {
            return 0;
        }
    }
}
