# Progress & Handoff — Duwitku

> File ini adalah "ingatan" proyek. Agent membacanya PERTAMA di setiap sesi dan memperbaruinya di AKHIR sesi.
> Dengan ini, sesi baru (atau model/tool lain) bisa melanjutkan tanpa membaca ulang semua dokumen.

## Status per milestone
| Milestone | Status | Catatan |
|-----------|--------|---------|
| M0 Persiapan | [ ] | |
| M1 Fondasi | [~] | M1a, M1b, M1c selesai. Build sukses. (Placeholder layar masih kosong, tapi fungsional navigasi berjalan). |
| M2 Domain & Data | [x] | M2a (model, formatter), M2b (Room: Entity, DAO, Database, DatabaseModule), M2c (TransactionMapper, TransactionRepository, TransactionRepositoryImpl, RepositoryModule binding) selesai. |
| M3 Form Transaksi | [~] | M3a (Komponen UI Form) selesai. Siap lanjut M3b (ViewModel). |
| M4 Daftar & Detail | [ ] | |
| M5 Beranda | [ ] | |
| M6 Statistik | [~] | M6a selesai (GetCategoryTotalsUseCase dan logika persentase). |
| M7 Kurs | [x] | Selesai (M7a dan M7b). Layar Kurs sudah terintegrasi dan bisa dikonversi secara real-time berdasarkan data API. |
| M8 Polishing | [ ] | |

Status: `[ ]` belum, `[~]` sebagian, `[x]` selesai & sudah dites di emulator.

## Sedang dikerjakan / titik berhenti terakhir
- Sub-task: Selesai M2b (Room) dan M2c (Repository transaksi). Entity, DAO, Database, DatabaseModule, TransactionMapper, TransactionRepositoryImpl, dan unit test.
- Berhenti di: Build `test assembleDebug` sukses.
- File terakhir diubah: `data/local/` (3 file), `di/DatabaseModule.kt`, `data/mapper/TransactionMapper.kt`, `data/repository/TransactionRepositoryImpl.kt`, `domain/repository/TransactionRepository.kt`, `di/RepositoryModule.kt`, 2 file test.
- Build terakhir: SUKSES (semua test lulus, 0 gagal).
- Keputusan: type/category disimpan sebagai String (tanpa TypeConverter); entity dengan type tidak dikenal dilewati (mapNotNull); category mismatch di-fallback ke OTHER_EXPENSE/OTHER_INCOME; save edit mempertahankan createdAt asli; DAO divalidasi lewat kompilasi KSP, test nyata via layar Form (M3c).

## Keputusan yang sudah diambil
- Package = `com.pemmob.duwitku`
- Versi AGP 9.x dengan Kotlin bawaan diatasi pakai `android.disallowKotlinSourceSets=false`.
- Hilt `2.60.1` menggunakan artefak `hilt-android-compiler`.
- Nama file font diubah dari format `-` kapital ke `_` lowercase agar lolos Android resource compiler.

## Masalah terbuka / TODO
- (kosong)

## File kunci yang sudah ada
- `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Spacing.kt`, `Theme.kt`
- `navigation/Routes.kt`, `BottomNavItem.kt`, `DuwitkuNavHost.kt`
- `DuwitkuApp.kt`, `MainActivity.kt`
- `domain/model/Transaction.kt`, `Category.kt`, dll.
- `util/CurrencyFormatter.kt`, `util/DateFormatter.kt`
- `ui/components/TypeToggle.kt`, `AmountTextField.kt`, `CategoryPicker.kt`, `DatePickerField.kt`, `DuwitkuTopBar.kt`, `FormComponentsPreview.kt`
- `data/remote/RateApi.kt`, `di/NetworkModule.kt`, `domain/repository/RateRepository.kt`
- `ui/feature/rates/RatesScreen.kt`, `RatesViewModel.kt`, `RatesUiState.kt`
- `data/local/TransactionEntity.kt`, `TransactionDao.kt`, `DuwitkuDatabase.kt`
- `di/DatabaseModule.kt`, `data/mapper/TransactionMapper.kt`
- `data/repository/TransactionRepositoryImpl.kt`, `domain/repository/TransactionRepository.kt`