package com.cnn.mushroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mushrooms")
data class MushroomEntity(

    val imagePath: String,
    val name: String,
    val timestamp: Int,
    val confidenceScore: Float? = null,
    val isEdible: Boolean? = null
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}
