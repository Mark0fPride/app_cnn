package com.cnn.mushroom.data

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MushroomDao{
    @Query("SELECT * FROM mushrooms")
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
}