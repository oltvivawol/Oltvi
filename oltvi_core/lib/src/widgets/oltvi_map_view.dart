import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';

import '../theme/oltvi_colors.dart';
import '../services/map_tile_service.dart';
import '../models/punto_geo.dart';

/// The OLTVI map view. Built on flutter_map + OpenStreetMap tiles which work
/// 24/7 without an API key, so the map ALWAYS works - that is the
/// non-negotiable requirement for this product.
///
/// Features:
///  - Multiple tile styles (street, dark, light, satellite) hot-switchable.
///  - Optional route polyline, origin/destination markers.
///  - Optional custom markers (e.g. vial events).
///  - "Recenter" + "Style" floating action buttons.
///  - Smooth animated camera, sane defaults, plays well in any screen.
class OltviMapView extends StatefulWidget {
  final LatLng initialCenter;
  final double initialZoom;
  final OltviMapStyle initialStyle;
  final List<LatLng> route;
  final List<Marker> markers;
  final LatLng? userPosition;
  final bool showStyleSwitcher;
  final bool showRecenter;
  final bool showAttribution;
  final bool interactive;
  final EdgeInsets controlsPadding;
  final void Function(LatLng tap)? onTap;
  final void Function(MapController controller)? onMapReady;

  const OltviMapView({
    super.key,
    required this.initialCenter,
    this.initialZoom = 14,
    this.initialStyle = OltviMapStyle.street,
    this.route = const [],
    this.markers = const [],
    this.userPosition,
    this.showStyleSwitcher = true,
    this.showRecenter = true,
    this.showAttribution = true,
    this.interactive = true,
    this.controlsPadding = const EdgeInsets.all(12),
    this.onTap,
    this.onMapReady,
  });

  @override
  State<OltviMapView> createState() => _OltviMapViewState();
}

