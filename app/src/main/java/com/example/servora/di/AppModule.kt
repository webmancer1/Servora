package com.example.servora.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.servora.data.repository.AuthRepository
import com.example.servora.data.repository.ServerRepository
import com.example.servora.data.repository.authDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideServerRepository(): ServerRepository {
        return ServerRepository()
    }

    @Provides
    @Singleton
    fun provideAuthDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.authDataStore
    }

    @Provides
    @Singleton
    fun provideAuthRepository(dataStore: DataStore<Preferences>): AuthRepository {
        return AuthRepository(dataStore)
    }
}
