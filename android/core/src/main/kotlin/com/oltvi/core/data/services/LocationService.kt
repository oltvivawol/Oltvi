package com.oltvi.core.data.services

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {
    companion object {
        val fallback = LatLng(-34.6037, -58.3816)
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): LatLng? {
        return try {
            val location = fusedLocationClient.lastLocation.await()
            location?.let { LatLng(it.latitude, it.longitude) }
        } catch (e: Exception) { null }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): Flow<LatLng> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    trySend(LatLng(it.latitude, it.longitude))
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(request, callback, context.mainLooper)
        awaitClose { fusedLocationClient.removeLocationUpdates(callback) }
    }
}