class _OltviMapViewState extends State<OltviMapView>
    with TickerProviderStateMixin {
  late final MapController _controller;
  late OltviMapStyle _style;

  @override
  void initState() {
    super.initState();
    _controller = MapController();
    _style = widget.initialStyle;
  }

  void _recenter() {
    final target = widget.userPosition ?? widget.initialCenter;
    _animatedMove(target, 16);
  }

  void _animatedMove(LatLng dest, double zoom) {
    final latTween = Tween<double>(
      begin: _controller.camera.center.latitude,
      end: dest.latitude,
    );
    final lngTween = Tween<double>(
      begin: _controller.camera.center.longitude,
      end: dest.longitude,
    );
    final zoomTween =
        Tween<double>(begin: _controller.camera.zoom, end: zoom);
    final ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 600),
    );
    final anim = CurvedAnimation(parent: ctrl, curve: Curves.easeInOutCubic);
    ctrl.addListener(() {
      _controller.move(
        LatLng(latTween.evaluate(anim), lngTween.evaluate(anim)),
        zoomTween.evaluate(anim),
      );
    });
    ctrl.addStatusListener((s) {
      if (s == AnimationStatus.completed || s == AnimationStatus.dismissed) {
        ctrl.dispose();
      }
    });
    ctrl.forward();
  }

  @override
  Widget build(BuildContext context) {
    final allMarkers = <Marker>[];
    if (widget.userPosition != null) {
      allMarkers.add(_userMarker(widget.userPosition!));
    }
    allMarkers.addAll(widget.markers);

    return Stack(
      children: [
        FlutterMap(
          mapController: _controller,
          options: MapOptions(
            initialCenter: widget.initialCenter,
            initialZoom: widget.initialZoom,
            minZoom: 3,
            maxZoom: 19,
            interactionOptions: InteractionOptions(
              flags: widget.interactive
                  ? InteractiveFlag.all & ~InteractiveFlag.rotate
                  : InteractiveFlag.none,
            ),
            onTap: widget.onTap == null
                ? null
                : (_, p) => widget.onTap!(p),
            onMapReady: () => widget.onMapReady?.call(_controller),
            backgroundColor: OltviColors.fondoGeneral,
          ),
          children: [
            TileLayer(
              urlTemplate: MapTileService.urlTemplate(_style),
              subdomains: MapTileService.subdomains(_style),
              userAgentPackageName: MapTileService.userAgent,
              maxZoom: 19,
              tileProvider: NetworkTileProvider(),
              errorTileCallback: (_, __, ___) {},
            ),
            if (widget.route.length >= 2)
              PolylineLayer(
                polylines: [
                  // Outer glow
                  Polyline(
                    points: widget.route,
                    strokeWidth: 10,
                    color: OltviColors.accion.withOpacity(0.20),
                    strokeCap: StrokeCap.round,
                    strokeJoin: StrokeJoin.round,
                  ),
                  // Core line
                  Polyline(
                    points: widget.route,
                    strokeWidth: 6,
                    color: OltviColors.accion,
                    strokeCap: StrokeCap.round,
                    strokeJoin: StrokeJoin.round,
                  ),
                ],
              ),
            MarkerLayer(markers: allMarkers),
          ],
        ),

        if (widget.showAttribution)
          Positioned(
            left: 8,
            bottom: 8,
            child: _AttributionPill(
              text: MapTileService.attribution(_style),
            ),
          ),

        Positioned(
          right: widget.controlsPadding.right,
          top: widget.controlsPadding.top,
          child: Column(
            children: [
              if (widget.showStyleSwitcher) ...[
                _MapControlButton(
                  icon: Icons.layers_rounded,
                  onTap: _showStylePicker,
                ),
                const SizedBox(height: 8),
              ],
              if (widget.showRecenter)
                _MapControlButton(
                  icon: Icons.my_location_rounded,
                  onTap: _recenter,
                  highlight: true,
                ),
            ],
          ),
        ),
      ],
    );
  }

  void _showStylePicker() {
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      builder: (ctx) {
        return Container(
          padding: const EdgeInsets.all(20),
          decoration: const BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
          ),
          child: SafeArea(
            top: false,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 40,
                  height: 4,
                  margin: const EdgeInsets.only(bottom: 16),
                  decoration: BoxDecoration(
                    color: const Color(0xFFCBD5E1),
                    borderRadius: BorderRadius.circular(4),
                  ),
                ),
                const Text(
                  'Estilo del mapa',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.w700,
                    color: OltviColors.textoPrincipal,
                  ),
                ),
                const SizedBox(height: 16),
                Wrap(
                  spacing: 10,
                  runSpacing: 10,
                  children: OltviMapStyle.values.map((s) {
                    final sel = s == _style;
                    return InkWell(
                      borderRadius: BorderRadius.circular(12),
                      onTap: () {
                        setState(() => _style = s);
                        Navigator.of(ctx).pop();
                      },
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 16,
                          vertical: 12,
                        ),
                        decoration: BoxDecoration(
                          color: sel
                              ? OltviColors.accion
                              : OltviColors.fondoGeneral,
                          borderRadius: BorderRadius.circular(12),
                          border: Border.all(
                            color: sel
                                ? OltviColors.accion
                                : const Color(0xFFE2E8F0),
                          ),
                        ),
                        child: Text(
                          MapTileService.label(s),
                          style: TextStyle(
                            color: sel
                                ? Colors.white
                                : OltviColors.textoPrincipal,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    );
                  }).toList(),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Marker _userMarker(LatLng p) {
    return Marker(
      point: p,
      width: 44,
      height: 44,
      child: const _PulseDot(),
    );
  }
}

/// Reusable origin/destination marker factories.
class OltviMarkers {
  OltviMarkers._();

  static Marker origin(PuntoGeo p) => Marker(
        point: p.toLatLng(),
        width: 44,
        height: 44,
        alignment: Alignment.topCenter,
        child: const _PinMarker(
          color: OltviColors.principal,
          icon: Icons.trip_origin_rounded,
        ),
      );

  static Marker destination(PuntoGeo p) => Marker(
        point: p.toLatLng(),
        width: 44,
        height: 44,
        alignment: Alignment.topCenter,
        child: const _PinMarker(
          color: OltviColors.exito,
          icon: Icons.flag_rounded,
        ),
      );

  static Marker vehicle(LatLng p, {String? label, double bearing = 0}) => Marker(
        point: p,
        width: 48,
        height: 48,
        child: _VehicleMarker(label: label, bearing: bearing),
      );

  static Marker incident(PuntoGeo p, {required IconData icon, Color? color}) =>
      Marker(
        point: p.toLatLng(),
        width: 40,
        height: 40,
        child: _IncidentMarker(
          icon: icon,
          color: color ?? OltviColors.error,
        ),
      );
}

class _PinMarker extends StatelessWidget {
  final Color color;
  final IconData icon;
  const _PinMarker({required this.color, required this.icon});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 36,
          height: 36,
          decoration: BoxDecoration(
            color: color,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 3),
            boxShadow: [
              BoxShadow(
                color: color.withOpacity(0.45),
                blurRadius: 12,
                spreadRadius: -2,
              ),
            ],
          ),
          child: Icon(icon, color: Colors.white, size: 18),
        ),
      ],
    );
  }
}

