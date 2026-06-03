package com.oltvi.neural.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestoreSettings
import com.google.firebase.firestore.memoryCacheSettings
import com.google.firebase.firestore.persistentCacheSettings
import com.google.firebase.storage.FirebaseStorage
import com.oltvi.neural.BuildConfig
import com.oltvi.neural.ai.AgenteGuia
import com.oltvi.neural.data.NeuralRepository
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
    fun provideGeminiKey(): String = BuildConfig.GEMINI_API_KEY

    @Provides
    @Singleton
    fun provideAgenteGuia(
        @Named("neuralGeminiKey") geminiKey: String
    ): AgenteGuia = AgenteGuia(geminiKey)

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance().also { db ->
        db.firestoreSettings = firestoreSettings {
            setLocalCacheSettings(persistentCacheSettings {})
        }
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideNeuralRepository(
        firestore: FirebaseFirestore,
        auth: FirebaseAuth,
        storage: FirebaseStorage
    ): NeuralRepository = NeuralRepository(firestore, auth, storage)
}
