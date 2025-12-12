package com.cnn.mushroom.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mushrooms")
data class MushroomEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    val imagePath: String,
    val name: String,
    val topKNames: List<String>,
    val timestamp: Long,
    val confidenceScore: Float? = null,
    val isEdible: String
) {

}


