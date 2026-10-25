# UdhaarWapas v1

WhatsApp-first khata (udhaar) app Pakistani wholesalers/distributors ke liye —
Urdu-first, dead simple, 100% offline. Zero permissions, koi account nahi, koi server nahi.

## Features
- **Customer khata:** customer add/edit/delete; har customer ki entries —
  *udhaar diya* / *payment mili* (amount, date, note); running balance
  (lene hain / dene hain) hamesha saaf nazar.
- **Daily summary (home):** kul udhaar baaki, aaj ki collection, sab se bare
  baqaya wale customer, aur 30+ din se payment na dene wale.
- **WhatsApp reminder:** ek tap — wa.me link ke saath tayyar shuda polite
  paighaam jisme exact baqaya rakam likhi hoti hai. Message template
  Settings me badla ja sakta hai ({name}, {amount}, {shop}).
- **PDF statement:** dukaan ke naam wali saaf balance statement (Android ke
  built-in PdfDocument se, koi library nahi) — share sheet se ya seedha
  WhatsApp par bhejo.
- **Settings:** dukaan ka naam, maalik ka naam, reminder template, zaban
  (English / اردو) — pehli dafa default Urdu.

## Data
Sab kuch phone par hi rehta hai — SharedPreferences me JSON
(`customers`, `entries`, settings). Internet permission hi nahi hai,
is liye data kahin ja hi nahi sakta.

## Files
- `data/KhataStore.java` — storage + hisaab (balances, aaj ki collection, 30-din overdue)
- `data/Customer.java`, `data/Entry.java` — models
- `MainActivity.java` — home / daily summary + customer list
- `CustomerActivity.java` — khata detail, entries, WhatsApp, PDF
- `SettingsActivity.java` — dukaan/maalik/template/zaban
- `util/PdfStatement.java` — PDF banana; `util/Fmt.java` — Rs/date/phone format;
  `util/Lang.java` — EN/UR wrap; `util/Ui.java` — shared green/white UI
- UI poori programmatic hai (koi layout XML nahi) — Bijli Bill wali pattern.

## Build
GitHub Actions (`build.yml`, debug-only — repo me keystore secrets nahi, is liye
release job nahi). **Note:** workflow file GitHub App ki permission se push nahi hota —
`.github/workflows/build.yml` manually add karna hota hai (Settings Guide wala step).
