# Design System & UI Spec — Duwitku

Sumber gaya: ikon aplikasi (`Ikon_Dompet_Ceria_Duwitku.png`) — dompet cokelat tersenyum, uang hijau, latar kuning krem, aksen kuning emas. Kesan: **hangat, ramah, ceria**, sudut membulat besar.

## 1. Warna

Nilai di bawah adalah perkiraan dari ikon. Boleh disesuaikan sedikit oleh tim, tapi tetap satu sumber kebenaran di `Color.kt`.

### Palet dasar (dari ikon)
| Token | Hex | Asal |
|-------|-----|------|
| Cream | `#FDDC87` | Latar ikon |
| Cream Soft | `#FFF6DC` | Turunan lebih terang untuk background |
| Brown | `#5A3420` | Dompet & teks logo |
| Green | `#3E8E4E` | Uang |
| Green Light | `#6FBF84` | Uang (terang) |
| Green Dark | `#2E6B3C` | Uang (gelap) |
| Amber | `#FBB92B` | Percikan |
| Coin | `#FDD05A` | Senyum & kancing |
| Expense Red | `#C0492B` | Tambahan: semantik pengeluaran |

### Light Scheme (Material 3)
| Role | Hex |
|------|-----|
| primary | `#5A3420` |
| onPrimary | `#FFF6DC` |
| primaryContainer | `#FDDC87` |
| onPrimaryContainer | `#3A1F10` |
| secondary | `#3E8E4E` |
| onSecondary | `#FFFFFF` |
| secondaryContainer | `#CDEBD3` |
| onSecondaryContainer | `#0F3A1B` |
| tertiary | `#FBB92B` |
| onTertiary | `#3A2A00` |
| background | `#FFF9EA` |
| onBackground | `#2B1B10` |
| surface | `#FFF9EA` |
| onSurface | `#2B1B10` |
| surfaceVariant | `#F6E9C8` |
| onSurfaceVariant | `#5C4A38` |
| outline | `#9A8468` |
| error | `#B3261E` |

### Dark Scheme
| Role | Hex |
|------|-----|
| primary | `#FDDC87` |
| onPrimary | `#3A1F10` |
| primaryContainer | `#5A3420` |
| onPrimaryContainer | `#FDDC87` |
| secondary | `#6FBF84` |
| onSecondary | `#0F3A1B` |
| secondaryContainer | `#2E6B3C` |
| onSecondaryContainer | `#CDEBD3` |
| tertiary | `#FBB92B` |
| background | `#1F1510` |
| onBackground | `#F3E6CF` |
| surface | `#1F1510` |
| onSurface | `#F3E6CF` |
| surfaceVariant | `#3A2B20` |
| onSurfaceVariant | `#D5C3A5` |
| outline | `#9A8468` |

### Warna semantik (di luar ColorScheme, buat `data class ExtendedColors` via `CompositionLocal`)
| Token | Terang | Gelap |
|-------|--------|-------|
| income | `#2E7D3E` | `#6FBF84` |
| expense | `#C0492B` | `#F2A38F` |

**Aturan:** nominal pemasukan selalu `income`, pengeluaran selalu `expense`. Jangan pernah mengandalkan warna saja: sertakan tanda `+` / `−` dan ikon.

## 2. Tipografi
- Font: **Nunito** (bundel di `res/font/`, bobot 400, 600, 700, 800). Bila file font belum ada, pakai `FontFamily.Default` dan jangan blokir pekerjaan.
- Skala memakai `Typography` M3 dengan override:

| Style | Ukuran / Bobot | Pakai untuk |
|-------|----------------|-------------|
| headlineLarge | 32 / ExtraBold | Saldo total |
| headlineSmall | 24 / Bold | Judul layar |
| titleMedium | 16 / SemiBold | Judul kartu, nama kategori |
| bodyLarge | 16 / Regular | Isi |
| bodyMedium | 14 / Regular | Catatan, subjudul |
| labelLarge | 14 / SemiBold | Tombol |
| labelSmall | 11 / Medium | Label kecil, tanggal |

## 3. Spacing, Shape, Elevasi
- Spacing (objek `Spacing`): `xs=4dp, sm=8dp, md=16dp, lg=24dp, xl=32dp`. Padding layar default `md`.
- Shape: small `12dp`, medium `20dp`, large `28dp`. Tombol dan chip `full rounded` (pill). Kartu `20dp`.
- Elevasi minim: gunakan perbedaan warna container, bukan bayangan tebal.

## 4. Komponen Reusable (`ui/components/`)
| Komponen | Keterangan |
|----------|------------|
| `DuwitkuTopBar` | `CenterAlignedTopAppBar` dengan judul, opsional tombol kembali |
| `BalanceCard` | Kartu besar `primaryContainer`: saldo, baris pemasukan & pengeluaran |
| `SummaryChip` | Ikon + label + nominal kecil (pemasukan/pengeluaran) |
| `TransactionItem` | Ikon kategori dalam lingkaran, nama kategori, catatan 1 baris, nominal berwarna, tanggal |
| `CategoryIcon` | Ikon + warna per kategori (pakai `Icons.Rounded.*` bawaan) |
| `AmountTextField` | `OutlinedTextField` angka, awalan "Rp", format ribuan saat mengetik |
| `TypeToggle` | `SingleChoiceSegmentedButtonRow` Pemasukan / Pengeluaran |
| `CategoryPicker` | `FlowRow` berisi `FilterChip` |
| `DatePickerField` | Field read-only yang membuka `DatePickerDialog` M3 |
| `LoadingView` | `CircularProgressIndicator` di tengah |
| `EmptyView` | Ikon + teks + (opsional) tombol aksi |
| `ErrorView` | Ikon + pesan + tombol "Coba lagi" |
| `ConfirmDialog` | `AlertDialog` untuk hapus |

