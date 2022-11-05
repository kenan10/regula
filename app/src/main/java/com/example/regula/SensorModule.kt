package com.example.regula

import android.app.Application
import com.example.regula.sensors.Accelerometer
import com.example.regula.sensors.Magnetometer
import com.example.regula.sensors.MeasurableSensor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SensorModule {
    @Provides
    @Singleton
    fun provideAccelerometer(app: Application): MeasurableSensor {
        return Accelerometer(app)
    }

    @Provides
    @Singleton
    fun provideMagnetometer(app: Application): MeasurableSensor {
        return Magnetometer(app)
    }
}