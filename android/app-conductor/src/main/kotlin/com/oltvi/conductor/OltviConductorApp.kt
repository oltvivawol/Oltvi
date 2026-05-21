package com.oltvi.conductor

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application entry-point for the OLTVI Trabajo (driver) app.
 *
 * Annotated with [HiltAndroidApp] to bootstrap the Dagger/Hilt graph for the
 * whole driver-side process. All dependency-injected components in this
 * module (ViewModels, services, etc.) resolve through here.
 */
@HiltAndroidApp
class OltviConductorApp : Application()