Ikon kategori (rounded): Makan `Restaurant`, Transport `DirectionsBus`, Belanja `ShoppingBag`, Tagihan `Receipt`, Hiburan `SportsEsports`, Kesehatan `HealthAndSafety`, Pendidikan `School`, Lainnya `MoreHoriz`, Uang Saku `Savings`, Gaji `Payments`, Bonus `CardGiftcard`. (Bila ikon belum ada di paket bawaan, pilih padanan terdekat; jangan tambah library ikon.)

## 5. Spesifikasi Layar

### 5.1 Beranda
- TopBar: "Halo! 👋" + nama app. (Tanpa nama pengguna.)
- `BalanceCard`: "Saldo kamu" + nominal besar; di bawahnya dua `SummaryChip` (Pemasukan bulan ini, Pengeluaran bulan ini).
- Dua tombol berdampingan: **+ Pemasukan** (hijau) dan **+ Pengeluaran** (cokelat/merah) → buka Form dengan `type` terisi.
- Judul "Transaksi terbaru" + tombol teks "Lihat semua" (pindah ke tab Transaksi) + maksimal 5 `TransactionItem` (LazyColumn, atau seluruh layar sebagai satu `LazyColumn`).
- State: Loading, Empty ("Belum ada transaksi. Yuk catat yang pertama!"), Error.

### 5.2 Transaksi
- TopBar "Transaksi", field pencarian catatan (`OutlinedTextField`).
- Baris `FilterChip`: Semua / Pemasukan / Pengeluaran.
- `LazyColumn` dikelompokkan per tanggal (header sticky opsional). `key = { it.id }`.
- FAB "+" → Form tanpa argumen.
- Ketuk item → Detail.
- State: Loading, Empty (berbeda untuk "belum ada data" vs "hasil filter kosong"), Error.

### 5.3 Form Transaksi (tambah/edit)
- TopBar "Tambah Transaksi" atau "Edit Transaksi", tombol kembali.
- Urutan field: `TypeToggle` → `AmountTextField` → `CategoryPicker` (menyesuaikan jenis) → `DatePickerField` (default hari ini) → Catatan (opsional, maks 100 karakter) → tombol **Simpan**.
- Validasi: nominal > 0 dan ≤ 999.999.999.999; kategori wajib; tanggal tidak boleh lebih dari hari ini. Pesan error muncul di bawah field (`supportingText`).
- Tombol Simpan nonaktif saat `isSaving`. Setelah sukses: kembali ke layar sebelumnya.
- Ganti jenis → kategori terpilih direset bila tidak valid untuk jenis baru.
- Input dipertahankan saat rotasi.

### 5.4 Detail Transaksi
- Kartu besar: ikon kategori, nominal berwarna, kategori, tanggal (format "Senin, 4 Oktober 2026"), catatan.
- Tombol **Edit** (→ Form dengan `transactionId`) dan **Hapus** (→ `ConfirmDialog`, lalu kembali).
- State: Loading, Error/Tidak ditemukan.

### 5.5 Statistik
- Pemilih bulan: ikon panah kiri/kanan + teks "Oktober 2026".
- Ringkasan: total pengeluaran bulan itu.
- Daftar per kategori pengeluaran, urut terbesar: ikon, nama, nominal, persentase, `LinearProgressIndicator` (rounded).
- State: Loading, Empty ("Belum ada pengeluaran bulan ini"), Error.

### 5.6 Kurs
- TopBar "Kurs & Konverter" + tombol refresh.
- `OutlinedTextField` nominal Rupiah (default 100.000).
- `LazyColumn` kartu per mata uang (USD, EUR, SGD, JPY, MYR, AUD): kode, nama, kurs "1 USD = Rp …", hasil konversi.
- Tampilkan tanggal pembaruan kurs.
- State: Loading, Success, Error dengan pesan "Tidak bisa memuat kurs. Cek koneksi internetmu." + "Coba lagi".

### 5.7 Navigasi
Bottom `NavigationBar` 4 item: Beranda (`Home`), Transaksi (`ReceiptLong`), Statistik (`PieChart`/`BarChart`), Kurs (`CurrencyExchange`). Indikator item aktif memakai `primaryContainer`.

## 6. Ikon Aplikasi
- Sumber: `Ikon_Dompet_Ceria_Duwitku.png`. Buat **Adaptive Icon** lewat Android Studio: *Image Asset Studio* → Launcher Icons (Adaptive and Legacy).
- Disarankan: foreground = gambar dompet + uang (tanpa teks "Duwitku" agar terbaca di ukuran kecil), background = warna `#FDDC87`.
- Aktifkan *Monochrome layer* (themed icon) bila memungkinkan.
- `app_name` = "Duwitku".

## 7. Copywriting (nada bahasa)
Santai, hangat, singkat, pakai "kamu". Contoh: "Belum ada transaksi. Yuk catat yang pertama!", "Berhasil disimpan 🎉", "Yakin mau hapus transaksi ini?". Hindari jargon keuangan.

## 8. Aksesibilitas
- Area sentuh minimal 48dp.
- Ikon bermakna punya `contentDescription`; ikon dekoratif `null`.
- Kontras teks memenuhi M3 default; jangan pakai warna sebagai satu-satunya pembeda.
