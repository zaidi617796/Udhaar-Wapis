package com.udhaarwapas.app;

import android.app.DatePickerDialog;
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

import com.udhaarwapas.app.data.CashEntry;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

import java.util.Calendar;
import java.util.List;

/**
 * Cash book: roz ka cash aaya / gaya, haath me cash ka balance,
 * aaj ke in/out ke total. Entry long-press par delete.
 */
public class CashBookActivity extends AppCompatActivity {

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
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.tile_cashbook), null, null);
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

        // --- Cash balance card ---
        LinearLayout balCard = new LinearLayout(this);
        balCard.setOrientation(LinearLayout.VERTICAL);
        balCard.setBackground(Ui.roundBg(this, Ui.TILE_BLUE, 14));
        balCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        TextView bl = new TextView(this);
        bl.setText(getString(R.string.cash_balance));
        bl.setTextColor(0xFFD6E6F7);
        bl.setTextSize(14);
        balCard.addView(bl);
        TextView bv = new TextView(this);
        bv.setText(Fmt.rs(store.cashBalance()));
        bv.setTextColor(Ui.WHITE);
        bv.setTextSize(30);
        bv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        balCard.addView(bv);
        TextView today = new TextView(this);
        today.setText(getString(R.string.cash_today_in) + ": " + Fmt.rs(store.cashTodayIn())
                + "   ·   " + getString(R.string.cash_today_out) + ": " + Fmt.rs(store.cashTodayOut()));
        today.setTextColor(0xFFD6E6F7);
        today.setTextSize(13);
        balCard.addView(today);
        content.addView(balCard, Ui.matchWrap(this, 0, 12));

        // --- Add buttons ---
        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        Button bIn = Ui.bigButton(this, getString(R.string.cash_add_in), Ui.GREEN);
        bIn.setOnClickListener(v -> showEntryDialog(CashEntry.TYPE_IN));
        Button bOut = Ui.bigButton(this, getString(R.string.cash_add_out), Ui.RED);
        bOut.setOnClickListener(v -> showEntryDialog(CashEntry.TYPE_OUT));
        LinearLayout.LayoutParams half1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        half1.setMarginEnd(Ui.dp(this, 6));
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        half2.setMarginStart(Ui.dp(this, 6));
        addRow.addView(bIn, half1);
        addRow.addView(bOut, half2);
        content.addView(addRow, Ui.matchWrap(this, 0, 14));

        // --- Entries ---
        List<CashEntry> list = store.getCashEntries();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.cash_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 10, 0));
        } else {
            for (CashEntry e : list) content.addView(entryRow(e));
        }
    }

    private View entryRow(CashEntry e) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        boolean in = e.type == CashEntry.TYPE_IN;
        TextView type = new TextView(this);
        type.setText(getString(in ? R.string.cash_in : R.string.cash_out));
        type.setTextSize(15);
        type.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        type.setTextColor(in ? Ui.GREEN_DARK : Ui.RED);
        left.addView(type);
        TextView sub = new TextView(this);
        String s = Fmt.date(e.date);
        if (!e.note.isEmpty()) s += " · " + e.note;
        sub.setText(s);
        sub.setTextSize(13);
        sub.setTextColor(Ui.MUTED);
        left.addView(sub);
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView amt = new TextView(this);
        amt.setText((in ? "+ " : "− ") + Fmt.rs(e.amount));
        amt.setTextSize(16);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amt.setTextColor(in ? Ui.GREEN_DARK : Ui.RED);
        row.addView(amt);

        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.entry_delete_title))
                    .setPositiveButton(getString(R.string.delete), (d, w) -> {
                        store.deleteCashEntry(e.id);
                        refresh();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    /** Cash in/out entry ka form (amount, date, note) — CustomerActivity wali hi tarz. */
    private void showEntryDialog(int type) {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        EditText amountIn = new EditText(this);
        amountIn.setHint(getString(R.string.entry_amount_hint));
        amountIn.setInputType(InputType.TYPE_CLASS_NUMBER);
        amountIn.setTextSize(18);

        final long[] chosenDate = {System.currentTimeMillis()};
        Button dateBtn = Ui.outlineButton(this,
                getString(R.string.entry_date_label) + ": " + Fmt.date(chosenDate[0]), Ui.GREEN_DARK);
        dateBtn.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(chosenDate[0]);
            new DatePickerDialog(this, (view, y, m, d) -> {
                Calendar c2 = Calendar.getInstance();
                c2.set(y, m, d, 12, 0, 0);
                chosenDate[0] = c2.getTimeInMillis();
                dateBtn.setText(getString(R.string.entry_date_label) + ": " + Fmt.date(chosenDate[0]));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        EditText noteIn = new EditText(this);
        noteIn.setHint(getString(R.string.cash_note_hint));
        noteIn.setTextSize(16);

        form.addView(Ui.label(this, getString(R.string.entry_amount_label)));
        form.addView(amountIn);
        form.addView(dateBtn, Ui.matchWrap(this, 12, 0));
        form.addView(Ui.label(this, getString(R.string.entry_note_label)), Ui.matchWrap(this, 12, 0));
        form.addView(noteIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(type == CashEntry.TYPE_IN ? R.string.cash_add_in : R.string.cash_add_out))
                .setView(form)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    long amt;
                    try {
                        amt = Long.parseLong(amountIn.getText().toString().trim());
                    } catch (Exception ex) {
                        amt = 0;
                    }
                    if (amt <= 0) {
                        Toast.makeText(this, R.string.err_amount, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    store.addCashEntry(type, amt, chosenDate[0], noteIn.getText().toString());
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
