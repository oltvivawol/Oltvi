import 'dart:math' as math;

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:latlong2/latlong.dart';
import 'package:oltvi_core/oltvi_core.dart';

import 'ride_request_sheet.dart';

/// The Ride - flagship immersive screen. The map is THE stage; everything
/// else floats on top of it. This is the cover of the OLTVI ecosystem.
class RideScreen extends StatefulWidget {
  const RideScreen({super.key});

  @override
  State<RideScreen> createState() => _RideScreenState();
}

class _RideScreenState extends State<RideScreen>
    with TickerProviderStateMixin {
  LatLng? _userPosition;
  LatLng _center = LocationService.fallback;
  PuntoGeo? _origen;
  PuntoGeo? _destino;
  List<LatLng> _route = const [];
  bool _loadingLocation = true;

  // Estimated metrics for the HUD (mock - would come from routing engine).
  double _etaMin = 0;
  double _distKm = 0;
  double _price = 0;

  late final AnimationController _heroCtrl;
  late final Animation<double> _heroAnim;

  @override
  void initState() {
    super.initState();
    _heroCtrl = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 900),
    );
    _heroAnim = CurvedAnimation(parent: _heroCtrl, curve: Curves.easeOutCubic);
    _heroCtrl.forward();
    _bootstrapLocation();
  }

  Future<void> _bootstrapLocation() async {
    final pos = await LocationService.currentOrFallback();
    if (!mounted) return;
    setState(() {
      _userPosition = pos;
      _center = pos;
      _origen = PuntoGeo.fromLatLng(pos, nombre: 'Mi ubicación');
      _loadingLocation = false;
    });
  }

  @override
  void dispose() {
    _heroCtrl.dispose();
    super.dispose();
  }

  void _setDestination(PuntoGeo destino) {
    final origen = _origen ??
        (_userPosition != null
            ? PuntoGeo.fromLatLng(_userPosition!, nombre: 'Mi ubicación')
            : PuntoGeo.fromLatLng(_center, nombre: 'Mi ubicación'));
    final route = _generateRoute(origen.toLatLng(), destino.toLatLng());
    const distance = Distance();
    final km = distance(origen.toLatLng(), destino.toLatLng()) / 1000.0;
    setState(() {
      _origen = origen;
      _destino = destino;
      _route = route;
      _distKm = km;
      _etaMin = km * 3.2 + 4;
      _price = 820 + km * 340;
    });
  }

  /// Synthesises a believable curved route between origin & destination so
  /// the line looks like a real path. Real impl would call a routing API.
  List<LatLng> _generateRoute(LatLng a, LatLng b) {
    final mid1 = LatLng(
      a.latitude + (b.latitude - a.latitude) * 0.35 + 0.001,
      a.longitude + (b.longitude - a.longitude) * 0.35 - 0.0012,
    );
    final mid2 = LatLng(
      a.latitude + (b.latitude - a.latitude) * 0.7 - 0.0008,
      a.longitude + (b.longitude - a.longitude) * 0.7 + 0.0015,
    );
    return _bezier([a, mid1, mid2, b], 40);
  }

  List<LatLng> _bezier(List<LatLng> ctrl, int samples) {
    final pts = <LatLng>[];
    for (var i = 0; i <= samples; i++) {
      final t = i / samples;
      pts.add(_deCasteljau(ctrl, t));
    }
    return pts;
  }

  LatLng _deCasteljau(List<LatLng> pts, double t) {
    var current = List<LatLng>.from(pts);
    while (current.length > 1) {
      final next = <LatLng>[];
      for (var i = 0; i < current.length - 1; i++) {
        final a = current[i];
        final b = current[i + 1];
        next.add(LatLng(
          a.latitude + (b.latitude - a.latitude) * t,
          a.longitude + (b.longitude - a.longitude) * t,
        ));
      }
      current = next;
    }
    return current.first;
  }

  Future<void> _openRequestSheet() async {
    HapticFeedback.lightImpact();
    final result = await showModalBottomSheet<PuntoGeo>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (_) => RideRequestSheet(origen: _origen ?? PuntoGeo.fromLatLng(_center)),
    );
    if (result != null) _setDestination(result);
  }

  void _clearRoute() {
    setState(() {
      _destino = null;
      _route = const [];
      _etaMin = 0;
      _distKm = 0;
      _price = 0;
    });
  }

  void _confirmRide() {
    HapticFeedback.mediumImpact();
    final s = Servicio(
      id: 's-${DateTime.now().millisecondsSinceEpoch}',
      tipo: TipoServicio.pasajero,
      nivel: NivelServicio.estandar,
      origen: _origen!,
      destino: _destino!,
      idCliente: MockDataService.instance.clienteDemo.id,
      precio: _price,
      distanciaKm: _distKm,
      tiempoEstimadoMin: _etaMin.round(),
      fechaCreacion: DateTime.now(),
      puntosRuta: _route.map((p) => PuntoGeo(lat: p.latitude, lng: p.longitude)).toList(),
      estado: EstadoServicio.solicitado,
    );
    MockDataService.instance.crear(s);
    showModalBottomSheet<void>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (_) => _RideConfirmationSheet(servicio: s),
    ).then((_) => _clearRoute());
  }

  @override
  Widget build(BuildContext context) {
    final markers = <Marker>[
      if (_origen != null) OltviMarkers.origin(_origen!),
      if (_destino != null) OltviMarkers.destination(_destino!),
      ...MockDataService.instance.eventos.map(
        (e) => OltviMarkers.incident(e.ubicacion, icon: _eventIcon(e.tipo)),
      ),
    ];

    return Scaffold(
      extendBodyBehindAppBar: true,
      body: Stack(
        children: [
          // === LAYER 1: The map. Always on. 24/7. ===
          Positioned.fill(
            child: OltviMapView(
              initialCenter: _center,
              initialZoom: OltviConstants.defaultMapZoom,
              userPosition: _userPosition,
              markers: markers,
              route: _route,
              controlsPadding: EdgeInsets.only(
                top: MediaQuery.of(context).padding.top + 96,
                right: 12,
              ),
            ),
          ),

          // === LAYER 2: Top floating glass header ===
          Positioned(
            top: MediaQuery.of(context).padding.top + 12,
            left: 16,
            right: 16,
            child: FadeTransition(
              opacity: _heroAnim,
              child: SlideTransition(
                position: Tween<Offset>(
                  begin: const Offset(0, -0.3),
                  end: Offset.zero,
                ).animate(_heroAnim),
                child: _TopHeader(loadingLocation: _loadingLocation),
              ),
            ),
          ),

          // === LAYER 3: Bottom panel - either CTA or ride summary ===
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: AnimatedSwitcher(
              duration: const Duration(milliseconds: 350),
              switchInCurve: Curves.easeOutCubic,
              switchOutCurve: Curves.easeInCubic,
              transitionBuilder: (child, anim) {
                return SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(0, 0.5),
                    end: Offset.zero,
                  ).animate(anim),
                  child: FadeTransition(opacity: anim, child: child),
                );
              },
              child: _destino == null
                  ? _IdleBottom(
                      key: const ValueKey('idle'),
                      onRequestPressed: _openRequestSheet,
                    )
                  : _RoutePreviewBottom(
                      key: const ValueKey('preview'),
                      origen: _origen!,
                      destino: _destino!,
                      etaMin: _etaMin.round(),
                      distKm: _distKm,
                      price: _price,
                      onCancel: _clearRoute,
                      onConfirm: _confirmRide,
                    ),
            ),
          ),
        ],
      ),
    );
  }

  IconData _eventIcon(TipoEventoVial t) {
    switch (t) {
      case TipoEventoVial.bache:
        return Icons.warning_amber_rounded;
      case TipoEventoVial.obra:
        return Icons.construction_rounded;
      case TipoEventoVial.corteTotal:
        return Icons.do_not_disturb_on_rounded;
      case TipoEventoVial.accidente:
        return Icons.car_crash_rounded;
      case TipoEventoVial.trafico:
        return Icons.traffic_rounded;
      case TipoEventoVial.semaforoFallando:
        return Icons.traffic_rounded;
      case TipoEventoVial.fugaAgua:
        return Icons.water_drop_rounded;
      case TipoEventoVial.fugaGas:
        return Icons.local_fire_department_rounded;
      case TipoEventoVial.cloacas:
        return Icons.plumbing_rounded;
      case TipoEventoVial.alumbrado:
        return Icons.lightbulb_rounded;
      case TipoEventoVial.senalizacion:
        return Icons.signpost_rounded;
      case TipoEventoVial.desvio:
        return Icons.alt_route_rounded;
      case TipoEventoVial.otro:
        return Icons.info_rounded;
    }
  }
}

