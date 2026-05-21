package com.oltvi.conductor.di

import com.oltvi.conductor.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConductorModule {

    @Provides
    @Singleton
    @Named("geminiKey")
    fun provideGeminiKey(): String = BuildConfig.GEMINI_API_KEY
}
