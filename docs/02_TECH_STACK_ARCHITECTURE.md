# Tech Stack & Arsitektur — Duwitku

## 1. Tech Stack (WAJIB, jangan tambah library lain tanpa bertanya)

| Kebutuhan | Pilihan |
|-----------|---------|
| Bahasa | Kotlin |
| UI | Jetpack Compose + Material 3 (BOM Compose) |
| Arsitektur | MVVM + UDF, 3 layer (ui / domain / data) |
| Navigasi | Navigation Compose, **type-safe** (`@Serializable` routes) |
| DI | Hilt (dengan KSP, bukan kapt) |
| Async | Coroutines + Flow |
| State di UI | `StateFlow` + `collectAsStateWithLifecycle()` |
| Networking | Retrofit + OkHttp (+ logging interceptor hanya di debug) |
| Serialization | kotlinx.serialization (+ retrofit kotlinx-serialization converter) |
| Database | Room (KSP) |
| Build | Gradle Kotlin DSL + **Version Catalog** (`gradle/libs.versions.toml`) |
| Testing | JUnit4, kotlinx-coroutines-test, (opsional) Turbine |

**Dilarang:** XML layout, LiveData, RxJava, Gson/Moshi, kapt, Dagger manual, library chart pihak ketiga, library UI selain Material 3, `GlobalScope`, `!!` (non-null assertion), hardcode string di Composable.

**Versi library:** gunakan versi stabil terbaru yang kompatibel, tulis semuanya di `libs.versions.toml`. Jangan menebak versi dari ingatan; cek lewat Android Studio / dokumentasi resmi. Jangan pakai API yang deprecated.

## 2. Struktur Package

```
com.duwitku/
├── DuwitkuApp.kt                  (@HiltAndroidApp)
├── MainActivity.kt                (@AndroidEntryPoint, setContent)
├── data/
│   ├── local/
│   │   ├── DuwitkuDatabase.kt
│   │   ├── TransactionDao.kt
│   │   └── TransactionEntity.kt
│   ├── remote/
│   │   ├── RateApi.kt             (Retrofit interface)
│   │   └── RateResponseDto.kt
│   ├── mapper/                    (Entity/DTO <-> domain model)
│   └── repository/
│       ├── TransactionRepositoryImpl.kt
│       └── RateRepositoryImpl.kt
├── domain/
│   ├── model/                     (Transaction, TransactionType, Category, MonthSummary, ExchangeRate)
│   ├── repository/                (interface TransactionRepository, RateRepository)
│   └── usecase/                   (hanya jika logika > 1 baris, mis. GetMonthSummaryUseCase)
├── di/                            (DatabaseModule, NetworkModule, RepositoryModule)
├── navigation/
│   ├── Routes.kt                  (@Serializable)
│   ├── DuwitkuNavHost.kt
│   └── BottomNavItem.kt
├── ui/
│   ├── theme/                     (Color.kt, Type.kt, Shape.kt, Theme.kt)
│   ├── components/                (komponen reusable: lihat dokumen design)
│   └── feature/
│       ├── home/        HomeScreen, HomeViewModel, HomeUiState
│       ├── transactions/ TransactionListScreen, TransactionListViewModel, TransactionListUiState
│       ├── form/        TransactionFormScreen, TransactionFormViewModel, TransactionFormUiState
│       ├── detail/      TransactionDetailScreen, TransactionDetailViewModel, TransactionDetailUiState
│       ├── stats/       StatsScreen, StatsViewModel, StatsUiState
│       └── rates/       RatesScreen, RatesViewModel, RatesUiState
└── util/                          (CurrencyFormatter, DateFormatter)
```

## 3. Aturan Arsitektur

### Alur data (UDF)
```
UI (Composable) --event--> ViewModel --> Repository --> Room / Retrofit
UI <--UiState (StateFlow)-- ViewModel <--Flow-- Repository
```
- **State turun, event naik.** Composable tidak boleh mengubah state milik ViewModel secara langsung.
- Setiap layar punya pasangan: `XxxScreen` (stateful, ambil ViewModel) → `XxxContent` (stateless, menerima `uiState` + lambda) agar mudah di-`@Preview`.
- Layer `ui` hanya mengenal model `domain`, **tidak boleh** mengimpor Entity Room atau DTO.
- Layer `domain` murni Kotlin (tanpa import `android.*`).

### UiState
Layar yang memuat data (Home, TransactionList, Stats, Rates, Detail):
```kotlin
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(/* data layar */) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
```
Layar form memakai satu `data class TransactionFormUiState(...)` (field, error per field, `isSaving`, `isSaved`).

