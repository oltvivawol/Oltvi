package com.oltvi.neural.di

import com.oltvi.neural.ai.AgenteGuia
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NeuralModule {

    @Provides
    @Named("neuralGeminiKey")
    fun provideGeminiKey(): String = com.oltvi.neural.BuildConfig.GEMINI_API_KEY

    @Provides
    @Singleton
    fun provideAgenteGuia(
        @Named("neuralGeminiKey") geminiKey: String
    ): AgenteGuia = AgenteGuia(geminiKey)
}
