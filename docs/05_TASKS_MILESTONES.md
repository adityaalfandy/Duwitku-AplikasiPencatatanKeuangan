# Rencana Kerja Bertahap — Duwitku

**Aturan main:** satu milestone per sesi AI. Setelah selesai: build, jalankan di emulator, cek acceptance criteria, lalu **commit Git** sebelum lanjut.

Prompt pembuka tiap sesi (tempel di awal):
> Baca `AGENTS.md` dan dokumen di `docs/` yang relevan. Kerjakan HANYA milestone **[Mx]** di `docs/05_TASKS_MILESTONES.md`. Jangan ubah file di luar scope. Jelaskan rencana singkat dulu, lalu implementasikan, lalu jalankan `./gradlew assembleDebug` dan perbaiki error.

---

## M0 — Persiapan (dikerjakan manual oleh manusia)
- [ ] Android Studio → New Project → *Empty Activity* (Compose), nama `Duwitku`, package `com.duwitku`, minSdk 26, Kotlin DSL.
- [ ] Pastikan build dan run default berhasil.
- [ ] `git init`, commit pertama, hubungkan ke repo tim.
- [ ] Salin `AGENTS.md` ke root, folder `docs/` ke root. Buat `CLAUDE.md` berisi satu baris `@AGENTS.md` (bila memakai Claude Code).
- [ ] Siapkan ikon (lihat bagian 6 di dokumen design) dan font Nunito (opsional).
- [ ] Sepakati dengan tim: nama package, Room atau in-memory, pembagian fitur.

## M1 — Fondasi
**Scope:** version catalog, dependensi, Hilt, tema, navigasi kerangka.
- Rapikan `libs.versions.toml`; tambah plugin: Hilt, KSP, kotlinx-serialization, Room.
- `DuwitkuApp` + `MainActivity` dengan Hilt.
- Tema M3 lengkap (Color, Type, Shape, Spacing, ExtendedColors) sesuai `03_DESIGN_SYSTEM_UI_SPEC.md`, light + dark.
- `Routes.kt`, `DuwitkuNavHost`, `Scaffold` + `NavigationBar` 4 tab dengan layar placeholder (teks nama layar), `TransactionFormRoute` dan `TransactionDetailRoute` placeholder.
- `strings.xml` awal.

**Acceptance criteria:**
- App berjalan, 4 tab bisa berpindah dan mempertahankan state.
- Bottom bar tersembunyi di layar Form/Detail.
- Tema terang & gelap tampil sesuai palet.
- Tidak ada warna/ukuran hardcode di luar folder `theme`.

## M2 — Domain & Data Layer
**Scope:** model, Room, repository, util.
- Model domain, enum `Category`, mapper.
- `TransactionEntity`, `TransactionDao`, `DuwitkuDatabase`, `DatabaseModule`.
- `TransactionRepository` + `TransactionRepositoryImpl`, `RepositoryModule`.
- `CurrencyFormatter` (Rp 1.250.000) dan `DateFormatter` (Locale id-ID).
- Unit test: `CurrencyFormatter`, mapper, validasi (bila sudah dibuat sebagai fungsi murni).

**Acceptance criteria:**
- Unit test lulus.
- Repository bisa menyimpan dan mengamati data (uji cepat lewat test atau debug).
- Layer `domain` tidak mengimpor `android.*`.

## M3 — Form Transaksi ★ (fitur "golden example")
**Scope:** layar Form lengkap + komponen reusable yang dipakai.
- Komponen: `TypeToggle`, `AmountTextField`, `CategoryPicker`, `DatePickerField`, `DuwitkuTopBar`.
- `TransactionFormViewModel` + `TransactionFormUiState`, baca argumen lewat `SavedStateHandle.toRoute`.
- Mode tambah & edit. Validasi sesuai dokumen data. Simpan ke repository.
- `@Preview` terang & gelap.

**Acceptance criteria:**
- Menyimpan transaksi valid → kembali ke layar sebelumnya, data tersimpan.
- Error per field tampil sesuai aturan; tombol nonaktif saat menyimpan.
- Rotasi layar mempertahankan input.
- Argumen `type` dari rute mengisi jenis awal.
- **Setelah milestone ini rapi, jadikan pola folder `feature/form` sebagai acuan** (tulis di `AGENTS.md` bagian "Golden example").

## M4 — Daftar & Detail Transaksi
**Scope:** layar Transaksi dan Detail.
- `TransactionItem`, `LoadingView`, `EmptyView`, `ErrorView`, `ConfirmDialog`.
- `TransactionListViewModel`: filter jenis + pencarian catatan (`combine` flow).
- `LazyColumn` dengan `key`, grup per tanggal, FAB tambah.
- Detail: tampilkan, Edit, Hapus dengan konfirmasi.

**Acceptance criteria:**
- Filter dan pencarian bekerja bersamaan.
- Empty state berbeda untuk data kosong vs hasil filter kosong.
- Hapus meminta konfirmasi, lalu kembali dan daftar terbarui otomatis.
- Item memakai `key = { it.id }`.

## M5 — Beranda
**Scope:** layar Beranda.
- `BalanceCard`, `SummaryChip`, dua tombol cepat, 5 transaksi terbaru, "Lihat semua".
- `HomeViewModel` menggabungkan saldo total + ringkasan bulan berjalan + transaksi terbaru.

