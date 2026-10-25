package com.udhaarwapas.app;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.udhaarwapas.app.data.Bill;
import com.udhaarwapas.app.data.BillItem;
import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Fmt;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.PdfBill;
import com.udhaarwapas.app.util.Ui;

import java.io.File;
import java.util.List;

/**
 * Bills screen: bane hue bills ki list. Naya bill NewBillActivity se banta hai.
 * Bill par tap = tafseel + PDF share / WhatsApp par bhejna.
 */
public class BillsActivity extends AppCompatActivity {

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
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.tile_bills), null, null);
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

        android.widget.Button add = Ui.bigButton(this, getString(R.string.bills_new), Ui.TILE_PURPLE);
        add.setOnClickListener(v -> startActivity(new Intent(this, NewBillActivity.class)));
        content.addView(add, Ui.matchWrap(this, 2, 12));

        List<Bill> list = store.getBills();
        if (list.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText(getString(R.string.bills_empty));
            empty.setTextColor(Ui.MUTED);
            empty.setTextSize(15);
            empty.setGravity(Gravity.CENTER);
            content.addView(empty, Ui.matchWrap(this, 10, 0));
        } else {
            for (Bill b : list) content.addView(billRow(b));
        }
    }

    private View billRow(Bill b) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackground(Ui.cardBg(this));
        row.setPadding(Ui.dp(this, 14), Ui.dp(this, 10), Ui.dp(this, 14), Ui.dp(this, 10));

        LinearLayout left = new LinearLayout(this);
        left.setOrientation(LinearLayout.VERTICAL);
        TextView no = new TextView(this);
        no.setText(getString(R.string.bills_bill_no, b.number));
        no.setTextSize(16);
        no.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        no.setTextColor(Ui.TEXT);
        left.addView(no);
        TextView sub = new TextView(this);
        String cust = b.customerName.isEmpty() ? getString(R.string.bills_walkin) : b.customerName;
        sub.setText(cust + " · " + Fmt.date(b.date));
        sub.setTextSize(13);
        sub.setTextColor(Ui.MUTED);
        left.addView(sub);
        row.addView(left, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        LinearLayout right = new LinearLayout(this);
        right.setOrientation(LinearLayout.VERTICAL);
        right.setGravity(Gravity.END);
        TextView amt = new TextView(this);
        amt.setText(Fmt.rs(b.total));
        amt.setTextSize(16);
        amt.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        amt.setTextColor(Ui.GREEN_DARK);
        right.addView(amt);
        TextView tag = new TextView(this);
        tag.setText(getString(b.onUdhaar ? R.string.bills_udhaar_tag : R.string.bills_paid));
        tag.setTextSize(12);
        tag.setTextColor(b.onUdhaar ? Ui.RED : Ui.MUTED);
        right.addView(tag);
        row.addView(right);

        row.setOnClickListener(v -> showBillActions(b));
        row.setLayoutParams(Ui.matchWrap(this, 0, 8));
        return row;
    }

    /** Bill ki tafseel + PDF / WhatsApp share ke buttons. */
    private void showBillActions(Bill b) {
        StringBuilder msg = new StringBuilder();
        String cust = b.customerName.isEmpty() ? getString(R.string.bills_walkin) : b.customerName;
        msg.append(cust).append(" · ").append(Fmt.date(b.date)).append("\n");
        for (BillItem it : b.items) {
            msg.append("\n").append(it.name)
                    .append("  ×").append(Fmt.qty(it.qty))
                    .append("  = ").append(Fmt.rs(it.amount()));
        }
        msg.append("\n\n").append(getString(R.string.bills_subtotal))
                .append(": ").append(Fmt.rs(b.subtotal()));
        if (b.discount > 0) {
            msg.append("\n").append(getString(R.string.pdf_discount))
                    .append(": - ").append(Fmt.rs(b.discount));
        }
        msg.append("\n").append(getString(R.string.bills_total))
                .append(": ").append(Fmt.rs(b.total));
        msg.append("\n").append(getString(b.onUdhaar ? R.string.bills_udhaar_tag : R.string.bills_paid));

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.bills_bill_no, b.number))
                .setMessage(msg.toString())
                .setPositiveButton(getString(R.string.bills_btn_pdf), (d, w) -> shareBillPdf(b, false))
                .setNeutralButton(getString(R.string.bills_btn_whatsapp), (d, w) -> shareBillPdf(b, true))
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Bill PDF bana kar share — whatsappOnly ho to seedha WhatsApp par. */
    private void shareBillPdf(Bill b, boolean whatsappOnly) {
        File pdf = PdfBill.generate(this, store, b);
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
        startActivity(Intent.createChooser(send, getString(R.string.share_bill_title)));
    }
}
