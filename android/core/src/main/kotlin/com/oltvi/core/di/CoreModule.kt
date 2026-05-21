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

    // Flip to false when API keys are configured in local.properties
    private const val MOCK_MODE = true

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext ctx: Context
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(ctx)

    @Provides
    @Singleton
    @Named("geminiKey")
    fun provideGeminiKey(@ApplicationContext ctx: Context): String {
        return try {
            val props = java.util.Properties()
            ctx.assets.open("local.properties").use { props.load(it) }
            props.getProperty("GEMINI_API_KEY") ?: System.getenv("GEMINI_API_KEY") ?: "mock"
        } catch (e: Exception) {
            System.getenv("GEMINI_API_KEY") ?: "mock"
        }
    }

    @Provides @Singleton
    fun provideMatchmaker(@Named("geminiKey") key: String): AgenteMatchmaker =
        AgenteMatchmaker(key, MOCK_MODE)

    @Provides @Singleton
    fun providePrecio(@Named("geminiKey") key: String): AgentePrecio =
        AgentePrecio(key, MOCK_MODE)

    @Provides @Singleton
    fun provideSeguridad(@Named("geminiKey") key: String): AgenteSeguridad =
        AgenteSeguridad(key, MOCK_MODE)

    @Provides @Singleton
    fun provideRuta(@Named("geminiKey") key: String): AgenteRuta =
        AgenteRuta(key, MOCK_MODE)

    @Provides @Singleton
    fun provideSoporte(@Named("geminiKey") key: String): AgenteSoporte =
        AgenteSoporte(key, MOCK_MODE)

    @Provides @Singleton
    fun provideFraude(@Named("geminiKey") key: String): AgenteFraude =
        AgenteFraude(key, MOCK_MODE)

    @Provides @Singleton
    fun provideCopiloto(@Named("geminiKey") key: String): AgenteCopiloto =
        AgenteCopiloto(key, MOCK_MODE)

    @Provides @Singleton
    fun provideOperaciones(@Named("geminiKey") key: String): AgenteOperaciones =
        AgenteOperaciones(key, MOCK_MODE)

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
