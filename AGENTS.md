# AGENTS.md — Duwitku

Aplikasi catatan keuangan Android native (Kotlin + Jetpack Compose + Material 3). Offline-first, tanpa login. Ini tugas proyek kampus, jadi kode harus **mudah dibaca dan dijelaskan**.

## Dokumen acuan (baca sebelum bekerja)
- `docs/01_PRD.md` — fitur, scope, non-goals
- `docs/02_TECH_STACK_ARCHITECTURE.md` — stack, package, aturan arsitektur
- `docs/03_DESIGN_SYSTEM_UI_SPEC.md` — warna, tipografi, komponen, spesifikasi layar
- `docs/04_DATA_MODEL_API.md` — model, Room, validasi, API kurs
- `docs/05_TASKS_MILESTONES.md` — urutan kerja dan acceptance criteria

Bila dokumen dan kode bertentangan, **tanyakan**, jangan menebak.

## Cara kerja
1. Kerjakan **satu milestone per sesi**. Jangan mengerjakan milestone lain atau fitur di luar PRD.
2. Jelaskan rencana singkat sebelum menulis kode.
3. Ubah hanya file yang perlu. Jangan refactor besar tanpa diminta.
4. Setelah selesai, jalankan `./gradlew assembleDebug` dan perbaiki error. Jalankan `./gradlew test` bila ada logika baru.
5. Bila butuh library, versi, atau keputusan yang tidak ada di dokumen: **berhenti dan tanya**.
6. Akhiri dengan ringkasan: file yang diubah, cara menguji, hal yang belum selesai.

## Stack (ringkas)
Kotlin, Compose + M3, MVVM + UDF, Hilt (KSP), Navigation Compose type-safe (`@Serializable`), Coroutines/Flow, Room (KSP), Retrofit + OkHttp + kotlinx.serialization, Gradle Kotlin DSL + version catalog. minSdk 26.

## Dilarang
XML layout, LiveData, RxJava, Gson/Moshi, kapt, library chart/UI pihak ketiga, `GlobalScope`, `!!`, hardcode string/warna/ukuran di Composable, mengimpor Entity/DTO ke layer `ui`, `android.*` di layer `domain`, menambah fitur yang tercantum di Non-Goals PRD.

## Aturan kode
- Identifier Inggris; teks UI Indonesia di `res/values/strings.xml`.
- Penamaan: `XxxScreen` (stateful) → `XxxContent` (stateless), `XxxViewModel`, `XxxUiState`.
- UiState layar data: `sealed interface` (`Loading`, `Success`, `Error`). Form: satu `data class`.
- ViewModel: `StateFlow` read-only ke UI; UI memakai `collectAsStateWithLifecycle()`.
- State turun, event naik (UDF). Composable tidak mengubah state ViewModel langsung.
- Setiap Composable layar/komponen punya `@Preview`. `modifier: Modifier = Modifier` selalu ada.
- `LazyColumn`/`LazyGrid` **wajib** `key` (dan `contentType` bila item beragam).
- Input pengguna di Composable pakai `rememberSaveable` atau di ViewModel (bertahan saat rotasi).
- Uang = `Long` rupiah, diformat lewat `CurrencyFormatter` (Locale id-ID). Tanggal domain = `LocalDate`.
- Semua panggilan jaringan di repository, dibungkus `Result`, error dipetakan ke pesan ramah.
- Nominal pemasukan warna `income`, pengeluaran `expense`, selalu dengan tanda +/− dan ikon.
- Komentar singkat dalam bahasa Indonesia hanya untuk logika yang tidak jelas dari kodenya.

