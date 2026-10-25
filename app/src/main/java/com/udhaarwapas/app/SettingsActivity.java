package com.udhaarwapas.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.udhaarwapas.app.data.KhataStore;
import com.udhaarwapas.app.util.Lang;
import com.udhaarwapas.app.util.Ui;

/**
 * Settings: dukaan ka naam, maalik ka naam, WhatsApp reminder template,
 * aur zaban (English / Urdu). Save par language poori app me lag jati hai.
 */
public class SettingsActivity extends AppCompatActivity {

    private KhataStore store;
    private String chosenLang;
    private Button btnEn;
    private Button btnUr;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(Lang.wrap(base));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new KhataStore(this);
        chosenLang = Lang.current(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        LinearLayout header = Ui.header(this, "←  " + getString(R.string.settings_title), null, null);
        header.setOnClickListener(v -> finish());
        root.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int pad = Ui.dp(this, 16);
        form.setPadding(pad, pad, pad, pad);
        scroll.addView(form, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        root.addView(scroll);

        // Dukaan ka naam
        form.addView(Ui.label(this, getString(R.string.set_shop_label)));
        EditText shopIn = new EditText(this);
        shopIn.setHint(getString(R.string.set_shop_hint));
        shopIn.setText(store.getShopName());
        shopIn.setTextSize(17);
        form.addView(shopIn);

        // Maalik ka naam
        form.addView(Ui.label(this, getString(R.string.set_owner_label)), Ui.matchWrap(this, 14, 0));
        EditText ownerIn = new EditText(this);
        ownerIn.setHint(getString(R.string.set_owner_hint));
        ownerIn.setText(store.getOwnerName());
        ownerIn.setTextSize(17);
        form.addView(ownerIn);

        // Reminder template
        form.addView(Ui.label(this, getString(R.string.set_template_label)), Ui.matchWrap(this, 14, 0));
        EditText templateIn = new EditText(this);
        String tpl = store.getReminderTemplate();
        templateIn.setText(tpl.isEmpty() ? getString(R.string.default_reminder) : tpl);
        templateIn.setTextSize(15);
        templateIn.setMinLines(3);
        templateIn.setGravity(android.view.Gravity.TOP);
        form.addView(templateIn);
        TextView help = new TextView(this);
        help.setText(getString(R.string.set_template_help));
        help.setTextSize(12);
        help.setTextColor(Ui.MUTED);
        form.addView(help);

        // Zaban
        TextView langLabel = Ui.label(this, getString(R.string.set_language));
        form.addView(langLabel, Ui.matchWrap(this, 18, 6));
        LinearLayout langRow = new LinearLayout(this);
        langRow.setOrientation(LinearLayout.HORIZONTAL);
        btnEn = Ui.outlineButton(this, getString(R.string.lang_en), Ui.GREEN_DARK);
        btnUr = Ui.outlineButton(this, getString(R.string.lang_ur), Ui.GREEN_DARK);
        btnEn.setOnClickListener(v -> {
            chosenLang = "en";
            refreshLangButtons();
        });
        btnUr.setOnClickListener(v -> {
            chosenLang = "ur";
            refreshLangButtons();
        });
        LinearLayout.LayoutParams h1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        h1.setMarginEnd(Ui.dp(this, 6));
        LinearLayout.LayoutParams h2 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        h2.setMarginStart(Ui.dp(this, 6));
        langRow.addView(btnEn, h1);
        langRow.addView(btnUr, h2);
        form.addView(langRow);
        refreshLangButtons();

        // Save
        Button save = Ui.bigButton(this, getString(R.string.save), Ui.GREEN);
        save.setOnClickListener(v -> {
            store.setShopName(shopIn.getText().toString());
            store.setOwnerName(ownerIn.getText().toString());
            store.setReminderTemplate(templateIn.getText().toString());
            boolean langChanged = !chosenLang.equals(Lang.current(this));
            store.setLang(chosenLang);
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            if (langChanged) {
                // Zaban badli hai — app ko Main se dobara shuru karo taake har jagah lag jaye
                Intent i = new Intent(this, MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
            } else {
                finish();
            }
        });
        form.addView(save, Ui.matchWrap(this, 22, 0));

        setContentView(root);
    }

    private void refreshLangButtons() {
        styleLangButton(btnEn, "en".equals(chosenLang));
        styleLangButton(btnUr, "ur".equals(chosenLang));
    }

    private void styleLangButton(Button b, boolean selected) {
        if (selected) {
            b.setBackground(Ui.roundBg(this, Ui.GREEN, 10));
            b.setTextColor(Ui.WHITE);
            b.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        } else {
            b.setBackground(Ui.roundBg(this, Ui.WHITE, 10));
            b.setTextColor(Ui.GREEN_DARK);
            b.setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
        }
    }
}
