# Data Model & API Contract — Duwitku

## 1. Domain Model

```kotlin
enum class TransactionType { INCOME, EXPENSE }

enum class Category(val type: TransactionType) {
    // Pengeluaran
    FOOD(EXPENSE), TRANSPORT(EXPENSE), SHOPPING(EXPENSE), BILLS(EXPENSE),
    ENTERTAINMENT(EXPENSE), HEALTH(EXPENSE), EDUCATION(EXPENSE), OTHER_EXPENSE(EXPENSE),
    // Pemasukan
    ALLOWANCE(INCOME), SALARY(INCOME), BONUS(INCOME), OTHER_INCOME(INCOME)
}
// Label tampilan Indonesia diambil dari strings.xml (mis. FOOD -> "Makan"), bukan hardcode di enum.

data class Transaction(
    val id: Long,              // 0 saat baru dibuat
    val type: TransactionType,
    val amount: Long,          // rupiah, > 0
    val category: Category,
    val date: LocalDate,
    val note: String           // boleh kosong, maks 100 karakter
)

data class MonthSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long          // income - expense
)

data class CategoryTotal(val category: Category, val total: Long, val percent: Float)

data class ExchangeRate(
    val currencyCode: String,  // "USD"
    val rateToIdr: Double,     // 1 USD = rateToIdr IDR
)
data class RatesResult(val date: LocalDate, val rates: List<ExchangeRate>)
```

Saldo total = seluruh pemasukan − seluruh pengeluaran (semua waktu).

## 2. Skema Room

**Tabel `transactions`**
| Kolom | Tipe | Catatan |
|-------|------|---------|
| id | INTEGER PK autoGenerate | |
| type | TEXT | `INCOME` / `EXPENSE` (TypeConverter dari enum atau simpan `name`) |
| amount | INTEGER | rupiah |
| category | TEXT | `Category.name` |
| dateEpochDay | INTEGER | `LocalDate.toEpochDay()`, diindeks |
| note | TEXT | default "" |
| createdAt | INTEGER | epoch millis, untuk urutan stabil |

Urutan default daftar: `dateEpochDay DESC, createdAt DESC`.

### DAO (minimum)
```kotlin
@Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC")
fun observeAll(): Flow<List<TransactionEntity>>

@Query("SELECT * FROM transactions ORDER BY dateEpochDay DESC, createdAt DESC LIMIT :limit")
fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

@Query("SELECT * FROM transactions WHERE dateEpochDay BETWEEN :start AND :end ORDER BY dateEpochDay DESC, createdAt DESC")
fun observeBetween(start: Long, end: Long): Flow<List<TransactionEntity>>

@Query("SELECT * FROM transactions WHERE id = :id")
fun observeById(id: Long): Flow<TransactionEntity?>

@Upsert suspend fun upsert(entity: TransactionEntity)
@Query("DELETE FROM transactions WHERE id = :id") suspend fun deleteById(id: Long)
```

### Repository (interface di domain)
```kotlin
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeByMonth(month: YearMonth): Flow<List<Transaction>>
    fun observeById(id: Long): Flow<Transaction?>
    fun observeTotalBalance(): Flow<Long>
    suspend fun save(transaction: Transaction)
    suspend fun delete(id: Long)
}
```
Hitungan ringkasan bulanan & total per kategori dilakukan di domain/use case dari daftar transaksi (sederhana dan mudah dites).

## 3. Aturan Validasi

| Field | Aturan | Pesan error (strings.xml) |
|-------|--------|---------------------------|
| amount | wajib, angka, > 0, ≤ 999.999.999.999 | "Nominal harus lebih dari 0" |
| category | wajib, harus sesuai jenis | "Pilih kategori dulu ya" |
| date | tidak lebih dari hari ini | "Tanggal tidak boleh di masa depan" |
| note | maks 100 karakter | "Maksimal 100 karakter" |

Validasi hidup di ViewModel (atau fungsi murni yang bisa dites), bukan di Composable.

## 4. REST API — Kurs Mata Uang

Layanan: **Frankfurter** (gratis, tanpa API key). Dokumentasi: https://frankfurter.dev

> Sebelum implementasi, **verifikasi dulu** endpoint & format respons dengan membuka URL contoh di browser atau dokumentasi resminya. Bila berbeda dari contoh di bawah, sesuaikan DTO dan perbarui dokumen ini.

**Base URL:** `https://api.frankfurter.dev/`

**Endpoint:**
```
GET v1/latest?base=USD&symbols=IDR
```
Strategi yang dipakai: ambil kurs **terhadap IDR untuk setiap mata uang** dengan satu panggilan `base=IDR` lalu dibalik (`1 / nilai`), atau satu panggilan per mata uang bila `base=IDR` bermasalah. Pilih salah satu dan catat di sini setelah diuji.

Contoh pemanggilan (satu panggilan, base IDR):
```
GET v1/latest?base=IDR&symbols=USD,EUR,SGD,JPY,MYR,AUD
```

Contoh respons (bentuk umum aktual dari API):
```json
{
  "amount": 1.0,
  "base": "IDR",
  "date": "2026-10-02",
  "rates": {
    "EUR": 5.0e-05,
    "JPY": 0.00878,
    "MYR": 0.00023,
    "SGD": 7.1e-05,
    "USD": 5.6e-05
  }
}
```
Catatan: AUD telah ditambahkan untuk menggantikan SAR yang tidak didukung.

**DTO:**
```kotlin
@Serializable
data class RateResponseDto(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
```
Gunakan `Json { ignoreUnknownKeys = true }`.

**Retrofit:**
```kotlin
interface RateApi {
    @GET("v1/latest")
    suspend fun getLatest(
        @Query("base") base: String = "IDR",
        @Query("symbols") symbols: String
    ): RateResponseDto
}
```

**Mapper:** `rateToIdr = 1.0 / rates[code]` (hindari pembagian dengan nol; abaikan kode yang hilang).

**Konversi di layar:** `hasil = nominalRupiah * rates[code]` (tampilkan hingga 2 desimal, JPY boleh 0–2).

**Penanganan error**
| Kondisi | Hasil UI |
|---------|----------|
| `UnknownHostException` / `IOException` | "Tidak bisa memuat kurs. Cek koneksi internetmu." |
| `SocketTimeoutException` | "Koneksi terlalu lama. Coba lagi ya." |
| `HttpException` (4xx/5xx) | "Layanan kurs sedang bermasalah. Coba lagi nanti." |
| Respons kosong / parsing gagal | "Data kurs tidak valid." |

Manifest: `<uses-permission android:name="android.permission.INTERNET" />` dan hanya HTTPS.
