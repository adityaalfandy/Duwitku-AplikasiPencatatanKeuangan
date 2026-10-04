# Progress & Handoff — Duwitku

> File ini adalah "ingatan" proyek. Agent membacanya PERTAMA di setiap sesi dan memperbaruinya di AKHIR sesi.
> Dengan ini, sesi baru (atau model/tool lain) bisa melanjutkan tanpa membaca ulang semua dokumen.

## Status per milestone
| Milestone | Status | Catatan |
|-----------|--------|---------|
| M0 Persiapan | [ ] | |
| M1 Fondasi | [~] | M1a, M1b, M1c selesai. Build sukses. (Placeholder layar masih kosong, tapi fungsional navigasi berjalan). |
| M2 Domain & Data | [~] | M2a (Model domain, formatter, test) selesai. Siap lanjut M2b (Room). |
| M3 Form Transaksi | [~] | M3a (Komponen UI Form) selesai. Siap lanjut M3b (ViewModel). |
| M4 Daftar & Detail | [ ] | |
| M5 Beranda | [ ] | |
| M6 Statistik | [ ] | |
| M7 Kurs | [x] | Selesai (M7a dan M7b). Layar Kurs sudah terintegrasi dan bisa dikonversi secara real-time berdasarkan data API. |
| M8 Polishing | [ ] | |

Status: `[ ]` belum, `[~]` sebagian, `[x]` selesai & sudah dites di emulator.

## Sedang dikerjakan / titik berhenti terakhir
- Sub-task: Selesai M7b (Integrasi UI Kurs: RatesUiState, RatesViewModel, RatesScreen). Layar kurs sudah menempati placeholder.
- Berhenti di: Build `assembleDebug` dan Unit Test M7b sukses (String.format locale-independent).
- File terakhir diubah: `RatesScreen.kt`, `RatesViewModel.kt`, `RatesUiState.kt`, `DuwitkuNavHost.kt`, `CurrencyFormatter.kt`, `RatesViewModelTest.kt`, komponen empty/loading/error.
- Build terakhir: SUKSES (0 errors).

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