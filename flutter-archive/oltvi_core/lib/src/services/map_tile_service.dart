import 'package:flutter/foundation.dart';

/// Map tile providers available to OLTVI.
///
/// Default is OpenStreetMap which works 24/7 with no API key.
/// This guarantees the map ALWAYS works as the user requested.
enum OltviMapStyle {
  street, // OSM standard
  dark, // CartoDB dark matter
  light, // CartoDB positron
  satellite, // Esri world imagery
}

class MapTileService {
  MapTileService._();

  static const String userAgent = 'com.oltvi.app';

  /// Returns the tile URL template for the given style.
  static String urlTemplate(OltviMapStyle style) {
    switch (style) {
      case OltviMapStyle.street:
        return 'https://tile.openstreetmap.org/{z}/{x}/{y}.png';
      case OltviMapStyle.dark:
        return 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/dark_all/{z}/{x}/{y}.png';
      case OltviMapStyle.light:
        return 'https://cartodb-basemaps-{s}.global.ssl.fastly.net/light_all/{z}/{x}/{y}.png';
      case OltviMapStyle.satellite:
        return 'https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/{z}/{y}/{x}';
    }
  }

  static List<String> subdomains(OltviMapStyle style) {
    switch (style) {
      case OltviMapStyle.street:
      case OltviMapStyle.satellite:
        return const [];
      case OltviMapStyle.dark:
      case OltviMapStyle.light:
        return const ['a', 'b', 'c', 'd'];
    }
  }

  static String attribution(OltviMapStyle style) {
    switch (style) {
      case OltviMapStyle.street:
        return '© OpenStreetMap';
      case OltviMapStyle.dark:
      case OltviMapStyle.light:
        return '© OpenStreetMap, © CARTO';
      case OltviMapStyle.satellite:
        return '© Esri';
    }
  }

  static String label(OltviMapStyle style) {
    switch (style) {
      case OltviMapStyle.street:
        return 'Calles';
      case OltviMapStyle.dark:
        return 'Noche';
      case OltviMapStyle.light:
        return 'Claro';
      case OltviMapStyle.satellite:
        return 'Satélite';
    }
  }

  static OltviMapStyle defaultStyle({Brightness? platformBrightness}) {
    if (platformBrightness == Brightness.dark) return OltviMapStyle.dark;
    return OltviMapStyle.street;
  }
}
