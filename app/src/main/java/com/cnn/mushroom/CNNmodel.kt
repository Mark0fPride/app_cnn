package com.cnn.mushroom
import android.content.Context
import android.graphics.BitmapFactory
import com.cnn.mushroom.data.MushroomEntity
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import androidx.core.graphics.scale

fun assetFilePath(context: Context, assetName: String): String {
    val file = File(context.filesDir, assetName)
    if (file.exists() && file.length() > 0) {
        return file.absolutePath
    }
    context.assets.open(assetName).use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }
            outputStream.flush()
        }
    }
    return file.absolutePath
}


suspend fun classifyMushroom(context: Context, imagePath: String): MushroomEntity {

    val model: Module = LiteModuleLoader.load(assetFilePath(context, "model_mobile.ptl"))
    val bitmap = BitmapFactory.decodeFile(imagePath)
    val resizedBitmap = bitmap.scale(224, 224)

    // 2. Przygotuj tensor wejściowy
    // Normalizacja musi być identyczna jak podczas treningu!
    // Poniżej standardowe wartości dla modeli trenowanych na ImageNet. Dopasuj je!
    val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    val std = floatArrayOf(0.229f, 0.224f, 0.225f)
    val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(resizedBitmap, mean, std)

    // 3. Wykonaj inferencję
    val outputTensor = model.forward(IValue.from(inputTensor)).toTensor()
    val scores = outputTensor.dataAsFloatArray

    // 4. Przetwórz wyniki
    // 'scores' to tablica z wynikami dla każdej klasy.
    // Musisz znaleźć indeks z najwyższą wartością.
    var maxScore = -Float.MAX_VALUE
    var maxScoreIdx = -1
    for (i in scores.indices) {
        if (scores[i] > maxScore) {
            maxScore = scores[i]
            maxScoreIdx = i
        }
    }

    val classNames = context.assets.open("mushroom.txt").bufferedReader().use { it.readText() }
        .split("\n")
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .toTypedArray()

    val predictedClassName = classNames[maxScoreIdx]

    return MushroomEntity(
        name = predictedClassName,
        imagePath = imagePath,
        timestamp = System.currentTimeMillis(),
        confidenceScore = maxScore * 100, // Możesz chcieć zastosować funkcję softmax
        isEdible = predictedClassName == "Prawdziwek" // Logika na podstawie nazwy
    )
}