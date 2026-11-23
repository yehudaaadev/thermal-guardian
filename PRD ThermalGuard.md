# PRD: ThermalGuard (nama sementara)

> **Instruksi untuk AI agent:** Dokumen ini adalah spesifikasi lengkap. Kerjakan SEMUA fitur, dari awal sampai akhir, dalam satu alur kerja berkelanjutan. Jangan berhenti untuk meminta konfirmasi di antara langkah. Jangan membagi pekerjaan menjadi fase yang menunggu persetujuan. Ambil keputusan sendiri bila ada detail yang belum ditentukan, catat asumsinya di `DECISIONS.md`, lalu lanjut. Berhenti hanya bila seluruh kriteria selesai (bagian 10) sudah terpenuhi atau ada hambatan yang benar-benar tidak bisa diselesaikan sendiri.

## 1. Ringkasan

Aplikasi Android native (Kotlin) untuk memantau suhu HP, memperingatkan sebelum terlalu panas, mendeteksi sumber panas, dan memberi saran pendinginan. Target utama: Huawei P50 Pro (HarmonyOS/EMUI, tanpa Google Mobile Services). Aplikasi tidak boleh mengklaim bisa mendinginkan HP secara fisik.

## 2. Latar Belakang

HP pengguna sering panas saat hotspot, scrolling lama, telponan, dan main game, bahkan sampai merusak komponen. Pengguna tidak punya pendingin eksternal. Aplikasi ini jadi sistem peringatan dini dan pembantu mengurangi sumber panas.

## 3. Tujuan dan Metrik

- Suhu tampil real-time dan akurat.
- Peringatan muncul kurang dari 5 detik setelah melewati ambang.
- Konsumsi baterai aplikasi di bawah 2% per hari.
- Tidak crash selama service berjalan 24 jam.
- Cold start di bawah 2 detik.

## 4. Fitur (semua wajib dikerjakan)

1. **Monitor suhu real-time:** baca suhu baterai dari `ACTION_BATTERY_CHANGED` (`EXTRA_TEMPERATURE`, satuan 0,1°C). Tampilkan °C dengan opsi °F. Tambahkan status termal sistem dari `PowerManager.getCurrentThermalStatus()` (API 29+) beserta listener `OnThermalStatusChangedListener`, dan `getThermalHeadroom()` bila tersedia.
2. **Alarm suhu bertingkat:** tiga level (hangat, panas, kritis) dengan ambang yang bisa diatur (default 38/40/43°C). Notifikasi berbeda tiap level, ada opsi getar dan suara, serta cooldown agar tidak spam.
3. **Foreground service:** pemantauan tetap jalan di latar belakang dengan notifikasi permanen berisi suhu terkini. Interval baca adaptif (jarang saat dingin, rapat saat panas). Otomatis jalan lagi setelah reboot (`BOOT_COMPLETED`).
4. **Riwayat dan grafik:** simpan data di Room, grafik 1 jam / 24 jam / 7 hari, tampilkan suhu maksimum dan rata-rata. WorkManager membersihkan data lama (default simpan 30 hari).
5. **Saran pendinginan kontekstual:** notifikasi dengan aksi cepat sesuai konteks: turunkan kecerahan, kurangi refresh rate, matikan hotspot, hentikan charging, lepas casing, tutup aplikasi boros. Tombol aksi membuka layar pengaturan terkait.
6. **Hotspot Guard:** deteksi hotspot aktif, hitung durasinya, ingatkan istirahat (default tiap 30 menit atau saat suhu masuk level panas). Pintasan ke pengaturan hotspot.
7. **Pendeteksi sumber panas:** daftar aplikasi paling aktif saat HP panas via `UsageStatsManager` (butuh izin akses penggunaan), dengan penjelasan kenapa izin diminta. Tampilkan sebagai "kemungkinan penyebab".
8. **Mode game:** deteksi aplikasi game di foreground, pantau laju kenaikan suhu, beri peringatan dini bila naik cepat (misal lebih dari 1°C per 2 menit).
9. **Pemantau pengisian daya:** deteksi charging, peringatkan bila suhu tinggi saat mengisi atau saat charging sambil main game/hotspot.
10. **Info baterai:** persentase, status charging, tegangan, kesehatan baterai (`EXTRA_HEALTH`), dan teknologi baterai.
11. **Widget layar utama:** widget suhu terkini (Glance / App Widget) dengan warna sesuai level.
12. **Quick Settings Tile:** tile untuk mulai/stop pemantauan dan lihat suhu sekilas.
13. **Ekspor data:** ekspor riwayat suhu ke CSV dan bagikan lewat share sheet.
14. **Pengaturan:** ambang suhu, satuan, interval, retensi data, getar/suara, tema (terang/gelap/ikuti sistem), bahasa (Indonesia dan Inggris).
15. **Onboarding dan panduan Huawei:** layar awal yang meminta izin notifikasi dan membantu pengguna mematikan pembatasan baterai/auto-launch agar service tidak dibunuh sistem. Sediakan pintasan ke pengaturan yang relevan dengan fallback bila intent tidak tersedia.
16. **Statistik harian:** ringkasan tiap hari (berapa kali panas, durasi di atas ambang, jam paling panas).
17. **Aksesibilitas dan kualitas:** dukungan TalkBack, ukuran teks dinamis, kontras cukup, dan tidak ada string hardcoded.

