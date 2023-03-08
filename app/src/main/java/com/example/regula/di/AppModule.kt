package com.example.regula.di

import android.app.Application
import androidx.room.Room
import com.example.regula.data.local.RegulaDatabase
import com.example.regula.tools.OrientationSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    @Named("orientationSensor")
    fun provideOrientationSensor(app: Application): OrientationSensor {
        return OrientationSensor(app)
    }

    @Provides
    @Singleton
    fun providePointsDatabase(app: Application): RegulaDatabase {
        return Room.databaseBuilder(app, RegulaDatabase::class.java, "reguladb.db")
            .fallbackToDestructiveMigration().build()
    }
}