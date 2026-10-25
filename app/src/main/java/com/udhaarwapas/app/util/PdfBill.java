package com.udhaarwapas.app.util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;

import com.udhaarwapas.app.R;
import com.udhaarwapas.app.data.Bill;
import com.udhaarwapas.app.data.BillItem;
import com.udhaarwapas.app.data.KhataStore;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Sale bill ki PDF — PdfStatement wali hi tarz: dukaan header, items table,
 * subtotal / discount / total. Built-in PdfDocument, koi library nahi.
 * File cache/statements/ me banti hai (FileProvider se share hoti hai).
 */
public class PdfBill {

    private static final int PAGE_W = 595;   // A4 points
    private static final int PAGE_H = 842;
    private static final int MARGIN = 40;
    private static final int ROW_H = 22;
    private static final int BOTTOM_LIMIT = PAGE_H - 170;

    /** PDF bana kar file wapas karo; fail ho to null. */
    public static File generate(Context ctx, KhataStore store, Bill bill) {
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
                cv.drawText(shop, MARGIN, 55, title);
                y = 78;
                if (!owner.isEmpty()) {
                    cv.drawText(ctx.getString(R.string.pdf_owner, owner), MARGIN, y, sub);
                    y += 18;
                }
                cv.drawText(ctx.getString(R.string.pdf_bill_title)
                        + " — " + Fmt.date(bill.date), MARGIN, y, sub);
                y += 20;
                cv.drawText(ctx.getString(R.string.pdf_bill_no, bill.number), MARGIN, y, sub);
                y += 30;
                String cust = bill.customerName.isEmpty()
                        ? ctx.getString(R.string.bills_walkin) : bill.customerName;
                cv.drawText(ctx.getString(R.string.pdf_customer) + ": " + cust, MARGIN, y, cellBold);
                y += 30;
            } else {
                y = 60;
            }

            // Table header row
            cv.drawRect(MARGIN, y, PAGE_W - MARGIN, y + ROW_H, paintOf(Ui.GREEN));
            int xItem = MARGIN + 8;
            int xQty = PAGE_W - MARGIN - 230;
            int xRate = PAGE_W - MARGIN - 160;
            int xAmt = PAGE_W - MARGIN - 85;
            cv.drawText(ctx.getString(R.string.pdf_item_col), xItem, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_qty_col), xQty, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_rate_col), xRate, y + 15, head);
            cv.drawText(ctx.getString(R.string.pdf_amount_col), xAmt, y + 15, head);
            y += ROW_H;

            while (idx < bill.items.size()) {
                BillItem it = bill.items.get(idx);
                if (y + ROW_H > BOTTOM_LIMIT) break;
                String name = it.name;
                if (name.length() > 30) name = name.substring(0, 29) + "…";
                cv.drawText(name, xItem, y + 15, cell);
                cv.drawText(Fmt.qty(it.qty), xQty, y + 15, cell);
                cv.drawText(Fmt.rs(it.rate), xRate, y + 15, cell);
                cv.drawText(Fmt.rs(it.amount()), xAmt, y + 15, cell);
                y += ROW_H;
                idx++;
            }

            boolean lastPage = idx >= bill.items.size();
            if (lastPage) {
                y += 14;
                cv.drawLine(MARGIN, y, PAGE_W - MARGIN, y, cell);
                y += 26;
                cv.drawText(ctx.getString(R.string.bills_subtotal) + ":  " + Fmt.rs(bill.subtotal()), MARGIN, y, cellBold);
                y += 20;
                if (bill.discount > 0) {
                    cv.drawText(ctx.getString(R.string.pdf_discount) + ":  - " + Fmt.rs(bill.discount), MARGIN, y, cellBold);
                    y += 20;
                }
                cv.drawText(ctx.getString(R.string.bills_total) + ":  " + Fmt.rs(bill.total), MARGIN, y, totalPaint);
                y += 26;
                cv.drawText(bill.onUdhaar
                        ? ctx.getString(R.string.bills_udhaar_tag)
                        : ctx.getString(R.string.bills_paid), MARGIN, y, cellBold);
                y += 30;
                cv.drawText(ctx.getString(R.string.pdf_thanks), MARGIN, y, sub);
                y += 18;
                cv.drawText(ctx.getString(R.string.pdf_generated_by), MARGIN, y, sub);
            }
            doc.finishPage(page);
            if (lastPage) break;
        }

        try {
            File dir = new File(ctx.getCacheDir(), "statements");
            if (!dir.exists()) dir.mkdirs();
            File out = new File(dir, "bill_" + bill.id + ".pdf");
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
