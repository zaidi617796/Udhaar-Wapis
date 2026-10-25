package com.udhaarwapas.app;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
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
import androidx.core.content.FileProvider;

import com.udhaarwapas.app.data.Customer;
import com.udhaarwapas.app.data.Entry;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.PdfStatement;
import com.udhaarwapas.app.util.Ui;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.List;

/**
 * Ek customer ka khata: balance, entries, udhaar/payment add,
 * WhatsApp reminder, PDF statement share.
 */
public class CustomerActivity extends AppCompatActivity {

    private KhataStore store;
    private Customer customer;
    private LinearLayout content;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Lang.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new KhataStore(this);
        long id = getIntent().getLongExtra("customer_id", -1);
        customer = store.getCustomer(id);
        if (customer == null) {
            finish();
            return;
        }

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.addView(Ui.header(this, "←  " + customer.name, getString(R.string.edit),
                v -> showEditDialog()));
        // header ke left hisse (naam) par click = wapas
        root.getChildAt(0).setOnClickListener(v -> finish());

        ScrollView scroll = new ScrollView(this);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 14);
        content.setPadding(pad, pad, pad, pad);
        scroll.addView(content, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);
        setContentView(root);
        refresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        store = new KhataStore(this);
        Customer fresh = store.getCustomer(customer.id);
        if (fresh == null) {
            finish();
            return;
        }
        customer = fresh;
        if (content != null) refresh();
    }

    private void refresh() {
        content.removeAllViews();
        long bal = store.balanceOf(customer.id);

        // --- Balance card ---
        LinearLayout balCard = new LinearLayout(this);
        balCard.setOrientation(LinearLayout.VERTICAL);
        balCard.setBackground(Ui.roundBg(this, Ui.GREEN, 14));
        balCard.setPadding(Ui.dp(this, 18), Ui.dp(this, 16), Ui.dp(this, 18), Ui.dp(this, 16));
        TextView bl = new TextView(this);
        bl.setText(bal > 0 ? getString(R.string.balance_lene)
                : bal < 0 ? getString(R.string.balance_dene)
                : getString(R.string.balance_clear));
        bl.setTextColor(0xFFD9EBDF);
        bl.setTextSize(15);
        balCard.addView(bl);
        TextView bv = new TextView(this);
        bv.setText(Fmt.rs(Math.abs(bal)));
        bv.setTextColor(Ui.WHITE);
        bv.setTextSize(30);
        bv.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        balCard.addView(bv);
        if (!customer.phone.isEmpty()) {
            TextView bp = new TextView(this);
            bp.setText(customer.phone);
            bp.setTextColor(0xFFD9EBDF);
            bp.setTextSize(14);
            balCard.addView(bp);
        }
        content.addView(balCard, Ui.matchWrap(this, 0, 12));

        // --- Add entry buttons ---
        LinearLayout addRow = new LinearLayout(this);
        addRow.setOrientation(LinearLayout.HORIZONTAL);
        Button bU = Ui.bigButton(this, getString(R.string.entry_add_udhaar), Ui.RED);
        bU.setOnClickListener(v -> showEntryDialog(Entry.TYPE_UDHAAR));
        Button bP = Ui.bigButton(this, getString(R.string.entry_add_payment), Ui.GREEN);
        bP.setOnClickListener(v -> showEntryDialog(Entry.TYPE_PAYMENT));
        LinearLayout.LayoutParams half1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        half1.setMarginEnd(Ui.dp(this, 6));
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        half2.setMarginStart(Ui.dp(this, 6));
        addRow.addView(bU, half1);
        addRow.addView(bP, half2);
        content.addView(addRow, Ui.matchWrap(this, 0, 10));

        // --- WhatsApp reminder ---
        Button bWa = Ui.bigButton(this, getString(R.string.btn_whatsapp_reminder), Ui.GREEN_DARK);
        bWa.setOnClickListener(v -> sendWhatsAppReminder());
        content.addView(bWa, Ui.matchWrap(this, 0, 8));

        // --- PDF buttons ---
        LinearLayout pdfRow = new LinearLayout(this);
        pdfRow.setOrientation(LinearLayout.HORIZONTAL);
        Button bPdf = Ui.outlineButton(this, getString(R.string.btn_pdf), Ui.GREEN_DARK);
        bPdf.setOnClickListener(v -> sharePdf(false));
        Button bWaPdf = Ui.outlineButton(this, getString(R.string.btn_whatsapp_pdf), Ui.GREEN_DARK);
        bWaPdf.setOnClickListener(v -> sharePdf(true));
        LinearLayout.LayoutParams h1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        h1.setMarginEnd(Ui.dp(this, 6));
        LinearLayout.LayoutParams h2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        h2.setMarginStart(Ui.dp(this, 6));
        pdfRow.addView(bPdf, h1);
        pdfRow.addView(bWaPdf, h2);
        content.addView(pdfRow, Ui.matchWrap(this, 0, 16));

        // --- Entries ---
        TextView et = new TextView(this);
        et.setText(getString(R.string.detail_entries));
        et.setTextSize(16);
        et.setTextColor(Ui.TEXT);
        et.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        et.setPadding(0, Ui.dp(this, 4), 0, Ui.dp(this, 8));
        content.addView(et);

        List<Entry> list = store.getEntries(customer.id);
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.entries_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 10, 0));
        } else {
            for (Entry e : list) content.addView(entryRow(e));
        }
    }

    private View entryRow(Entry e) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView type = new TextView(this);
        boolean udhaar = e.type == Entry.TYPE_UDHAAR;
        type.setText(getString(udhaar ? R.string.entry_udhaar : R.string.entry_payment));
        type.setTextSize(15);
        type.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        type.setTextColor(udhaar ? Ui.RED : Ui.GREEN_DARK);
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
        amt.setText((udhaar ? "+ " : "− ") + Fmt.rs(e.amount));
        amt.setTextSize(16);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amt.setTextColor(udhaar ? Ui.RED : Ui.GREEN_DARK);
        row.addView(amt);

        row.setOnLongClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.entry_delete_title))
                    .setPositiveButton(getString(R.string.delete), (d, w) -> {
                        store.deleteEntry(e.id);
                        refresh();
                    })
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show();
            return true;
        });
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    /** Udhaar ya payment entry add karne ka form (amount, date, note). */
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
        noteIn.setHint(getString(R.string.entry_note_hint));
        noteIn.setTextSize(16);

        form.addView(Ui.label(this, getString(R.string.entry_amount_label)));
        form.addView(amountIn);
        form.addView(dateBtn, Ui.matchWrap(this, 12, 0));
        form.addView(Ui.label(this, getString(R.string.entry_note_label)), Ui.matchWrap(this, 12, 0));
        form.addView(noteIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(type == Entry.TYPE_UDHAAR
                        ? R.string.entry_add_udhaar : R.string.entry_add_payment))
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
                    store.addEntry(customer.id, type, amt, chosenDate[0], noteIn.getText().toString());
                    refresh();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void showEditDialog() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 20);
        form.setPadding(pad, Ui.dp(this, 8), pad, 0);

        EditText nameIn = new EditText(this);
        nameIn.setText(customer.name);
        nameIn.setTextSize(16);
        EditText phoneIn = new EditText(this);
        phoneIn.setText(customer.phone);
        phoneIn.setInputType(InputType.TYPE_CLASS_PHONE);
        phoneIn.setTextSize(16);
        form.addView(Ui.label(this, getString(R.string.cust_name_label)));
        form.addView(nameIn);
        form.addView(Ui.label(this, getString(R.string.cust_phone_label)), Ui.matchWrap(this, 10, 0));
        form.addView(phoneIn);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.cust_edit_title))
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
                    store.updateCustomer(customer, name, phone);
                    recreate();
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .setNeutralButton(getString(R.string.delete), (d, w) -> {
                    new AlertDialog.Builder(this)
                            .setTitle(getString(R.string.cust_delete_title))
                            .setMessage(getString(R.string.cust_delete_msg, customer.name))
                            .setPositiveButton(getString(R.string.delete), (dd, ww) -> {
                                store.deleteCustomer(customer.id);
                                finish();
                            })
                            .setNegativeButton(getString(R.string.cancel), null)
                            .show();
                })
                .show();
    }

    /** WhatsApp reminder: wa.me link + tayyar shuda paighaam (template {name} {amount} {shop}). */
    private void sendWhatsAppReminder() {
        long bal = store.balanceOf(customer.id);
        if (customer.phone.isEmpty()) {
            Toast.makeText(this, R.string.err_no_phone, Toast.LENGTH_SHORT).show();
            return;
        }
        if (bal <= 0) {
            Toast.makeText(this, getString(R.string.balance_clear), Toast.LENGTH_SHORT).show();
            return;
        }
        String template = store.getReminderTemplate();
        if (template.isEmpty()) template = getString(R.string.default_reminder);
        String shop = store.getShopName();
        if (shop.isEmpty()) shop = getString(R.string.app_name);
        String msg = template
                .replace("{name}", customer.name)
                .replace("{amount}", Fmt.rs(bal))
                .replace("{shop}", shop);
        String url;
        try {
            url = "https://wa.me/" + Fmt.waNumber(customer.phone)
                    + "?text=" + URLEncoder.encode(msg, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            url = "https://wa.me/" + Fmt.waNumber(customer.phone);
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.err_no_whatsapp, Toast.LENGTH_SHORT).show();
        }
    }

    /** PDF bana kar share — whatsappOnly ho to seedha WhatsApp par. */
    private void sharePdf(boolean whatsappOnly) {
        File pdf = PdfStatement.generate(this, store, customer);
        if (pdf == null) {
            Toast.makeText(this, R.string.err_no_whatsapp, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdf);
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("application/pdf");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (whatsappOnly) {
            send.setPackage("com.whatsapp");
            try {
                startActivity(send);
                return;
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.err_no_whatsapp, Toast.LENGTH_SHORT).show();
                return;
            }
        }
        startActivity(Intent.createChooser(send, getString(R.string.share_statement_title)));
    }
}
