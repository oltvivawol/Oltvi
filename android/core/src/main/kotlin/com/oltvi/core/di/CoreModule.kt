package com.oltvi.core.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.oltvi.core.ai.agents.*
import com.oltvi.core.ai.orchestrator.OltviOrchestrator
import com.oltvi.core.data.services.LocationService
import com.oltvi.core.data.services.MockDataService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModule {

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext ctx: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)

    // Each app module must provide @Named("geminiKey") from its own BuildConfig.
    // mockMode is auto-derived: blank or "mock" key → mock responses, real key → Gemini AI.

    @Provides @Singleton
    fun provideMatchmaker(@Named("geminiKey") key: String): AgenteMatchmaker =
        AgenteMatchmaker(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun providePrecio(@Named("geminiKey") key: String): AgentePrecio =
        AgentePrecio(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideSeguridad(@Named("geminiKey") key: String): AgenteSeguridad =
        AgenteSeguridad(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideRuta(@Named("geminiKey") key: String): AgenteRuta =
        AgenteRuta(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideSoporte(@Named("geminiKey") key: String): AgenteSoporte =
        AgenteSoporte(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideFraude(@Named("geminiKey") key: String): AgenteFraude =
        AgenteFraude(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideCopiloto(@Named("geminiKey") key: String): AgenteCopiloto =
        AgenteCopiloto(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideOperaciones(@Named("geminiKey") key: String): AgenteOperaciones =
        AgenteOperaciones(key, key.isBlank() || key == "mock")

    @Provides @Singleton
    fun provideOrchestrator(
        matchmaker: AgenteMatchmaker,
        precio: AgentePrecio,
        seguridad: AgenteSeguridad,
        ruta: AgenteRuta,
        soporte: AgenteSoporte,
        fraude: AgenteFraude,
        copiloto: AgenteCopiloto,
        operaciones: AgenteOperaciones
    ): OltviOrchestrator = OltviOrchestrator(
        matchmaker, precio, seguridad, ruta, soporte, fraude, copiloto, operaciones
    )

    @Provides @Singleton
    fun provideMockDataService(): MockDataService = MockDataService()
}
