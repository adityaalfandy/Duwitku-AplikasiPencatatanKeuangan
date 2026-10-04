# PRD — Duwitku (Aplikasi Catatan Keuangan)

## 1. Ringkasan
**Duwitku** adalah aplikasi Android native untuk mencatat pemasukan dan pengeluaran harian dengan tampilan ceria dan sederhana. Fokusnya: **mencatat cepat, melihat ringkasan jelas**.

**Tagline:** "Catat duitmu, tenang hatimu."

## 2. Tujuan
1. Pengguna bisa mencatat transaksi dalam kurang dari 15 detik.
2. Pengguna langsung tahu saldo, total pemasukan, dan total pengeluaran bulan ini.
3. Pengguna tahu pengeluaran terbesarnya per kategori.
4. Proyek memenuhi ketentuan teknis tugas (lihat bagian 9).

## 3. Target Pengguna
Mahasiswa, pelajar, dan pekerja pemula (18–30 tahun) yang ingin disiplin mencatat uang tanpa aplikasi yang rumit.

## 4. Platform & Batasan Teknis
- Android native, Kotlin + Jetpack Compose, Material 3
- minSdk 26, targetSdk/compileSdk = versi stabil terbaru
- Hanya HP (portrait). Tablet/foldable bukan prioritas.
- **Offline-first**: semua data transaksi disimpan lokal. Internet hanya dipakai untuk fitur Kurs.
- Tanpa login, tanpa akun, tanpa backend sendiri.
- Izin (permission): hanya `INTERNET`.
- Bahasa UI: Indonesia. Mata uang utama: Rupiah (Rp), disimpan sebagai `Long` (tanpa desimal).

## 5. Fitur (MVP)

| ID | Fitur | Deskripsi |
|----|-------|-----------|
| F1 | Beranda | Saldo total, ringkasan bulan ini (pemasukan/pengeluaran), 5 transaksi terbaru, tombol cepat "+ Pemasukan" dan "+ Pengeluaran" |
| F2 | Tambah/Edit Transaksi | Form: jenis, nominal, kategori, tanggal, catatan. Validasi input |
| F3 | Daftar Transaksi | Semua transaksi, dikelompokkan/diurutkan terbaru, filter (Semua/Pemasukan/Pengeluaran), cari catatan |
| F4 | Detail Transaksi | Lihat detail, tombol Edit dan Hapus (dengan konfirmasi) |
| F5 | Statistik | Total per kategori bulan ini (progress bar + persentase), pilih bulan |
| F6 | Kurs & Konverter | Ambil kurs dari REST API, konversi nominal Rupiah ke USD, EUR, SGD, JPY, MYR, AUD |

### Kategori (tetap, bukan input bebas)
- Pengeluaran: Makan, Transport, Belanja, Tagihan, Hiburan, Kesehatan, Pendidikan, Lainnya
- Pemasukan: Uang Saku, Gaji, Bonus, Lainnya

## 6. Non-Goals (JANGAN dibuat)
Login/akun, sinkronisasi cloud, multi-dompet/rekening, anggaran (budget) per kategori, transaksi berulang, export PDF/Excel, notifikasi, widget, scan struk, grafik pakai library pihak ketiga, tablet layout, multi-bahasa, in-app purchase.

## 7. User Flow Utama
1. **Catat transaksi:** Beranda → tombol "+ Pengeluaran" → Form (jenis sudah terisi) → Simpan → kembali ke Beranda, saldo terbarui.
2. **Lihat riwayat:** Tab Transaksi → ketuk item → Detail → Edit/Hapus.
3. **Lihat statistik:** Tab Statistik → pilih bulan → lihat kategori terbesar.
4. **Cek kurs:** Tab Kurs → muncul loading → daftar kurs → isi nominal Rp → hasil konversi.

## 8. Daftar Layar
| Layar | Tipe | Keterangan |
|-------|------|------------|
| Beranda | Tab (bottom nav) | F1 |
| Transaksi | Tab | F3 |
| Statistik | Tab | F5 |
| Kurs | Tab | F6 |
| Form Transaksi | Non-tab | F2, menerima argumen `transaksiId?` dan `jenis?` |
| Detail Transaksi | Non-tab | F4, menerima argumen `transaksiId` |

Total 6 layar (syarat minimal 3 terpenuhi).

## 9. Pemetaan ke Ketentuan Proyek (7 materi, target semua)

| # | Materi | Diterapkan di |
|---|--------|---------------|
| 1 | UI & Layout Dasar | Semua layar: Column/Row/Box + Modifier rapi (Beranda: kartu saldo, ringkasan) |
| 2 | Material 3 | Tema warna & tipografi kustom, Button, OutlinedTextField, Card, FilterChip, AlertDialog |
| 3 | State & UDF | Form Transaksi (`rememberSaveable`, state hoisting), event naik / state turun di semua layar |
| 4 | Lazy Layouts | Daftar Transaksi & transaksi terbaru (`LazyColumn` dengan `key = { it.id }`), daftar kurs |
| 5 | Networking & API | Kurs & Konverter (Retrofit + Frankfurter API) |
| 6 | Arsitektur MVVM | `ViewModel` per layar + `UiState` sealed (Loading/Success/Error) |
| 7 | Navigation Compose | Type-safe routes (`@Serializable`), 4 tab bottom nav + Scaffold, kirim argumen ke Form & Detail |

## 10. Kriteria Sukses (Definition of Done proyek)
- Semua fitur F1–F6 berjalan di emulator dan HP fisik tanpa crash.
- Data tetap ada setelah aplikasi ditutup dan dibuka ulang.
- Rotasi layar tidak menghilangkan isi form (`rememberSaveable`).
- Setiap layar punya state Loading, Empty, dan Error yang ditangani.
- Mode terang dan gelap tampil benar.
- Kurs menampilkan pesan error ramah saat tanpa internet, dan aplikasi tetap bisa dipakai.
- `./gradlew assembleDebug` dan `./gradlew test` lulus.

## 11. Asumsi & Pertanyaan Terbuka (konfirmasi dengan tim)
- Apakah tim sudah belajar Room? Jika belum, mulai dengan repository in-memory (lihat dokumen arsitektur), lalu ganti ke Room.
- Nama package final (default dokumen: `com.duwitku`).
