package com.cnn.mushroom.data

import android.content.Context
import com.cnn.mushroom.data.repository.UserSettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideMushroomRepository(
        mushroomDao: MushroomDao,
        @ApplicationContext context: Context // <-- 1. Request the context here
    ): MushroomRepository {
        // 2. Pass the context to the repository's constructor
        return DefaultMushroomRepository(mushroomDao, context)
    }

    @Provides
    @Singleton
    fun provideUserSettingsRepository(
        @ApplicationContext context: Context // <-- 1. Request the context here
    ): UserSettingsRepository {
        // 2. Pass the context to the repository's constructor
        return UserSettingsRepository(context)
    }
}