## Perintah
```
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## Golden example
Setelah M3 selesai, pola `ui/feature/form/` menjadi acuan struktur, penamaan, dan gaya untuk fitur lain. Tiru polanya:
1. Pemisahan `XxxScreen` (stateful, inject ViewModel) dan `XxxContent` (stateless, nerima state & event lambdas).
2. `UiState` adalah `data class` atau `sealed interface` yang HANYA memuat tipe domain/primitif, bukan string UI atau context.
3. Validasi input kompleks dipisahkan ke use case Kotlin murni (mis. `ValidateTransactionInputUseCase`).
4. Semua state UI mengalir dari `_uiState.update {}` secara fungsional di ViewModel.
5. Pemetaan dari error type ke string resource (pesan ramah pengguna) dilakukan *di dalam* layer UI, misal melalui helper function `@Composable`.
6. Unit testing ViewModel menguji berbagai interaksi, state perantara, dan dependensi (repositori, clock) dengan Dispatcher Test.

## Status proyek
Isi/perbarui bagian ini di akhir tiap sesi:
- Milestone selesai: (belum ada yang utuh, progres berjalan)
- Sedang dikerjakan: M1c (Navigasi & Layar Placeholder)
- Catatan/keputusan penting: AGP 9.x konflik KSP diatasi dengan `android.disallowKotlinSourceSets=false` di gradle.properties. Hilt compiler memakai `hilt-android-compiler` versi `2.60.1`. Font di-rename ke standar snake_case agar lolos resource compilation.

## Lingkungan kerja
- Kode ditulis lewat agent di Antigravity; aplikasi dijalankan, di-sync, dan diuji manusia di **Android Studio** (proyek yang sama, folder yang sama).
- Jangan mengubah `local.properties`, `.idea/`, atau file `build/`.
- Jangan menjalankan emulator/perangkat. Build cukup lewat `./gradlew assembleDebug` (Windows: `gradlew.bat assembleDebug`).
- Bila Gradle gagal karena lingkungan (JAVA_HOME, SDK tidak ditemukan, tidak ada internet), **berhenti**, jelaskan masalahnya, dan minta aku menjalankan Sync/Build di Android Studio lalu menempelkan error-nya. Jangan mengutak-atik konfigurasi sistem.
- Setiap kali menambah/mengubah file `.gradle.kts` atau `libs.versions.toml`, ingatkan aku untuk menekan **Sync Project with Gradle Files**.

## Hemat token (WAJIB)
- Awal sesi: baca `docs/06_PROGRESS.md` dulu. Baca dokumen lain **hanya** sesuai peta di bawah, bukan semuanya.
- Kerjakan satu sub-task per sesi (lihat "Pecahan sub-task" di `docs/05_TASKS_MILESTONES.md`).
- Jangan menampilkan isi file panjang di balasan; sebut nama file saja.
- Edit bagian yang perlu, jangan menulis ulang file utuh. Jangan memindai seluruh proyek.
- Jalankan build sekali di akhir sub-task, bukan setelah tiap file.
- Penjelasan singkat: maksimal ~10 baris di akhir. Tanpa pengulangan isi dokumen.
- Jika kuota/konteks hampir habis: perbarui `docs/06_PROGRESS.md` lebih dulu (titik berhenti yang jelas), baru berhenti.
- Akhir sesi selalu perbarui `docs/06_PROGRESS.md`.

## Peta dokumen per sub-task
| Pekerjaan | Baca |
|-----------|------|
| Gradle, DI, struktur, navigasi | `docs/02` |
| Tema, komponen, tampilan layar | `docs/03` (hanya bagian layar terkait) |
| Model, Room, validasi, API | `docs/04` |
| Cakupan fitur / non-goals | `docs/01` (hanya bila ragu) |

## Aturan versi library
- Jangan menebak atau menyalin versi dari hasil pencarian tanpa verifikasi. Pastikan versinya benar-benar ada di Maven Central / Google Maven (`maven-metadata.xml`) sebelum menulisnya ke `libs.versions.toml`.
- Proyek memakai AGP 9 (Kotlin bawaan): jangan tambahkan plugin `kotlin.android`. Lihat `docs/02` bagian 6.
- Bila build gagal dua kali karena masalah versi/plugin yang sama: berhenti, tulis ringkasan masalah di `docs/06_PROGRESS.md`, dan tanya aku.