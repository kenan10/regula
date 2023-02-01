package com.example.regula.di

import android.app.Application
import androidx.room.Room
import com.example.regula.data.local.RegulaDatabase
import com.example.regula.sensors.Accelerometer
import com.example.regula.sensors.Magnetometer
import com.example.regula.sensors.MeasurableSensor
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
    @Named("accelerometer")
    fun provideAccelerometer(app: Application): MeasurableSensor {
        return Accelerometer(app)
    }

    @Provides
    @Singleton
    @Named("magnetometer")
    fun provideMagnetometer(app: Application): MeasurableSensor {
        return Magnetometer(app)
    }

    @Provides
    @Singleton
    fun providePointsDatabase(app: Application): RegulaDatabase {
        return Room.databaseBuilder(app, RegulaDatabase::class.java, "reguladb.db")
            .fallbackToDestructiveMigration().build()
    }
}