class _VehicleMarker extends StatelessWidget {
  final String? label;
  final double bearing;
  const _VehicleMarker({this.label, this.bearing = 0});

  @override
  Widget build(BuildContext context) {
    return Transform.rotate(
      angle: bearing,
      child: Container(
        decoration: BoxDecoration(
          color: OltviColors.accion,
          shape: BoxShape.circle,
          border: Border.all(color: Colors.white, width: 3),
          boxShadow: [
            BoxShadow(
              color: OltviColors.accion.withOpacity(0.55),
              blurRadius: 16,
              spreadRadius: -2,
            ),
          ],
        ),
        child: const Icon(
          Icons.navigation_rounded,
          color: Colors.white,
          size: 22,
        ),
      ),
    );
  }
}

class _IncidentMarker extends StatelessWidget {
  final IconData icon;
  final Color color;
  const _IncidentMarker({required this.icon, required this.color});

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        shape: BoxShape.circle,
        border: Border.all(color: color, width: 2.5),
        boxShadow: [
          BoxShadow(
            color: color.withOpacity(0.35),
            blurRadius: 10,
            spreadRadius: -2,
          ),
        ],
      ),
      padding: const EdgeInsets.all(6),
      child: Icon(icon, color: color, size: 18),
    );
  }
}

class _PulseDot extends StatefulWidget {
  const _PulseDot();

  @override
  State<_PulseDot> createState() => _PulseDotState();
}

class _PulseDotState extends State<_PulseDot>
    with SingleTickerProviderStateMixin {
  late final AnimationController _ctrl;
  late final Animation<double> _anim;

  @override
  void initState() {
    super.initState();
    _ctrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1600),
    )..repeat();
    _anim = CurvedAnimation(parent: _ctrl, curve: Curves.easeOut);
  }

  @override
  void dispose() {
    _ctrl.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return AnimatedBuilder(
      animation: _anim,
      builder: (_, __) {
        final t = _anim.value;
        return Stack(
          alignment: Alignment.center,
          children: [
            Opacity(
              opacity: 1 - t,
              child: Container(
                width: 40 * t + 16,
                height: 40 * t + 16,
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  color: OltviColors.accion.withOpacity(0.35),
                ),
              ),
            ),
            Container(
              width: 18,
              height: 18,
              decoration: BoxDecoration(
                shape: BoxShape.circle,
                color: OltviColors.accion,
                border: Border.all(color: Colors.white, width: 3),
                boxShadow: [
                  BoxShadow(
                    color: OltviColors.accion.withOpacity(0.6),
                    blurRadius: 12,
                    spreadRadius: -2,
                  ),
                ],
              ),
            ),
          ],
        );
      },
    );
  }
}

class _MapControlButton extends StatelessWidget {
  final IconData icon;
  final VoidCallback onTap;
  final bool highlight;
  const _MapControlButton({
    required this.icon,
    required this.onTap,
    this.highlight = false,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: highlight ? OltviColors.accion : Colors.white,
      elevation: 6,
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onTap,
        child: SizedBox(
          width: 44,
          height: 44,
          child: Icon(
            icon,
            color: highlight ? Colors.white : OltviColors.principal,
            size: 22,
          ),
        ),
      ),
    );
  }
}

class _AttributionPill extends StatelessWidget {
  final String text;
  const _AttributionPill({required this.text});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.88),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        text,
        style: const TextStyle(
          fontSize: 10,
          color: OltviColors.textoSecundario,
          fontWeight: FontWeight.w500,
        ),
      ),
    );
  }
}
