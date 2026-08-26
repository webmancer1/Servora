package com.example.servora.di

import android.content.Context
import androidx.room.Room
import com.example.servora.data.local.AlertDao
import com.example.servora.data.local.MetricDao
import com.example.servora.data.local.RemoteActionDao
import com.example.servora.data.local.ServerDao
import com.example.servora.data.local.ServoraDatabase
import com.example.servora.data.repository.AccountRepository
import com.example.servora.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ServoraDatabase =
        Room.databaseBuilder(context, ServoraDatabase::class.java, "servora.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideServerDao(database: ServoraDatabase): ServerDao = database.serverDao()

    @Provides
    fun provideMetricDao(database: ServoraDatabase): MetricDao = database.metricDao()

    @Provides
    fun provideAlertDao(database: ServoraDatabase): AlertDao = database.alertDao()

    @Provides
    fun provideRemoteActionDao(database: ServoraDatabase): RemoteActionDao = database.remoteActionDao()

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository {
        return AuthRepository(firebaseAuth)
    }

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Provides
    @Singleton
    fun provideAccountRepository(
        firestore: FirebaseFirestore,
        firebaseAuth: FirebaseAuth
    ): AccountRepository {
        return AccountRepository(firestore, firebaseAuth)
    }
}