### Di luar cakupan

- Klaim "mendinginkan HP", "membersihkan RAM", atau fitur gimmick lain.
- Mematikan hotspot atau mengubah pengaturan sistem otomatis tanpa aksi pengguna.
- Iklan dan pelacakan data pengguna.

## 5. Tech Stack

- Kotlin, Jetpack Compose, Material 3, Navigation Compose
- MVVM + Clean Architecture ringan, Coroutines + Flow
- Hilt untuk DI
- Room (riwayat), DataStore (pengaturan), WorkManager
- Grafik: komponen Compose custom (Canvas) atau library tanpa ketergantungan GMS
- minSdk 26, targetSdk terbaru yang stabil
- **Dilarang** bergantung pada Google Play Services/Firebase. Bila perlu layanan HMS, jadikan opsional.
- Izin: `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE` (+ tipe yang sesuai), `RECEIVE_BOOT_COMPLETED`, `PACKAGE_USAGE_STATS` (opsional, diminta kontekstual)

## 6. Struktur Proyek

Modul tunggal `app` dengan paket: `data` (room, datastore, repository), `domain` (model, usecase), `service` (monitor service, boot receiver), `ui` (screens, components, theme), `widget`, `util`.

## 7. Alur Pengguna

1. Onboarding: izin notifikasi, panduan pembatasan baterai, penjelasan singkat.
2. Layar utama: suhu besar, status termal, tombol mulai/stop, ringkasan baterai.
3. Suhu naik, notifikasi bertingkat muncul dengan saran dan aksi cepat.
4. Layar riwayat: grafik, statistik harian, ekspor CSV.
5. Layar penyebab: aplikasi paling aktif dan status hotspot/game/charging.
6. Pengaturan.

## 8. Risiko dan Mitigasi

| Risiko | Mitigasi |
| --- | --- |
| EMUI/HarmonyOS membunuh service latar belakang | Onboarding pembatasan baterai, foreground service, restart otomatis |
| Suhu baterai berbeda dari suhu CPU | Tampilkan juga status termal sistem; jelaskan di UI |
| Pemantauan boros baterai | Interval adaptif, hindari wake lock, batch penulisan ke database |
| Izin usage stats ditolak | Fitur 7 dan 8 degradasi dengan baik dan tetap menampilkan penjelasan |
| API termal tidak ada di perangkat | Deteksi ketersediaan dan fallback ke suhu baterai saja |

## 9. Pengujian

- Unit test: logika ambang, level alarm, perhitungan laju kenaikan, penghapusan data lama.
- Instrumented test dasar untuk Room dan service.
- Simulasi suhu tanpa HP asli: emulator + `adb shell dumpsys battery set temp 450` (satuan 0,1°C), reset dengan `adb shell dumpsys battery reset`.
- Sediakan skrip `scripts/simulate_temp.sh` untuk memutar skenario suhu naik-turun.

## 10. Kriteria Selesai

- Semua 17 fitur di bagian 4 berfungsi.
- Proyek bisa dibuild (`./gradlew assembleDebug`) tanpa error dan tanpa warning kritis.
- Unit test lulus.
- Tidak ada dependensi GMS/Firebase.
- `README.md` (cara build, cara simulasi suhu, daftar izin dan alasannya) dan `DECISIONS.md` (asumsi yang diambil) tersedia.
- APK debug berhasil dihasilkan.