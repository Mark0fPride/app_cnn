package com.cnn.mushroom


import com.cnn.mushroom.data.MushroomEntity
import kotlinx.coroutines.delay


suspend fun classifyMushroom(path: String): MushroomEntity {
    delay(5000) // simulate CNN processing

    val name = "Test Mushroom"
    val timestamp = 1
    val confidence = (0 until 100).random().toFloat()

    return MushroomEntity(
        name = name,
        imagePath = path,
        timestamp = timestamp,
        confidenceScore = confidence,
        isEdible = true
    )
}
