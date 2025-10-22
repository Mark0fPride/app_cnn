package com.cnn.mushroom.data

import kotlinx.coroutines.flow.Flow

interface MushroomRepository {
    fun getAllMushrooms(): Flow<List<MushroomEntity>>
}

class DefaultMushroomRepository(
    private val mushroomDao: MushroomDao
) : MushroomRepository {

    override fun getAllMushrooms(): Flow<List<MushroomEntity>> =
        mushroomDao.getAllMushrooms()

}

