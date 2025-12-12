package com.cnn.mushroom.data


import androidx.core.graphics.scale
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


interface MushroomRepository {
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
    suspend fun addMushroom(mushroomEntity: MushroomEntity)
    suspend fun deleteAllMushrooms()
    suspend fun deleteMushroom(mushroomEntity: MushroomEntity)
    fun getRecentMushroom(): Flow<MushroomEntity?>
    fun classifyMushroom(imagePath: Uri): Flow<MushroomEntity?>
    fun getCommonName(scientificName: String): String
    fun getMushroomsByDateRange(startDate: Long, endDate: Long): Flow<List<MushroomEntity>>
    suspend fun deleteMushroomsByIds(ids: List<Int>)
    fun getMushroomByIdSingle(mushroomID: Int): Flow<MushroomEntity?>
    suspend fun updateMushroom(entity: MushroomEntity)
    fun getEdibility(mushroomName: String): String

}

@Singleton
class DefaultMushroomRepository @Inject constructor(
    private val mushroomDao: MushroomDao,
    @ApplicationContext private val context: Context
) : MushroomRepository {

    private val classNames: List<String>
    private val mushroomTranslations: Map<String, String>
    private val mushroomEdibilityMap: Map<String, String> // Mapa nazwy naukowej → jadalność
    private val currentLanguageCode = context.resources.configuration.locales[0].language

    init {
        // Ładujemy wszystkie dane z pliku CSV
        val csvData = loadMushroomDataFromCsv()
        classNames = csvData.map { it.scientificName }
        mushroomTranslations = when (currentLanguageCode) {
            "pl" -> csvData.associate { it.scientificName to it.polishName }
            "en" -> csvData.associate { it.scientificName to it.englishName }
            else -> csvData.associate { it.scientificName to it.englishName }
        }
        mushroomEdibilityMap = csvData.associate {
            it.scientificName to translateEdibility(it.edibility, currentLanguageCode)
        }
    }

    /**
     * Klasa pomocnicza do przechowywania danych o grzybach z pliku CSV
     */
    private data class MushroomData(
        val scientificName: String,
        val polishName: String,
        val englishName: String,
        val edibility: String
    )

    /**
     * Ładuje wszystkie dane o grzybach z pliku CSV
     */
    private fun loadMushroomDataFromCsv(): List<MushroomData> {
        val mushroomDataList = mutableListOf<MushroomData>()

        try {
            val inputStream = context.assets.open("format21.csv")
            val reader = inputStream.bufferedReader()

            // Pomijamy nagłówek
            reader.readLine()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line?.split(",")

                if (tokens != null && tokens.size >= 4) {
                    val scientificName = tokens[0].trim()
                    val polishName = tokens[1].trim()
                    val englishName = tokens[2].trim()
                    val edibility = tokens[3].trim()

                    if (scientificName.isNotEmpty()) {
                        mushroomDataList.add(
                            MushroomData(scientificName, polishName, englishName, edibility)
                        )
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mushroomDataList
    }

    private fun translateEdibility(edibility: String, lang: String): String {
        return when (lang) {
            "pl" -> when (edibility.lowercase()) {
                "edible" -> "Jadalny"
                "poisonous" -> "Trujący"
                "unknown" -> "Nieznane"
                else -> edibility
            }

            "en" -> when (edibility.lowercase()) {
                "edible" -> "Edible"
                "poisonous" -> "Poisonous"
                "unknown" -> "Unknown"
                else -> edibility
            }

            else -> edibility
        }
    }

    override fun getEdibility(mushroomName: String): String {
        val edibility = mushroomEdibilityMap[mushroomName]
        return edibility ?: "Unknown"
    }

    override fun getCommonName(scientificName: String): String {
        val targetName = scientificName.trim()
        return mushroomTranslations[targetName] ?: scientificName
    }

    override fun classifyMushroom(imagePath: Uri): Flow<MushroomEntity?> = flow {
        try {
            Log.d("MushroomClassification", "Starting mushroom classification for image: $imagePath")

            // --- 1. Load class names ---
            Log.d("MushroomClassification", "Loaded ${classNames.size} class names")

            // --- 2. Load PyTorch Mobile model ---
            val model: Module = LiteModuleLoader.load(assetFilePath("mushroom_classifier.ptl"))
            Log.d("MushroomClassification", "Model loaded successfully")

            // --- 3. Load and preprocess bitmap ---
            val bitmap = loadBitmapFromUri(context = context, uri = imagePath)
                ?: throw IOException("Failed to decode bitmap from path: $imagePath")

            val scaledBitmap = bitmap.scale(224, 224)

            // --- 4. Konwertuj bitmapę do tensora z NORMALIZACJĄ ---
            // TensorImageUtils.convertBitmapToFloat32Tensor automatycznie:
            // 1. Dzieli przez 255 (zakres 0-1)
            // 2. Stosuje normalizację z podanymi mean/std
            // 3. Zwraca tensor w formacie NCHW

            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                scaledBitmap,
                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
                TensorImageUtils.TORCHVISION_NORM_STD_RGB
            )
            val data = inputTensor.dataAsFloatArray
            val minVal = data.minOrNull()
            val maxVal = data.maxOrNull()
            Log.d("MushroomClassification", "Input Tensor Range: min=$minVal, max=$maxVal")


            // --- 5. Forward pass ---
            val output = model.forward(IValue.from(inputTensor))
            val logits = output.toTensor().dataAsFloatArray

            Log.d("MushroomClassification", "Model output (logits) size: ${logits.size}")
            Log.d("MushroomClassification", "Logits sample (first 5): ${logits.take(5).joinToString()}")

            // --- 6. Stabilny softmax ---
            val maxLogit = logits.maxOrNull() ?: 0f
            val expLogits = logits.map { kotlin.math.exp(it - maxLogit) }
            val sumExp = expLogits.sum()
            val probabilities = expLogits.map { (it / sumExp).toFloat() }

            // --- 7. Find top class ---
            val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
            val maxScore = probabilities[maxIndex]
            val predictedScientificName = classNames[maxIndex]
            val confidencePercentage = maxScore * 100

            // Debug: wypisz top 5 predykcji
            Log.d("MushroomClassification", "Top 5 predictions:")

// 1. Sortowanie i wybieranie top 5 predykcji
            val top5Predictions = probabilities.mapIndexed { index, prob ->
                Pair(classNames[index], prob) // Tworzenie pary (Nazwa Klasy, Prawdopodobieństwo)
            }.sortedByDescending { it.second }
                .take(5)

// Inicjalizacja listy do przechowywania nazw klas od 2 do 5
            val topKClasses: List<String> = top5Predictions
                // 2. Pobranie predykcji od indeksu 1 do 4 (czyli top-2, top-3, top-4, top-5)
                .drop(1)
                .take(4)
                // 3. Wyodrębnienie samej nazwy klasy (first z pary Pair)
                .map { it.first }

// Wypisanie predykcji i użycie listy
            top5Predictions.forEachIndexed { i, pair ->
                Log.d("MushroomClassification",
                    "${i + 1}. ${pair.first}: ${pair.second * 100}%")
            }

// Debug: Wypisz stworzoną listę topKClasses
            Log.d("MushroomClassification", "topKClasses (Top 2-5): $topKClasses")


            // --- 8. Pobierz nazwę potoczną i informację o jadalności ---
            val commonName = getCommonName(predictedScientificName)
            val isEdible = getEdibility(predictedScientificName)

            Log.d("MushroomClassification", "Final prediction: $predictedScientificName ($commonName), " +
                    "Confidence: $confidencePercentage%, Edible: $isEdible")

            // --- 9. Low confidence check ---
            if (confidencePercentage < 0.1f) {
                Log.w("MushroomClassification", "Low confidence ($confidencePercentage%). Returning null.")
                emit(null)
                return@flow
            }

            // --- 10. Emit result ---
            val mushroom = MushroomEntity(
                name = predictedScientificName,
                imagePath = imagePath.toString(),
                topKNames = topKClasses,
                timestamp = System.currentTimeMillis(),
                confidenceScore = confidencePercentage,
                isEdible = isEdible
            )

            emit(mushroom)

        } catch (e: Exception) {
            Log.e("MushroomClassification", "Error during mushroom classification: ${e.message}", e)
            throw e
        }
    }.flowOn(Dispatchers.IO)


    // --- Reszta metody bez zmian ---
    private fun assetFilePath(modelName: String): String {
        Log.d("MushroomClassification", "Getting asset file path for: $modelName")
        val file = File(context.cacheDir, modelName)
        if (file.exists() && file.length() > 0) {
            Log.d("MushroomClassification", "Model file already exists in cache: ${file.absolutePath}")
            return file.absolutePath
        }

        Log.d("MushroomClassification", "Copying model from assets to cache")
        context.assets.open(modelName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                var totalBytes = 0
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                    totalBytes += read
                }
                outputStream.flush()
                Log.d("MushroomClassification", "Model copied successfully: $totalBytes bytes")
            }
            return file.absolutePath
        }
    }

    // --- Reszta metod DAO bez zmian ---
    override fun getAllMushrooms(): Flow<List<MushroomEntity>> =
        mushroomDao.getAllMushrooms()

    override suspend fun addMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.addMushroom(mushroomEntity)

    override suspend fun deleteAllMushrooms() =
        mushroomDao.deleteAllMushrooms()

    override suspend fun deleteMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.deleteMushroom(mushroomEntity)

    override fun getRecentMushroom(): Flow<MushroomEntity?> =
        mushroomDao.getRecentMushroom()

    override fun getMushroomsByDateRange(startDate: Long, endDate: Long): Flow<List<MushroomEntity>> {
        return mushroomDao.getMushroomsByDateRange(startDate, endDate)
    }

    override suspend fun deleteMushroomsByIds(ids: List<Int>) {
        return mushroomDao.deleteByIds(ids)
    }

    override fun getMushroomByIdSingle(mushroomID: Int): Flow<MushroomEntity?> {
        return mushroomDao.getMushroomByIdSingle(mushroomID)
    }

    override suspend fun updateMushroom(entity: MushroomEntity) {
        return mushroomDao.updateMushroom(entity)
    }
}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IOException("Failed to decode bitmap from URI: $uri")
}



