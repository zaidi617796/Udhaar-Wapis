package com.udhaarwapas.app;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.udhaarwapas.app.data.Expense;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

import java.util.Calendar;
import java.util.List;

/**
 * Kharcha screen: categories (bijli/gas, kiraya, chai-pani, transport, other),
 * oopar is month ka total, neeche is month ki entries. Long-press = delete.
 */
public class ExpenseActivity extends AppCompatActivity {

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
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.tile_expense), null, null);
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

        // --- Is month ka total card ---
        LinearLayout totalCard = new LinearLayout(this);
        totalCard.setOrientation(LinearLayout.VERTICAL);
        totalCard.setBackground(Ui.roundBg(this, Ui.TILE_BROWN, 14));
        totalCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        TextView tl = new TextView(this);
        tl.setText(getString(R.string.exp_month_total));
        tl.setTextColor(0xFFE8DCD6);
        tl.setTextSize(14);
        totalCard.addView(tl);
        TextView tv = new TextView(this);
        tv.setText(Fmt.rs(store.expenseThisMonth()));
        tv.setTextColor(Ui.WHITE);
        tv.setTextSize(30);
        tv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        totalCard.addView(tv);
        content.addView(totalCard, Ui.matchWrap(this, 0, 12));

        Button add = Ui.bigButton(this, getString(R.string.exp_add), Ui.GREEN);
        add.setOnClickListener(v -> showExpenseDialog());
        content.addView(add, Ui.matchWrap(this, 0, 12));

        List<Expense> list = store.expensesThisMonth();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.exp_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 10, 0));
        } else {
            for (Expense x : list) content.addView(expenseRow(x));
        }
    }

    private View expenseRow(Expense x) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView cat = new TextView(this);
        cat.setText(getString(Expense.nameRes(x.category)));
        cat.setTextSize(15);
        cat.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        cat.setTextColor(Ui.TEXT);
        left.addView(cat);
        TextView sub = new TextView(this);
        String s = Fmt.date(x.date);
        if (!x.note.isEmpty()) s += " · " + x.note;
        sub.setText(s);
        sub.setTextSize(13);
        sub.setTextColor(Ui.MUTED);
        left.addView(sub);
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView amt = new TextView(this);
        amt.setText("− " + Fmt.rs(x.amount));
        amt.setTextSize(16);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amt.setTextColor(Ui.RED);
        row.addView(amt);

        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.entry_delete_title))
                    .setPositiveButton(getString(R.string.delete), (d, w) -> {
                        store.deleteExpense(x.id);
                        refresh();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    /** Kharcha add karne ka form: category spinner, amount, date, note. */
    private void showExpenseDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        Spinner catSpin = new Spinner(this);
        String[] cats = new String[Expense.CAT_COUNT];
        for (int i = 0; i < Expense.CAT_COUNT; i++) cats[i] = getString(Expense.nameRes(i));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, cats);
        catSpin.setAdapter(adapter);

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
        noteIn.setHint(getString(R.string.entry_note_hint));
        noteIn.setTextSize(16);

        form.addView(Ui.label(this, getString(R.string.exp_category)));
        form.addView(catSpin);
        form.addView(Ui.label(this, getString(R.string.entry_amount_label)), Ui.matchWrap(this, 10, 0));
        form.addView(amountIn);
        form.addView(dateBtn, Ui.matchWrap(this, 12, 0));
        form.addView(Ui.label(this, getString(R.string.entry_note_label)), Ui.matchWrap(this, 12, 0));
        form.addView(noteIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.exp_add))
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
                    store.addExpense(catSpin.getSelectedItemPosition(), amt,
                            chosenDate[0], noteIn.getText().toString());
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
