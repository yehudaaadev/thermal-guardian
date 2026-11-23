# DECISIONS.md - Architectural & Technical Decisions

Dokumen ini mencatat semua asumsi teknis, keputusan desain, dan strategi implementasi pada aplikasi **ThermalGuard**.

---

## 1. Arsitektur & Struktur Proyek
- **Pilihan:** Modul tunggal `app` dengan pendekatan *Clean Architecture + MVVM* dan Dagger Hilt untuk Dependency Injection.
- **Rasional:** Modul tunggal meminimalkan overhead build Gradle pada lingkungan lokal sambil tetap memisahkan `domain` (business logic, model, use case), `data` (Room, DataStore, repository), `service` (Foreground Service, receivers), `ui` (Compose screens, components, viewmodels), dan `util`.

---

## 2. Kepatuhan Android 14 (Target SDK 34) & Foreground Service
- **Pilihan:** Menggunakan `FOREGROUND_SERVICE_SPECIAL_USE` dengan deklarasi properti `android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE` bernilai *"Continuous device thermal status monitoring and overheat prevention alerts"*.
- **Rasional:** Android 14 mewajibkan setiap foreground service mendeklarasikan tipe yang spesifik. Karena pemantauan suhu perangkat hardware 24/7 tidak memiliki tipe `dataSync` atau `health` yang cocok secara presisi tanpa pembatasan waktu, tipe `specialUse` adalah standar resmi yang tepat.

---

## 3. Nol Dependensi GMS / Firebase (Privasi & Kompatibilitas Huawei)
- **Pilihan:** Tidak ada library dari `com.google.android.gms:*` ataupun `com.google.firebase:*`.
- **Rasional:** Sesuai spesifikasi PRD, target utama adalah perangkat seperti Huawei P50 Pro (HarmonyOS/EMUI tanpa Google Play Services). Semua pemrosesan data (analisis penggunaan, riwayat suhu, kalkulasi laju pemanasan) dilakukan 100% *on-device*.

---

## 4. Strategi Interval Adaptif & Hemat Daya (< 2% Baterai / Hari)
- **Pilihan:**
  - Suhu **Normal / Cool** (< 38°C): Interval pembacaan 30 detik.
  - Suhu **Hangat / Warm** (38°C – 40°C): Interval pembacaan 15 detik.
  - Suhu **Panas / Hot** (40°C – 43°C): Interval pembacaan 5 detik.
  - Suhu **Kritis / Critical** (> 43°C): Interval pembacaan 3 detik.
- **Rasional:** Menyeimbangkan efisiensi baterai saat suhu aman, sekaligus memastikan peringatan bahaya muncul kurang dari 5 detik saat suhu melonjak.

---

## 5. Strategi Penulisan Database (Room)
- **Pilihan:** Penulisan data ke Room dilakukan tiap 30 detik atau jika terjadi lompatan suhu signifikan (|delta| >= 0,5°C dalam rentang minimal 10 detik).
- **Rasional:** Menghindari *disk I/O thrashing* dan mengurangi pemakaian baterai akibat penulisan database berulang kali per detik.

---

## 6. Visualisasi Grafik Kustom (Jetpack Compose Canvas)
- **Pilihan:** Mengembangkan komponen grafik custom menggunakan `androidx.compose.foundation.Canvas` dengan interpolasi kurva *cubic bezier*, gradient shading, dan deteksi gesture sentuh (*scrubbing tooltip*).
- **Rasional:** Menghindari penambahan library grafik eksternal yang berat, bebas dari dependensi GMS, dan memiliki rendering performa tinggi 60/120fps.

---

## 7. Penanganan Izin Akses Penggunaan (`PACKAGE_USAGE_STATS`)
- **Pilihan:** Fitur deteksi sumber panas dan mode game dibuat *gracefully degraded*. Jika izin belum diberikan, UI menampilkan kartu edukasi yang menjelaskan mengapa izin dibutuhkan beserta tombol pintasan ke pengaturan sistem tanpa memblokir fungsi utama pemantauan suhu.

---

## 8. Penanganan Fragmentasi OEM (Huawei / Xiaomi / Oppo / Vivo)
- **Pilihan:** Helper `HuaweiGuideHelper` menyediakan daftar fallback intent untuk OEM spesifik (seperti `com.huawei.systemmanager`, `com.miui.securitycenter`, dll.) dan secara otomatis beralih ke `Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS` atau `ACTION_APPLICATION_DETAILS_SETTINGS` jika intent OEM tidak tersedia.

---

## 9. Notifikasi Interaktif & Quick Actions Terintegrasi
- **Pilihan:** Notifikasi peringatan suhu tinggi dan pengisian daya dilengkapi dengan tombol aksi langsung (*Action Buttons*) seperti *Layar*, *Hotspot*, dan *Aplikasi* yang langsung membuka intent pengaturan sistem terkait.
- **Rasional:** Memungkinkan pengguna meredakan panas perangkat dalam 1 klik langsung dari status bar / lockscreen tanpa harus membuka aplikasi terlebih dahulu.

---

## 10. Alih Bahasa Reaktif & Pilihan Interval Fleksibel
- **Pilihan:** Pengaturan menyediakan opsi interval pemantauan (Adaptif, 15s, 30s, 60s) serta alih bahasa reaktif (Indonesia & Inggris) via `CompositionLocalProvider` yang langsung memperbarui string UI secara instan tanpa perlu merestart Activity.

---

## 11. Floating Thermal HUD Overlay (`SYSTEM_ALERT_WINDOW`)
- **Pilihan:** Layanan `ThermalOverlayService` menggunakan WindowManager view ringan tanpa XML overhead dengan sistem sentuh draggable dan klik untuk membuka aplikasi utama.
- **Rasional:** Memberikan visibilitas suhu waktu-nyata saat bermain game tanpa menutupi elemen penting di layar (HUD transparan).

---

## 12. Thermal Stress Test Engine & Auto-Cutoff Safety
- **Pilihan:** `StressTestRunner` menggunakan coroutine multi-core non-blocking yang mengeksekusi komputasi matematika dengan pengawas suhu setiap detik dan batas pemutus otomatis di 42.0°C.
- **Rasional:** Memberikan kemampuan benchmark disipasi termal perangkat yang terukur tanpa membahayakan kesehatan baterai lithium atau hardware pengguna.

---

## 13. Cooling Stopwatch & Laju Disipasi Termal
- **Pilihan:** Stopwatch interaktif yang mengukur delta penurunan suhu (°C) dan menghitung laju pendinginan (`°C/menit`).
- **Rasional:** Membantu pengguna membandingkan efektivitas berbagai metode pendinginan (misal: melepas casing vs menggunakan kipas pendingin eksternal).

---

## 14. Kalibrasi Sensor, Jam Tenang, & Proteksi Pengisian Daya
- **Pilihan:** 
  - Offset kalibrasi (-2.0°C s/d +2.0°C) diterapkan langsung pada lapisan pembacaan sensor.
  - Jam tenang (22:00 - 06:00) menyenyapkan alarm hangat & panas, namun mempertahankan bunyi darurat untuk suhu kritis (> 43°C).
  - Alarm batas pengisian daya (80% / 100%) mengingatkan pengguna mencabut charger saat ponsel tercolok untuk menjaga keawetan baterai lithium.


