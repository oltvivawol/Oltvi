package com.oltvi.jefe.di

import com.oltvi.jefe.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object JefeModule {

    @Provides
    @Singleton
    @Named("geminiKey")
    fun provideGeminiKey(): String = BuildConfig.GEMINI_API_KEY
}
