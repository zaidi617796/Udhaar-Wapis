package com.udhaarwapas.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.udhaarwapas.app.R;
import com.udhaarwapas.app.data.Customer;
import com.udhaarwapas.app.data.Entry;
import com.udhaarwapas.app.data.KhataStore;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer ki balance statement PDF — Android ke built-in PdfDocument se,
 * koi external library nahi. File cache/statements/ me banti hai (FileProvider se share).
 */
public class PdfStatement {

    private static final int PAGE_W = 595;   // A4 points
    private static final int PAGE_H = 842;
    private static final int MARGIN = 40;
    private static final int ROW_H = 22;
    private static final int TOP_AFTER_HEADER = 205;
    private static final int BOTTOM_LIMIT = PAGE_H - 130;

    /** PDF bana kar file wapas karo; fail ho to null. */
    public static File generate(Context ctx, KhataStore store, Customer customer) {
        List<Entry> newestFirst = store.getEntries(customer.id);
        List<Entry> entries = new ArrayList<>(newestFirst);
        // PDF me purani pehle, nayi baad me (khata reading order)
        java.util.Collections.reverse(entries);

        long totalUdhaar = 0, totalPay = 0;
        for (Entry e : entries) {
            if (e.type == Entry.TYPE_UDHAAR) totalUdhaar += e.amount;
            else totalPay += e.amount;
        }
        long balance = totalUdhaar - totalPay;

        String shop = store.getShopName();
        if (shop.isEmpty()) shop = ctx.getString(R.string.app_name);
        String owner = store.getOwnerName();

        Paint title = new Paint();
        title.setTextSize(22);
        title.setColor(Ui.GREEN_DARK);
        title.setFakeBoldText(true);

        Paint sub = new Paint();
        sub.setTextSize(12);
        sub.setColor(Color.DKGRAY);

        Paint head = new Paint();
        head.setTextSize(11);
        head.setColor(Color.WHITE);
        head.setFakeBoldText(true);

        Paint cell = new Paint();
        cell.setTextSize(11);
        cell.setColor(Color.BLACK);

        Paint cellBold = new Paint(cell);
        cellBold.setFakeBoldText(true);

        Paint totalPaint = new Paint();
        totalPaint.setTextSize(14);
        totalPaint.setFakeBoldText(true);
        totalPaint.setColor(Ui.GREEN_DARK);

        PdfDocument doc = new PdfDocument();
        int pageNum = 0;
        int idx = 0;

        while (true) {
            pageNum++;
            PdfDocument.Page page = doc.startPage(
                    new PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, pageNum).create());
            android.graphics.Canvas cv = page.getCanvas();

            int y;
            if (pageNum == 1) {
                // Header: dukaan ka naam, owner, statement title, customer
                cv.drawText(shop, MARGIN, 55, title);
                y = 78;
                if (!owner.isEmpty()) {
                    cv.drawText(ctx.getString(R.string.pdf_owner, owner), MARGIN, y, sub);
                    y += 18;
                }
                cv.drawText(ctx.getString(R.string.pdf_statement_title)
                        + " — " + Fmt.date(System.currentTimeMillis()), MARGIN, y, sub);
                y += 32;
                cv.drawText(ctx.getString(R.string.pdf_customer) + ": " + customer.name
                        + (customer.phone.isEmpty() ? "" : "  (" + customer.phone + ")"), MARGIN, y, cellBold);
                y = TOP_AFTER_HEADER - 40;
            } else {
                y = 60;
            }

            // Table header row
            cv.drawRect(MARGIN, y, PAGE_W - MARGIN, y + ROW_H,
                    paintOf(Ui.GREEN));
            int xDate = MARGIN + 8;
            int xDetail = MARGIN + 110;
            int xUdhaar = PAGE_W - MARGIN - 170;
            int xPay = PAGE_W - MARGIN - 85;
            cv.drawText(ctx.getString(R.string.pdf_date_col), xDate, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_detail_col), xDetail, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_udhaar_col), xUdhaar, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_payment_col), xPay, y + 15, head);
            y += ROW_H;

            // Rows
            while (idx < entries.size()) {
                Entry e = entries.get(idx);
                if (y + ROW_H > BOTTOM_LIMIT) break;
                String detail = e.type == Entry.TYPE_UDHAAR
                        ? ctx.getString(R.string.entry_udhaar)
                        : ctx.getString(R.string.entry_payment);
                if (!e.note.isEmpty()) detail += " — " + e.note;
                if (detail.length() > 34) detail = detail.substring(0, 33) + "…";
                cv.drawText(Fmt.date(e.date), xDate, y + 15, cell);
                cv.drawText(detail, xDetail, y + 15, cell);
                if (e.type == Entry.TYPE_UDHAAR) {
                    cv.drawText(Fmt.rs(e.amount), xUdhaar, y + 15, cell);
                } else {
                    cv.drawText(Fmt.rs(e.amount), xPay, y + 15, cell);
                }
                y += ROW_H;
                idx++;
            }

            boolean lastPage = idx >= entries.size();
            if (lastPage) {
                y += 14;
                cv.drawLine(MARGIN, y, PAGE_W - MARGIN, y, cell);
                y += 26;
                cv.drawText(ctx.getString(R.string.pdf_total_udhaar) + ":  " + Fmt.rs(totalUdhaar), MARGIN, y, cellBold);
                y += 20;
                cv.drawText(ctx.getString(R.string.pdf_total_payment) + ":  " + Fmt.rs(totalPay), MARGIN, y, cellBold);
                y += 28;
                cv.drawText(ctx.getString(R.string.pdf_balance) + ":  " + Fmt.rs(Math.max(0, balance)), MARGIN, y, totalPaint);
                y += 34;
                cv.drawText(ctx.getString(R.string.pdf_generated_by), MARGIN, y, sub);
            }
            doc.finishPage(page);
            if (lastPage) break;
        }

        try {
            File dir = new File(ctx.getCacheDir(), "statements");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "statement_" + customer.id + ".pdf");
            FileOutputStream fos = new FileOutputStream(out);
            doc.writeTo(fos);
            fos.close();
            doc.close();
            return out;
        } catch (Exception e) {
            doc.close();
            return null;
        }
    }

    private static Paint paintOf(int color) {
        Paint p = new Paint();
        p.setColor(color);
        p.setStyle(Paint.Style.FILL);
        return p;
    }
}
