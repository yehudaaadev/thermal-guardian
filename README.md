# ThermalGuard

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.23-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Platform-Android_8.0+_(API_26+)-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4?style=flat-square&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Clean Architecture](https://img.shields.io/badge/Architecture-Clean_Architecture_+_MVVM-blue?style=flat-square)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](LICENSE)

Aplikasi Android Native (**Kotlin + Jetpack Compose + Material 3**) untuk memantau suhu perangkat keras secara *real-time*, menghitung skor kesehatan termal perangkat, memprediksi pemulihan hotspot, mendeteksi sumber beban CPU, serta memberikan peringatan dini bertingkat sebelum terjadi *thermal throttling* atau *overheat*.

Dioptimalkan khusus untuk perangkat Android standar serta **Huawei HarmonyOS / EMUI** dengan arsitektur 100% mandiri (bebas dari ketergantungan Google Play Services / Firebase).

---

## Unduh Langsung Aplikasi (Direct APK Download)

Pilih berkas instalasi APK yang sesuai dengan tahun rilis dan spesifikasi perangkat smartphone Anda:

| Arsitektur CPU | Rekomendasi Tipe HP | Tautan Unduhan Langsung | Ukuran & Kompatibilitas |
|---|---|---|---|
| **64-bit (`arm64-v8a`)** | **HP Rilis 2017 ke Atas** *(Modern)* | [Unduh ThermalGuard 64-bit APK](https://github.com/yehudaaadev/thermal-guardian/releases/download/v1.0.0/ThermalGuard-v1.0.0-arm64-v8a.apk) | ~17 MB · Dioptimalkan untuk 95% smartphone masa kini (Android 8.0 - 14+, Snapdragon, MediaTek, Kirin, Exynos 64-bit). Paling hemat daya & responsif. |
| **32-bit (`armeabi-v7a`)** | **HP Rilis 2017 ke Bawah** *(Lawas)* | [Unduh ThermalGuard 32-bit APK](https://github.com/yehudaaadev/thermal-guardian/releases/download/v1.0.0/ThermalGuard-v1.0.0-armeabi-v7a.apk) | ~17 MB · Khusus untuk smartphone jadul, chipset 32-bit, atau perangkat Android entry-level agar tetap stabil dan ringan. |
| **Universal APK** | Seluruh Perangkat Android | [Unduh ThermalGuard Universal APK](https://github.com/yehudaaadev/thermal-guardian/releases/download/v1.0.0/ThermalGuard-v1.0.0-universal.apk) | ~17 MB · Kompatibel secara universal pada semua arsitektur prosesor Android. |

### Panduan Memilih Versi yang Tepat:
- **Pilih Versi 64-bit (`arm64-v8a`)**: Jika smartphone Anda dibeli atau dirilis pada tahun **2017 hingga sekarang** (misal: Samsung Galaxy seri A/M/S modern, Xiaomi Redmi Note 5 ke atas, Poco, Oppo, Vivo, Realme, Infinix, Huawei P30/P50). Versi ini mengeksekusi instruksi 64-bit penuh sehingga sensor dan background service bekerja lebih ringan.
- **Pilih Versi 32-bit (`armeabi-v7a`)**: Jika Anda menggunakan smartphone keluaran tahun **2017 ke bawah** atau HP dengan arsitektur prosesor 32-bit lawas agar tidak terjadi kendala inkompatibilitas instruksi biner.

#### Langkah Instalasi di HP:
1. Klik salah satu tautan unduhan langsung di atas menggunakan peramban HP Anda (Chrome, Samsung Internet, atau Browser bawaan).
2. Setelah unduhan selesai, buka berkas `.apk`.
3. Aktifkan opsi **"Izinkan penginstalan dari sumber ini"** jika sistem Android meminta konfirmasi keamanan.
4. Tekan **Instal**, buka ThermalGuard, dan berikan izin notifikasi agar pemantauan suhu aktif di bilah status.

---

## Arsitektur Sistem & Fitur Unggulan

### 1. Modul Pemantauan & Kesehatan Termal
- **Thermal Health Score (0-100):** Menghitung indeks kesehatan termal perangkat secara dinamis berdasarkan akumulasi stres panas, kecepatan lonjakan suhu, dan kebiasaan pemakaian.
- **Hotspot Safe-Recovery & Cooldown Estimator:** Memantau status penambatan jaringan (tethering/hotspot) dan memperkirakan waktu pendinginan aman (target < 39.0°C) saat hotspot dinonaktifkan sistem akibat suhu tinggi, disertai notifikasi kesiapan saat perangkat sudah aman.
- **Fast Charging Standby Mode:** Otomatis menjeda pemantauan intensif saat pengisian daya aktif guna meminimalkan konsumsi prosesor dan memaksimalkan kecepatan pengisian baterai.
- **Auto-Stop on Recents Swipe:** Menghentikan *foreground service* seketika saat aplikasi dibersihkan dari layar Recent Apps (*swipe away*), menjaga efisiensi memori sistem.
- **Monitor Sensor Real-Time:** Membaca suhu baterai via `ACTION_BATTERY_CHANGED` (`EXTRA_TEMPERATURE`, presisi 0,1°C), status termal sistem via `PowerManager.getCurrentThermalStatus()` (API 29+), dan pembacaan `getThermalHeadroom()` (API 30+).

### 2. Fitur Diagnostik & Analisis Perangkat
- **Floating Thermal HUD Overlay:** Gelembung suhu mengambang yang dapat digeser bebas (*draggable*) untuk memantau suhu secara langsung di atas game atau aplikasi layar penuh.
- **Thermal Stress Test & Benchmark Engine:** Alat pengujian komputasi CPU multi-core aman (15s/30s/60s) dengan *auto-cutoff* darurat di 42.0°C untuk mengukur efisiensi pelepasan panas fisik bodi HP.
- **Cooling Stopwatch:** Mengukur laju pendinginan perangkat (°C/menit) setelah selesai bermain game atau komputasi berat.
- **Pendeteksi Sumber Panas:** Menampilkan daftar aplikasi paling aktif membebani CPU via `UsageStatsManager` saat suhu perangkat melonjak.
- **Riwayat & Grafik Interaktif:** Grafik berbasis Jetpack Compose Canvas dengan kurva halus (*cubic bezier*), visualisasi 1 jam / 24 jam / 7 hari, dan retensi data lokal Room Database.

---

## Spesifikasi Teknis & Stack

| Lapisan | Komponen / Pustaka | Keterangan |
|---|---|---|
| **Bahasa Pemrograman** | Kotlin 1.9.23 | Strict typing, Coroutines & Flow |
| **Framework UI** | Jetpack Compose + Material 3 | Desain deklaratif, Navigation Compose |
| **Pola Desain** | Clean Architecture + MVVM | Pemisahan domain, data, dan UI layer |
| **Dependency Injection** | Dagger Hilt 2.51.1 | Modularitas dan kemudahan pengujian |
| **Penyimpanan Lokal** | Room Database 2.6.1 & DataStore Preferences | Persistensi data suhu offline |
| **Layanan Latar Belakang**| Android Foreground Service & WorkManager | Konsumsi daya rendah (< 2%/hari) |
| **Kompatibilitas** | Target SDK 34, Min SDK 26 | Mendukung Android 8.0 s.d. Android 14+ |

---

## Panduan Pemasangan & Kompilasi

### Prasyarat Lingkungan
- Java JDK 17
- Android SDK (Platform 34 / Build Tools 34.0.0+)

### Menjalankan Unit Test
```powershell
.\gradlew.bat testDebugUnitTest
```

### Membangun Berkas APK Debug
```powershell
.\gradlew.bat assembleDebug
```
Berkas keluaran APK berada pada direktori:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## Pengujian Simulasi Suhu Tanpa Perangkat Fisik (ADB)

Respon alarm, grafik, dan kartu rekomendasi dapat disimulasikan menggunakan perintah `dumpsys battery`:

```bash
# 1. Atur suhu ke 38.5°C (Tingkat Hangat / Warm Alert)
adb shell dumpsys battery set temp 385

# 2. Atur suhu ke 41.5°C (Tingkat Panas / Hot Alert & Hotspot Warning)
adb shell dumpsys battery set temp 415

# 3. Atur suhu ke 44.0°C (Tingkat Kritis / Critical Alert)
adb shell dumpsys battery set temp 440

# 4. Kembalikan sensor baterai ke status fisik asli
adb shell dumpsys battery reset
```

---

## Matriks Perizinan Sistem

| Izin Sistem | Rationale & Kegunaan |
|---|---|
| `POST_NOTIFICATIONS` | Menampilkan suhu konstan pada status bar dan notifikasi peringatan suhu tinggi. |
| `FOREGROUND_SERVICE` | Menjalankan pemantauan latar belakang adaptif tanpa dihentikan sistem operasi. |
| `PACKAGE_USAGE_STATS` | Mengidentifikasi aplikasi dengan konsumsi prosesor tinggi saat terjadi kenaikan suhu. |
| `ACCESS_WIFI_STATE` & `ACCESS_NETWORK_STATE` | Memantau status penambatan hotspot untuk modul Hotspot Guard. |
| `SYSTEM_ALERT_WINDOW` | Menampilkan Floating Thermal HUD di atas aplikasi lain. |
| `RECEIVE_BOOT_COMPLETED` | Memulai kembali pemantauan setelah perangkat dinyalakan ulang. |

---

## Lisensi

Proyek ini dilisensikan di bawah ketentuan [MIT License](LICENSE).  
Hak Cipta (c) 2026 David Yehuda Surbakti.
