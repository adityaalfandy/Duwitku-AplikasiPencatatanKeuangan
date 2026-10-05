# Progress & Handoff — Duwitku

> File ini adalah "ingatan" proyek. Agent membacanya PERTAMA di setiap sesi dan memperbaruinya di AKHIR sesi.
> Dengan ini, sesi baru (atau model/tool lain) bisa melanjutkan tanpa membaca ulang semua dokumen.

## Status per milestone
| Milestone | Status | Catatan |
|-----------|--------|---------|
| M0 Persiapan | [ ] | |
| M1 Fondasi | [~] | M1a, M1b, M1c selesai. Build sukses. (Placeholder layar masih kosong, tapi fungsional navigasi berjalan). |
| M2 Domain & Data | [x] | M2a (model, formatter), M2b (Room: Entity, DAO, Database, DatabaseModule), M2c (TransactionMapper, TransactionRepository, TransactionRepositoryImpl, RepositoryModule binding) selesai. |
| M3 Form Transaksi | [x] | M3a, M3b, M3c selesai. Layar terhubung dengan NavHost. |
| M4 Daftar & Detail | [x] | M4a (Layar Daftar Transaksi dan filter/search) dan M4b (Detail Transaksi & Hapus) selesai. |
| M5 Beranda | [x] | M5a (HomeUiState, HomeViewModel, Komponen Kartu) dan M5b (HomeScreen, Integrasi Navigasi) selesai. |
| M6 Statistik | [x] | M6a dan M6b (Layar Statistik, ViewModel, Komponen) selesai. |
| M7 Kurs | [x] | Selesai (M7a dan M7b). Layar Kurs sudah terintegrasi dan bisa dikonversi secara real-time berdasarkan data API. |
| M8 Polishing | [ ] | |

Status: `[ ]` belum, `[~]` sebagian, `[x]` selesai & sudah dites di emulator.

## Sedang dikerjakan / titik berhenti terakhir
- Sub-task: Selesai M6b (Layar Statistik, ViewModel, Komponen, dan integrasi navigasi).
- Berhenti di: Build `test assembleDebug` sukses (107 unit tests passed).
- File terakhir diubah: `StatsUiState.kt`, `StatsViewModel.kt`, `StatsScreen.kt`, `MonthSelector.kt`, `CategoryTotalItem.kt`, `DuwitkuNavHost.kt`, `strings.xml`, `StatsViewModelTest.kt`, `06_PROGRESS.md`, `AGENTS.md`.
- Build terakhir: SUKSES (107 unit tests passed, 0 fail).
- Keputusan: Penggunaan retryTrigger berbasis MutableStateFlow untuk menangani onRetry() pada flatMapLatest; FakeTransactionRepository diperbarui agar mencakup method save/observeRecent/observeTotalBalance.

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
- `domain/usecase/ValidateTransactionInputUseCase.kt`
- `ui/feature/form/TransactionFormUiState.kt`, `TransactionFormViewModel.kt`