### ViewModel
- Diekspos: `val uiState: StateFlow<...>` (read-only). `MutableStateFlow` bersifat `private`.
- Flow dari Room diubah dengan `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Loading)`.
- Aksi dari UI dibuat sebagai fungsi publik (`onAmountChange`, `onSaveClick`, dst.).
- ViewModel tidak menyimpan referensi `Context`.

### Navigasi (type-safe)
```kotlin
@Serializable data object HomeRoute
@Serializable data object TransactionsRoute
@Serializable data object StatsRoute
@Serializable data object RatesRoute
@Serializable data class TransactionFormRoute(val transactionId: Long? = null, val type: String? = null)
@Serializable data class TransactionDetailRoute(val transactionId: Long)
```
- Argumen dibaca di ViewModel lewat `SavedStateHandle.toRoute<...>()`.
- `Scaffold` + `NavigationBar` hanya muncul di 4 rute tab; disembunyikan di Form dan Detail.
- Gunakan `launchSingleTop`, `restoreState`, dan `saveState` untuk perpindahan tab.

### Networking
- Base URL & endpoint ada di `NetworkModule` / `RateApi`, bukan tersebar.
- Panggilan jaringan dibungkus `try/catch` di repository dan dikembalikan sebagai `Result<T>`; ViewModel memetakan ke `UiState.Error` dengan pesan ramah (tanpa internet, timeout, error server).
- Timeout OkHttp 15 detik.

### Database
- `TransactionDao` mengembalikan `Flow<List<TransactionEntity>>` untuk daftar; fungsi tulis memakai `suspend`.
- `exportSchema = true` atau `false` — konsisten, versi awal `version = 1`.

### Fallback bila tim belum belajar Room
Buat `InMemoryTransactionRepository` (implementasi `TransactionRepository` dengan `MutableStateFlow<List<Transaction>>`). Karena UI hanya tahu interface, penggantian ke Room nanti tidak mengubah layer UI.

## 4. Konvensi Kode
- Nama: `HomeScreen`, `HomeViewModel`, `HomeUiState`, `TransactionRepository`, `TransactionRepositoryImpl`, `TransactionEntity`, `RateResponseDto`.
- Identifier kode dalam bahasa Inggris; teks UI dalam bahasa Indonesia di `res/values/strings.xml`.
- Satu Composable publik per file bila besar; Composable kecil privat boleh di file yang sama.
- Setiap Composable layar/komponen wajib punya minimal satu `@Preview` (terang) dan idealnya satu gelap.
- Parameter `modifier: Modifier = Modifier` selalu ada dan menjadi parameter opsional pertama.
- Daftar Lazy **wajib** memakai `key` (dan `contentType` bila item beragam).
- Uang: `Long` (rupiah). Tanggal: `java.time.LocalDate` di domain; di Room simpan sebagai epoch day (`Long`) atau epoch millis. Tampilan lewat `CurrencyFormatter` dan `DateFormatter` (Locale `id-ID`).
- Tidak ada magic number untuk spacing/warna: pakai token dari tema.

## 5. Perintah Build & Test
```
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

## 6. Catatan Kompatibilitas Build (AGP 9 + Kotlin bawaan)
Proyek dibuat dari template Android Studio terbaru (AGP 9.x, Gradle 9.x). Konsekuensinya:
- **Jangan** menerapkan plugin `org.jetbrains.kotlin.android` / alias `kotlin.android`. AGP 9 sudah membawa Kotlin bawaan; menambahkan plugin itu menimbulkan error "already on the classpath". Plugin `kotlin.plugin.compose` dan `kotlin.plugin.serialization` tetap dipakai.
- **Hilt**: pakai Hilt versi 2.59 ke atas (dukungan AGP 9 untuk plugin Gradle baru ada sejak 2.59; 2.59.2 diketahui ada). Jangan memakai angka versi yang tidak terverifikasi.
- **KSP**: pakai rilis KSP terbaru yang kompatibel dengan versi Kotlin proyek (KSP versi baru memakai penomoran sendiri, bukan lagi `<kotlin>-<ksp>`). Cek halaman rilis resmi KSP.
- Bila KSP/Room/Hilt tetap gagal karena Kotlin bawaan (ada laporan bug KSP + AGP 9 built-in Kotlin), jalur darurat: kembali ke mode klasik lewat `gradle.properties` (`android.builtInKotlin=false`, `android.newDsl=false`) dan terapkan lagi plugin `kotlin.android`. Pilih jalur ini hanya setelah versi terbaru dicoba.
- **Verifikasi setiap versi sebelum dipakai:** buka `maven-metadata.xml` di Maven Central / Google Maven (atau panel Dependencies di Android Studio) dan pastikan angka versinya ada. Jangan menyalin angka dari ringkasan hasil pencarian.