**Acceptance criteria:**
- Saldo & ringkasan benar setelah tambah/ubah/hapus transaksi (uji manual + unit test use case ringkasan).
- Tombol cepat membuka Form dengan jenis terisi.
- Loading, Empty, Error tertangani.

## M6 — Statistik
**Scope:** layar Statistik.
- Pemilih bulan, total pengeluaran, daftar per kategori + persentase + progress bar.
- Use case `GetCategoryTotalsUseCase` + unit test.

**Acceptance criteria:**
- Persentase berjumlah ±100%, urut terbesar ke terkecil.
- Ganti bulan memperbarui data; bulan tanpa pengeluaran menampilkan Empty.

## M7 — Kurs & Konverter (Networking)
**Scope:** layar Kurs.
- `NetworkModule` (Retrofit, OkHttp, Json), `RateApi`, DTO, mapper, `RateRepositoryImpl` dengan `Result`.
- Verifikasi endpoint dulu (lihat `04_DATA_MODEL_API.md`).
- `RatesViewModel` + `RatesUiState` (Loading/Success/Error), input nominal, refresh.
- `LazyColumn` kartu mata uang.

**Acceptance criteria:**
- Mode pesawat → pesan error ramah + tombol "Coba lagi" berfungsi setelah internet kembali.
- Mengubah nominal langsung memperbarui hasil tanpa memanggil API ulang.
- Tanggal pembaruan kurs tampil.

## M8 — Polishing & Pengujian
- Telusuri semua layar: state Loading/Empty/Error, dark mode, ukuran font besar, rotasi.
- Ikon launcher (adaptive), `app_name`.
- Hapus kode mati, `TODO`, import tak terpakai; jalankan `./gradlew lint test assembleDebug`.
- Tambah unit test ViewModel kunci (Form, Home).
- Uji di HP fisik.
- Lengkapi `README.md` (deskripsi, fitur, cara build, screenshot, pembagian tugas tim).

**Acceptance criteria:** semua poin "Kriteria Sukses" di `01_PRD.md` bagian 10 terpenuhi.

## M9 — Rilis/Pengumpulan
- Build APK debug/release untuk dikumpulkan, cek ukuran dan bisa dipasang.
- Siapkan demo: alur catat → lihat riwayat → statistik → kurs.
- Siapkan tabel pemetaan 7 materi (bagian 9 PRD) untuk presentasi.

---

## Tips Kolaborasi Tim
- Satu branch per milestone/fitur (`feature/form`, `feature/rates`); merge ke `main` setelah build lulus.
- Setelah M1–M3 selesai (fondasi + golden example), M4–M7 bisa dikerjakan paralel oleh anggota berbeda karena tiap fitur punya folder sendiri.
- Hindari dua orang mengubah `Routes.kt`/`libs.versions.toml` bersamaan; tunjuk satu penanggung jawab.

## Prompt Perbaikan yang Berguna
- *Build error:* "Jalankan `./gradlew assembleDebug`, baca error, perbaiki akar masalahnya (jangan menekan warning atau menghapus fitur), ulangi sampai berhasil."
- *Review konsistensi:* "Bandingkan fitur `[x]` dengan pola `feature/form`. Daftar penyimpangan terhadap `AGENTS.md`, jangan ubah kode dulu."
- *Penjelasan untuk presentasi:* "Jelaskan bagaimana UDF diterapkan di `HomeScreen` dengan menunjuk baris kodenya."

---

## Pecahan sub-task (untuk sesi hemat token)
Satu sub-task = satu sesi pendek. Acceptance criteria milestone dicek setelah semua sub-task-nya selesai.

| Sub-task | Isi |
|----------|-----|
| M1a | Version catalog, dependensi, Hilt, `DuwitkuApp`, `MainActivity` |
| M1b | Tema M3: Color, Type, Shape, Spacing, ExtendedColors (terang + gelap) |
| M1c | `Routes`, `NavHost`, `Scaffold` + bottom bar + layar placeholder |
| M2a | Model domain, enum `Category`, `CurrencyFormatter`, `DateFormatter` + test |
| M2b | Room: Entity, DAO, Database, `DatabaseModule` |
| M2c | Mapper, `TransactionRepository` + Impl, `RepositoryModule` |
| M3a | Komponen form: `TypeToggle`, `AmountTextField`, `CategoryPicker`, `DatePickerField` |
| M3b | `TransactionFormViewModel`, `UiState`, validasi + test |
| M3c | `TransactionFormScreen`/`Content`, sambung ke navigasi, `@Preview` |
| M4a | `TransactionItem`, `LoadingView`/`EmptyView`/`ErrorView`, layar daftar + filter + cari |
| M4b | Layar Detail + `ConfirmDialog` hapus |
| M5a | `BalanceCard`, `SummaryChip`, `HomeViewModel` + use case ringkasan |
| M5b | `HomeScreen` lengkap (mengacu mockup) |
| M6a | Use case total per kategori + test |
| M6b | `StatsScreen` + pemilih bulan |
| M7a | `NetworkModule`, `RateApi`, DTO, mapper, `RateRepositoryImpl` |
| M7b | `RatesViewModel` + `RatesScreen` |
| M8 | Polishing, test, README (pecah per layar bila perlu) |