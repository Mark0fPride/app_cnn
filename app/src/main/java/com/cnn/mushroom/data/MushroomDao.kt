package com.cnn.mushroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MushroomDao{
    @Query("SELECT * FROM mushrooms")
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addMushroom(mushroomEntity: MushroomEntity)
    @Query("Delete FROM mushrooms")
    suspend fun deleteAllMushrooms()
    @Delete
    fun deleteMushroom(mushroomEntity: MushroomEntity)
    @Query("SELECT * FROM mushrooms WHERE id = :id")
    fun getMushroomById(id: Int): Flow<MushroomEntity?>
    @Query("SELECT * FROM mushrooms ORDER BY timestamp DESC LIMIT 1")
    fun getRecentMushroom(): Flow<MushroomEntity?>
}


