package com.cnn.mushroom.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
    suspend fun deleteMushroom(mushroomEntity: MushroomEntity)
    @Query("SELECT * FROM mushrooms WHERE id = :id")
    fun getMushroomById(id: Int): Flow<MushroomEntity?>
    @Query("SELECT * FROM mushrooms ORDER BY timestamp DESC LIMIT 1")
    fun getRecentMushroom(): Flow<MushroomEntity?>
    @Query("SELECT * FROM mushrooms WHERE timestamp BETWEEN :from AND :to")
    fun getMushroomsByDateRange(from: Long, to: Long): Flow<List<MushroomEntity>>
    @Query("DELETE FROM mushrooms WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<Int>)
    @Query("SELECT * FROM mushrooms WHERE id = :i")
    fun getMushroomByIdSingle(i: Int) : Flow<MushroomEntity?>
    @Update
    suspend fun updateMushroom(entity: MushroomEntity)

}


