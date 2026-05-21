import 'dart:async';
import 'package:geolocator/geolocator.dart';
import 'package:latlong2/latlong.dart';

/// Wraps geolocator with safe defaults and a graceful fallback so the map
/// never blocks the UI even if permission is denied or GPS is off.
class LocationService {
  LocationService._();

  /// Default fallback when location is unavailable (Buenos Aires, AR).
  /// Chosen because the project is being developed in Argentina.
  static const LatLng fallback = LatLng(-34.6037, -58.3816);

  static Future<bool> hasPermission() async {
    final p = await Geolocator.checkPermission();
    return p == LocationPermission.always || p == LocationPermission.whileInUse;
  }

  static Future<bool> requestPermission() async {
    var p = await Geolocator.checkPermission();
    if (p == LocationPermission.denied) {
      p = await Geolocator.requestPermission();
    }
    return p == LocationPermission.always || p == LocationPermission.whileInUse;
  }

  /// Gets the current location, falling back to [fallback] if unavailable.
  /// Never throws. Returns a [LatLng].
  static Future<LatLng> currentOrFallback({
    LocationAccuracy accuracy = LocationAccuracy.high,
    Duration timeout = const Duration(seconds: 8),
  }) async {
    try {
      final serviceOk = await Geolocator.isLocationServiceEnabled();
      if (!serviceOk) return fallback;
      final ok = await requestPermission();
      if (!ok) return fallback;
      final pos = await Geolocator.getCurrentPosition(
        desiredAccuracy: accuracy,
        timeLimit: timeout,
      );
      return LatLng(pos.latitude, pos.longitude);
    } catch (_) {
      return fallback;
    }
  }

  /// Live position stream. Emits [fallback] first so the UI can render
  /// immediately even before a real GPS fix arrives.
  static Stream<LatLng> positionStream({
    LocationAccuracy accuracy = LocationAccuracy.high,
    int distanceFilterMeters = 5,
  }) async* {
    yield await currentOrFallback(accuracy: accuracy);
    try {
      yield* Geolocator.getPositionStream(
        locationSettings: LocationSettings(
          accuracy: accuracy,
          distanceFilter: distanceFilterMeters,
        ),
      ).map((p) => LatLng(p.latitude, p.longitude));
    } catch (_) {
      // Stream errors swallowed; UI keeps the last known fix.
    }
  }
}
