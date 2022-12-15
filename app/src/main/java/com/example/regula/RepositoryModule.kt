package com.example.regula

import com.example.regula.data.repository.PointsRepositoryImpl
import com.example.regula.domain.repository.PointsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserPointsRepository(userPointRepository: PointsRepositoryImpl): PointsRepository
}