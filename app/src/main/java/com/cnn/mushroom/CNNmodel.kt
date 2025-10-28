package com.cnn.mushroom

import com.cnn.mushroom.data.MushroomEntity


fun classifyMushroom(path: String): MushroomEntity {

    var name = "test name"

    val timestamp = 1
    val confidence = (0 until 100).random().toFloat()
    var mushroom = MushroomEntity(
        name = name,
        imagePath = path,
        timestamp = timestamp,
        confidenceScore = confidence,
        isEdible = true
    )
    return mushroom
}

