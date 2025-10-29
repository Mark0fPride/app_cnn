package com.cnn.mushroom.data

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMushroomRepository(
        mushroomDao: MushroomDao
    ): MushroomRepository {
        return DefaultMushroomRepository(mushroomDao)
    }
}
