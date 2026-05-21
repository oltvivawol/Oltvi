package com.oltvi.jefe.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for the OLTVI Mando (jefe) application.
 *
 * All singleton bindings required by this app — [com.oltvi.core.data.services.MockDataService],
 * [com.oltvi.core.ai.orchestrator.OltviOrchestrator], and every AI agent — are provided by
 * `CoreModule` in the `:core` module. Because both modules are installed in
 * [SingletonComponent], Hilt merges them automatically and no re-declaration is needed here.
 *
 * Add app-jefe-specific bindings (e.g. a Mando-only analytics repository) to this object
 * when the need arises.
 */
@Module
@InstallIn(SingletonComponent::class)
object JefeModule
