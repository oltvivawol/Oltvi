package com.oltvi.usuario.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object UsuarioModule
// All dependencies (LocationService, MockDataService, OltviOrchestrator) are provided
// via @Singleton @Inject constructor in the :core module. No extra bindings needed here.
