import 'package:latlong2/latlong.dart';

/// Geographic point with optional metadata.
class PuntoGeo {
  final double lat;
  final double lng;
  final String? nombre;
  final String? direccion;

  const PuntoGeo({
    required this.lat,
    required this.lng,
    this.nombre,
    this.direccion,
  });

  LatLng toLatLng() => LatLng(lat, lng);

  factory PuntoGeo.fromLatLng(LatLng p, {String? nombre, String? direccion}) =>
      PuntoGeo(
        lat: p.latitude,
        lng: p.longitude,
        nombre: nombre,
        direccion: direccion,
      );

  Map<String, dynamic> toJson() => {
        'lat': lat,
        'lng': lng,
        'nombre': nombre,
        'direccion': direccion,
      };

  factory PuntoGeo.fromJson(Map<String, dynamic> j) => PuntoGeo(
        lat: (j['lat'] as num).toDouble(),
        lng: (j['lng'] as num).toDouble(),
        nombre: j['nombre'] as String?,
        direccion: j['direccion'] as String?,
      );

  @override
  String toString() => nombre ?? '${lat.toStringAsFixed(5)}, ${lng.toStringAsFixed(5)}';
}