// ===========================================================================
//  TOP HEADER
// ===========================================================================

class _TopHeader extends StatelessWidget {
  final bool loadingLocation;
  const _TopHeader({required this.loadingLocation});

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(20),
      child: BackdropGlass(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
          child: Row(
            children: [
              const OltviLogo(size: 38),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Text(
                          'OLTVI',
                          style: TextStyle(
                            fontWeight: FontWeight.w900,
                            fontSize: 17,
                            color: OltviColors.textoPrincipal,
                            letterSpacing: 2,
                          ),
                        ),
                        const SizedBox(width: 6),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 6,
                            vertical: 2,
                          ),
                          decoration: BoxDecoration(
                            color: OltviColors.accion,
                            borderRadius: BorderRadius.circular(4),
                          ),
                          child: const Text(
                            'VIAJES',
                            style: TextStyle(
                              fontWeight: FontWeight.w900,
                              fontSize: 9,
                              color: Colors.white,
                              letterSpacing: 1.5,
                            ),
                          ),
                        ),
                      ],
                    ),
                    Row(
                      children: [
                        Icon(
                          loadingLocation
                              ? Icons.gps_not_fixed_rounded
                              : Icons.gps_fixed_rounded,
                          size: 12,
                          color: loadingLocation
                              ? OltviColors.alerta
                              : OltviColors.exito,
                        ),
                        const SizedBox(width: 4),
                        Text(
                          loadingLocation
                              ? 'Localizando...'
                              : 'Tu ruta, nuestra inteligencia',
                          style: const TextStyle(
                            fontSize: 11,
                            color: OltviColors.textoSecundario,
                            letterSpacing: 0.2,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              _RoundIconButton(
                icon: Icons.notifications_rounded,
                badge: 3,
                onTap: () {},
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class BackdropGlass extends StatelessWidget {
  final Widget child;
  const BackdropGlass({super.key, required this.child});

  @override
  Widget build(BuildContext context) {
    return OltviGlassPanel(
      blur: 16,
      tint: Colors.white.withOpacity(0.78),
      borderRadius: BorderRadius.circular(20),
      padding: EdgeInsets.zero,
      child: child,
    );
  }
}

class _RoundIconButton extends StatelessWidget {
  final IconData icon;
  final int? badge;
  final VoidCallback onTap;

  const _RoundIconButton({
    required this.icon,
    required this.onTap,
    this.badge,
  });

  @override
  Widget build(BuildContext context) {
    return Stack(
      clipBehavior: Clip.none,
      children: [
        Material(
          color: OltviColors.principal,
          shape: const CircleBorder(),
          child: InkWell(
            customBorder: const CircleBorder(),
            onTap: onTap,
            child: SizedBox(
              width: 42,
              height: 42,
              child: Icon(icon, color: Colors.white, size: 20),
            ),
          ),
        ),
        if (badge != null && badge! > 0)
          Positioned(
            top: -2,
            right: -2,
            child: Container(
              padding: const EdgeInsets.all(4),
              constraints: const BoxConstraints(minWidth: 18, minHeight: 18),
              decoration: BoxDecoration(
                color: OltviColors.accion,
                shape: BoxShape.circle,
                border: Border.all(color: Colors.white, width: 2),
              ),
              child: Text(
                '$badge',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 10,
                  fontWeight: FontWeight.w800,
                ),
                textAlign: TextAlign.center,
              ),
            ),
          ),
      ],
    );
  }
}

// ===========================================================================
//  IDLE BOTTOM (no destination yet)
// ===========================================================================

class _IdleBottom extends StatelessWidget {
  final VoidCallback onRequestPressed;
  const _IdleBottom({super.key, required this.onRequestPressed});

  @override
  Widget build(BuildContext context) {
    final activos = MockDataService.instance.servicios
        .where((s) =>
            s.estado != EstadoServicio.entregado &&
            s.estado != EstadoServicio.cancelado)
        .take(2)
        .toList();

    return OltviGlassPanel(
      tint: Colors.white.withOpacity(0.95),
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const OltviSheetHandle(),
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: OltviColors.accion.withOpacity(0.12),
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: const Icon(
                    Icons.auto_awesome_rounded,
                    color: OltviColors.accion,
                    size: 18,
                  ),
                ),
                const SizedBox(width: 10),
                const Expanded(
                  child: Text(
                    '¿A dónde vamos?',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.w800,
                      color: OltviColors.textoPrincipal,
                      letterSpacing: -0.3,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            const Text(
              'Predicción inteligente activa · Rutas optimizadas',
              style: TextStyle(
                fontSize: 12,
                color: OltviColors.textoSecundario,
              ),
            ),
            const SizedBox(height: 16),
            OltviPrimaryButton(
              label: 'Pedir un viaje',
              icon: Icons.search_rounded,
              onPressed: onRequestPressed,
            ),
            const SizedBox(height: 14),
            Row(
              children: [
                Expanded(
                  child: _QuickAction(
                    icon: Icons.bookmark_rounded,
                    label: 'Guardados',
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _QuickAction(
                    icon: Icons.share_location_rounded,
                    label: 'Compartir',
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: _QuickAction(
                    icon: Icons.shield_rounded,
                    label: 'SOS',
                    color: OltviColors.error,
                  ),
                ),
              ],
            ),
            if (activos.isNotEmpty) ...[
              const SizedBox(height: 18),
              Padding(
                padding: const EdgeInsets.symmetric(horizontal: 2),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: const [
                    Text(
                      'ACTIVOS AHORA',
                      style: OltviTextStyles.eyebrow,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 8),
              ...activos.map((s) => Padding(
                    padding: const EdgeInsets.only(bottom: 8),
                    child: _ActiveServiceTile(servicio: s),
                  )),
            ],
          ],
        ),
      ),
    );
  }
}

class _QuickAction extends StatelessWidget {
  final IconData icon;
  final String label;
  final Color? color;
  const _QuickAction({required this.icon, required this.label, this.color});

  @override
  Widget build(BuildContext context) {
    final c = color ?? OltviColors.principal;
    return InkWell(
      borderRadius: BorderRadius.circular(14),
      onTap: () {},
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
        decoration: BoxDecoration(
          color: OltviColors.fondoGeneral,
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: c.withOpacity(0.18)),
        ),
        child: Column(
          children: [
            Icon(icon, color: c, size: 22),
            const SizedBox(height: 6),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: c,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _ActiveServiceTile extends StatelessWidget {
  final Servicio servicio;
  const _ActiveServiceTile({required this.servicio});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        children: [
          Container(
            width: 38,
            height: 38,
            decoration: BoxDecoration(
              color: OltviColors.accion.withOpacity(0.12),
              borderRadius: BorderRadius.circular(10),
            ),
            child: const Icon(
              Icons.directions_car_rounded,
              color: OltviColors.accion,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${servicio.origen.nombre} → ${servicio.destino.nombre}',
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: const TextStyle(
                    fontWeight: FontWeight.w700,
                    fontSize: 13,
                    color: OltviColors.textoPrincipal,
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  '${OltviFormat.distance(servicio.distanciaKm)} · ${OltviFormat.duration(servicio.tiempoEstimadoMin)}',
                  style: const TextStyle(
                    fontSize: 11,
                    color: OltviColors.textoSecundario,
                  ),
                ),
              ],
            ),
          ),
          OltviStatusChip.forEstado(servicio.estado, dense: true),
        ],
      ),
    );
  }
}

// ===========================================================================
//  ROUTE PREVIEW BOTTOM (with destination)
// ===========================================================================

class _RoutePreviewBottom extends StatelessWidget {
  final PuntoGeo origen;
  final PuntoGeo destino;
  final int etaMin;
  final double distKm;
  final double price;
  final VoidCallback onCancel;
  final VoidCallback onConfirm;

  const _RoutePreviewBottom({
    super.key,
    required this.origen,
    required this.destino,
    required this.etaMin,
    required this.distKm,
    required this.price,
    required this.onCancel,
    required this.onConfirm,
  });

  @override
  Widget build(BuildContext context) {
    return OltviGlassPanel(
      tint: Colors.white.withOpacity(0.96),
      padding: const EdgeInsets.fromLTRB(20, 12, 20, 24),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const OltviSheetHandle(),
            // Top HUD strip: ETA, distance, price
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: OltviColors.heroGradient,
                borderRadius: BorderRadius.circular(18),
                boxShadow: [
                  BoxShadow(
                    color: OltviColors.principal.withOpacity(0.3),
                    blurRadius: 24,
                    offset: const Offset(0, 12),
                    spreadRadius: -8,
                  ),
                ],
              ),
              child: Row(
                children: [
                  _HudMetric(
                    label: 'LLEGADA',
                    value: OltviFormat.duration(etaMin),
                    icon: Icons.schedule_rounded,
                  ),
                  Container(
                    width: 1,
                    height: 40,
                    color: Colors.white.withOpacity(0.15),
                  ),
                  _HudMetric(
                    label: 'DISTANCIA',
                    value: OltviFormat.distance(distKm),
                    icon: Icons.route_rounded,
                  ),
                  Container(
                    width: 1,
                    height: 40,
                    color: Colors.white.withOpacity(0.15),
                  ),
                  _HudMetric(
                    label: 'PRECIO',
                    value: OltviFormat.money(price),
                    icon: Icons.payments_rounded,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 16),
            _RouteRow(
              dotColor: OltviColors.principal,
              label: origen.nombre ?? 'Origen',
              sub: 'Punto de partida',
            ),
            Padding(
              padding: const EdgeInsets.only(left: 6),
              child: SizedBox(
                height: 14,
                child: CustomPaint(
                  size: const Size(2, 14),
                  painter: _DashedLinePainter(),
                ),
              ),
            ),
            _RouteRow(
              dotColor: OltviColors.exito,
              label: destino.nombre ?? 'Destino',
              sub: 'Te llevamos acá',
            ),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: onCancel,
                    style: OutlinedButton.styleFrom(
                      minimumSize: const Size(0, 56),
                      side: const BorderSide(color: Color(0xFFE2E8F0)),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(14),
                      ),
                    ),
                    child: const Text(
                      'Cancelar',
                      style: TextStyle(
                        fontWeight: FontWeight.w700,
                        color: OltviColors.textoPrincipal,
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  flex: 2,
                  child: OltviPrimaryButton(
                    label: 'Confirmar viaje',
                    icon: Icons.bolt_rounded,
                    height: 56,
                    onPressed: onConfirm,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }
}

class _HudMetric extends StatelessWidget {
  final String label;
  final String value;
  final IconData icon;

  const _HudMetric({
    required this.label,
    required this.value,
    required this.icon,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 4),
        child: Column(
          children: [
            Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(icon, color: OltviColors.accion, size: 14),
                const SizedBox(width: 4),
                Text(
                  label,
                  style: TextStyle(
                    fontSize: 9,
                    fontWeight: FontWeight.w800,
                    letterSpacing: 1.2,
                    color: Colors.white.withOpacity(0.6),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 4),
            Text(
              value,
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.w800,
                color: Colors.white,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ],
        ),
      ),
    );
  }
}

class _RouteRow extends StatelessWidget {
  final Color dotColor;
  final String label;
  final String sub;
  const _RouteRow({
    required this.dotColor,
    required this.label,
    required this.sub,
  });

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 14,
          height: 14,
          margin: const EdgeInsets.symmetric(horizontal: 0),
          decoration: BoxDecoration(
            color: dotColor,
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 3),
            boxShadow: [
              BoxShadow(
                color: dotColor.withOpacity(0.4),
                blurRadius: 8,
                spreadRadius: -1,
              ),
            ],
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                label,
                style: const TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w700,
                  color: OltviColors.textoPrincipal,
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
              Text(
                sub,
                style: const TextStyle(
                  fontSize: 11,
                  color: OltviColors.textoSecundario,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }
}

class _DashedLinePainter extends CustomPainter {
  @override
  void paint(Canvas canvas, Size size) {
    final paint = Paint()
      ..color = const Color(0xFFCBD5E1)
      ..strokeWidth = 2;
    const dash = 3.0;
    const gap = 3.0;
    var y = 0.0;
    while (y < size.height) {
      canvas.drawLine(
        Offset(size.width / 2, y),
        Offset(size.width / 2, math.min(y + dash, size.height)),
        paint,
      );
      y += dash + gap;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

// ===========================================================================
//  CONFIRMATION SHEET
// ===========================================================================

class _RideConfirmationSheet extends StatelessWidget {
  final Servicio servicio;
  const _RideConfirmationSheet({required this.servicio});

  @override
  Widget build(BuildContext context) {
    return OltviGlassPanel(
      tint: Colors.white,
      padding: const EdgeInsets.fromLTRB(24, 14, 24, 28),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const OltviSheetHandle(),
            Container(
              width: 84,
              height: 84,
              decoration: BoxDecoration(
                gradient: OltviColors.accionGradient,
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: OltviColors.accion.withOpacity(0.4),
                    blurRadius: 32,
                    spreadRadius: -4,
                  ),
                ],
              ),
              child: const Icon(
                Icons.check_rounded,
                color: Colors.white,
                size: 48,
              ),
            ),
            const SizedBox(height: 16),
            const Text(
              'Buscando conductor',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w800,
                color: OltviColors.textoPrincipal,
                letterSpacing: -0.3,
              ),
            ),
            const SizedBox(height: 6),
            Text(
              'Servicio #${servicio.id.substring(servicio.id.length - 6)}',
              style: const TextStyle(
                fontSize: 13,
                color: OltviColors.textoSecundario,
              ),
            ),
            const SizedBox(height: 20),
            Container(
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: OltviColors.fondoGeneral,
                borderRadius: BorderRadius.circular(14),
              ),
              child: Row(
                children: [
                  _MiniMetric(
                    label: 'Distancia',
                    value: OltviFormat.distance(servicio.distanciaKm),
                  ),
                  _Divider(),
                  _MiniMetric(
                    label: 'Llegada',
                    value: OltviFormat.duration(servicio.tiempoEstimadoMin),
                  ),
                  _Divider(),
                  _MiniMetric(
                    label: 'Total',
                    value: OltviFormat.money(servicio.precio),
                    highlight: true,
                  ),
                ],
              ),
            ),
            const SizedBox(height: 18),
            OltviPrimaryButton(
              label: 'Ver seguimiento',
              icon: Icons.gps_fixed_rounded,
              onPressed: () => Navigator.of(context).pop(),
            ),
          ],
        ),
      ),
    );
  }
}

class _MiniMetric extends StatelessWidget {
  final String label;
  final String value;
  final bool highlight;
  const _MiniMetric({
    required this.label,
    required this.value,
    this.highlight = false,
  });

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        children: [
          Text(
            label.toUpperCase(),
            style: TextStyle(
              fontSize: 10,
              fontWeight: FontWeight.w800,
              color: highlight
                  ? OltviColors.accion
                  : OltviColors.textoSecundario,
              letterSpacing: 1,
            ),
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: TextStyle(
              fontSize: 15,
              fontWeight: FontWeight.w800,
              color: highlight
                  ? OltviColors.accion
                  : OltviColors.textoPrincipal,
            ),
          ),
        ],
      ),
    );
  }
}

class _Divider extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Container(width: 1, height: 28, color: const Color(0xFFE2E8F0));
  }
}
