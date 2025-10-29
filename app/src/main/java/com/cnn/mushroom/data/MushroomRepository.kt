package com.cnn.mushroom.data

import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

interface MushroomRepository {
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
    fun addMushroom(mushroomEntity: MushroomEntity)
    fun deleteAllMushrooms()
    fun deleteMushroom(mushroomEntity: MushroomEntity)
    fun getMushroomById(id: Int): Flow<MushroomEntity?>
    fun getRecentMushroom(): Flow<MushroomEntity?>
}

@Singleton
class DefaultMushroomRepository @Inject constructor(
    private val mushroomDao: MushroomDao
) : MushroomRepository {

    override fun getAllMushrooms(): Flow<List<MushroomEntity>> =
        mushroomDao.getAllMushrooms()

    override fun addMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.addMushroom(mushroomEntity)

    override fun deleteAllMushrooms() =
        mushroomDao.deleteAllMushrooms()

    override fun deleteMushroom(mushroomEntity: MushroomEntity) =
        mushroomDao.deleteMushroom(mushroomEntity)

    override fun getMushroomById(id: Int): Flow<MushroomEntity?> =
        mushroomDao.getMushroomById(id)

    override  fun getRecentMushroom(): Flow<MushroomEntity?> =
        mushroomDao.getRecentMushroom()
}


