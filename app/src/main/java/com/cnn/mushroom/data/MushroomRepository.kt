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
import androidx.core.net.toUri


interface MushroomRepository {
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
    suspend fun addMushroom(mushroomEntity: MushroomEntity)
    suspend fun deleteAllMushrooms()
    fun deleteMushroom(mushroomEntity: MushroomEntity)
    fun getMushroomById(id: Int): Flow<MushroomEntity?>
    fun getRecentMushroom(): Flow<MushroomEntity?>
    fun classifyMushroom(imagePath: Uri): Flow<MushroomEntity>
}

@Singleton
class DefaultMushroomRepository @Inject constructor(
    private val mushroomDao: MushroomDao,
    @ApplicationContext private val context: Context
) : MushroomRepository {

    // --- DAO methods ---
    override fun getAllMushrooms(): Flow<List<MushroomEntity>> =
        mushroomDao.getAllMushrooms()

    override suspend fun addMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.addMushroom(mushroomEntity)

    override suspend fun deleteAllMushrooms() =
        mushroomDao.deleteAllMushrooms()

    override fun deleteMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.deleteMushroom(mushroomEntity)

    override fun getMushroomById(id: Int): Flow<MushroomEntity?> =
        mushroomDao.getMushroomById(id)

    override fun getRecentMushroom(): Flow<MushroomEntity?> =
        mushroomDao.getRecentMushroom()

    override fun classifyMushroom(imagePath: Uri): Flow<MushroomEntity> = flow {
        try {
            Log.d("MushroomClassification", "Starting mushroom classification for image: $imagePath")

            // Otwieranie pliku z nazwami klas
            Log.d("MushroomClassification", "Loading class names from mushroom.txt")
            val classNames: Array<String> = context.assets.open("mushrooms.txt").bufferedReader().use {
                it.readText()
            }.split("\n").map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray()

            Log.d("MushroomClassification", "Loaded ${classNames.size} class names: ${classNames.joinToString(", ")}")

            // Ładowanie modelu
            Log.d("MushroomClassification", "Loading PyTorch model")
            val model: Module = LiteModuleLoader.load(assetFilePath("mushroom_classifier.ptl"))
            Log.d("MushroomClassification", "Model loaded successfully")

            // Przetwarzanie obrazu
            Log.d("MushroomClassification", "Loading and processing bitmap from: $imagePath")
            val bitmap = loadBitmapFromUri(context = context, uri = imagePath)
            if (bitmap == null) {
                Log.e("MushroomClassification", "Failed to decode bitmap from path: $imagePath")
                throw IOException("Failed to decode bitmap")
            }

            Log.d("MushroomClassification", "Original bitmap size: ${bitmap.width}x${bitmap.height}")
            val scaledBitmap = bitmap.scale(224, 224)
            Log.d("MushroomClassification", "Scaled bitmap size: ${scaledBitmap.width}x${scaledBitmap.height}")

            val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
            val std = floatArrayOf(0.229f, 0.224f, 0.225f)

            Log.d("MushroomClassification", "Converting bitmap to tensor")
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(scaledBitmap, mean, std)
            Log.d("MushroomClassification", "Tensor conversion completed")

            // Inferencja
            Log.d("MushroomClassification", "Running model inference")
            val output = model.forward(IValue.from(inputTensor))
            val scoresTensor = output.toTensor()
            val scores = scoresTensor.dataAsFloatArray

            Log.d("MushroomClassification", "Inference completed. Scores array size: ${scores.size}")
            Log.d("MushroomClassification", "Score values: ${scores.take(5).joinToString()}...") // Log first 5 scores

            val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
            val maxScore = scores[maxIndex]
            val predictedClassName = classNames[maxIndex]

            Log.d("MushroomClassification", "Prediction result - Index: $maxIndex, Class: $predictedClassName, Score: $maxScore")

            // Tworzenie obiektu MushroomEntity
            val confidencePercentage = maxScore * 100
            val isEdible = predictedClassName == "Prawdziwek"

            Log.d("MushroomClassification", "Creating MushroomEntity - Confidence: $confidencePercentage%, Edible: $isEdible")

            val mushroom = MushroomEntity(
                name = predictedClassName,
                imagePath = imagePath.toString(),
                timestamp = System.currentTimeMillis(),
                confidenceScore = confidencePercentage,
                isEdible = isEdible
            )

            Log.i("MushroomClassification", "Classification completed: $predictedClassName (${"%.2f".format(confidencePercentage)}% confidence)")
            emit(mushroom)

        } catch (e: Exception) {
            Log.e("MushroomClassification", "Error during mushroom classification: ${e.message}", e)
            throw e // Re-throw to maintain the error handling in the flow
        }
    }.flowOn(Dispatchers.IO)

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



}

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap {
    return context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it)
    } ?: throw IOException("Failed to decode bitmap from URI: $uri")
}



