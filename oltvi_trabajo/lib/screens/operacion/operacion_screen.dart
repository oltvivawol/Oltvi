import 'dart:async';
import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:oltvi_core/oltvi_core.dart';

/// Pantalla central del conductor.
/// Mapa de fondo + toggle de disponibilidad + lista de solicitudes entrantes.
class OperacionScreen extends StatefulWidget {
  const OperacionScreen({super.key});

  @override
  State<OperacionScreen> createState() => _OperacionScreenState();
}

class _OperacionScreenState extends State<OperacionScreen>
    with TickerProviderStateMixin {
  bool _online = false;
  Servicio? _servicioActivo;
  Servicio? _solicitudEntrante;
  Timer? _simulador;
  late final AnimationController _pulse;

  LatLng _ubicacion = LocationService.fallback;

  @override
  void initState() {
    super.initState();
    _pulse = AnimationController(
      vsync: this,
      duration: const Duration(milliseconds: 1500),
    )..repeat();
    _cargarUbicacion();
  }

  Future<void> _cargarUbicacion() async {
    final pos = await LocationService.currentOrFallback();
    if (!mounted) return;
    setState(() => _ubicacion = pos);
  }

  @override
  void dispose() {
    _simulador?.cancel();
    _pulse.dispose();
    super.dispose();
  }

  void _toggleOnline() {
    setState(() => _online = !_online);
    if (_online) {
      // Simular llegada de una solicitud después de 3 segundos.
      _simulador = Timer(const Duration(seconds: 3), () {
        if (!mounted || !_online || _servicioActivo != null) return;
        final pendientes = MockDataService.instance.servicios
            .where((s) => s.estado == EstadoServicio.solicitado)
            .toList();
        if (pendientes.isNotEmpty) {
          setState(() => _solicitudEntrante = pendientes.first);
        }
      });
    } else {
      _simulador?.cancel();
      setState(() => _solicitudEntrante = null);
    }
  }

  void _aceptarSolicitud() {
    if (_solicitudEntrante == null) return;
    final aceptado = _solicitudEntrante!.copyWith(
      estado: EstadoServicio.asignado,
      idConductor: MockDataService.instance.conductorDemo.id,
      nombreConductor: MockDataService.instance.conductorDemo.nombre,
    );
    MockDataService.instance.actualizar(aceptado);
    setState(() {
      _servicioActivo = aceptado;
      _solicitudEntrante = null;
    });
  }

  void _rechazarSolicitud() {
    setState(() => _solicitudEntrante = null);
  }

  void _completarServicio() {
    if (_servicioActivo == null) return;
    MockDataService.instance.actualizar(
      _servicioActivo!.copyWith(estado: EstadoServicio.entregado),
    );
    setState(() => _servicioActivo = null);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          // MAPA DE FONDO 24/7
          OltviMapView(
            initialCenter: _ubicacion,
            initialZoom: 15,
            initialStyle: OltviMapStyle.dark,
            markers: [
              if (_servicioActivo != null)
                OltviMarkers.origin(_servicioActivo!.origen),
              if (_servicioActivo != null)
                OltviMarkers.destination(_servicioActivo!.destino),
            ],
          ),

          // Header flotante
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: _header(),
            ),
          ),

          // Panel inferior según estado
          Align(
            alignment: Alignment.bottomCenter,
            child: SafeArea(
              child: AnimatedSwitcher(
                duration: const Duration(milliseconds: 350),
                transitionBuilder: (child, anim) => SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(0, 0.3),
                    end: Offset.zero,
                  ).animate(CurvedAnimation(
                      parent: anim, curve: Curves.easeOutCubic)),
                  child: FadeTransition(opacity: anim, child: child),
                ),
                child: _buildBottomPanel(),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _header() {
    return OltviGlassPanel(
      dark: true,
      borderRadius: BorderRadius.circular(20),
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      child: Row(
        children: [
          const OltviLogo(size: 36),
          const SizedBox(width: 10),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('TRABAJO',
                  style: OltviTextStyles.eyebrow.copyWith(
                    color: OltviColors.accion,
                  )),
              Text(_online ? 'En línea' : 'Fuera de línea',
                  style: OltviTextStyles.subtitulo.copyWith(
                    color: Colors.white,
                  )),
            ],
          ),
          const Spacer(),
          _hudMini('Hoy', OltviFormat.money(4250)),
          const SizedBox(width: 8),
          _hudMini('Viajes', '7'),
        ],
      ),
    );
  }

  Widget _hudMini(String label, String valor) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.08),
        borderRadius: BorderRadius.circular(10),
        border: Border.all(color: Colors.white.withOpacity(0.1)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(label,
              style: OltviTextStyles.eyebrow.copyWith(
                color: Colors.white.withOpacity(0.5),
                fontSize: 9,
              )),
          Text(valor,
              style: OltviTextStyles.subtitulo.copyWith(
                color: Colors.white,
                fontSize: 13,
              )),
        ],
      ),
    );
  }

  Widget _buildBottomPanel() {
    if (_solicitudEntrante != null) {
      return _panelSolicitud(key: const ValueKey('solicitud'));
    }
    if (_servicioActivo != null) {
      return _panelServicioActivo(key: const ValueKey('activo'));
    }
    return _panelToggle(key: const ValueKey('toggle'));
  }

  Widget _panelToggle({Key? key}) {
    return Padding(
      key: key,
      padding: const EdgeInsets.all(16),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: Colors.white.withOpacity(0.08)),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withOpacity(0.6),
              blurRadius: 30,
              offset: const Offset(0, -4),
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const OltviSheetHandle(),
            const SizedBox(height: 6),
            Text(
              _online
                  ? 'Esperando solicitudes...'
                  : '¿Listo para trabajar?',
              style: OltviTextStyles.titulo.copyWith(color: Colors.white),
            ),
            const SizedBox(height: 6),
            Text(
              _online
                  ? 'Te avisamos cuando llegue un viaje cerca tuyo'
                  : 'Tocá el botón para empezar a recibir viajes',
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.6),
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 22),
            GestureDetector(
              onTap: _toggleOnline,
              child: AnimatedBuilder(
                animation: _pulse,
                builder: (context, _) {
                  return SizedBox(
                    width: 180,
                    height: 180,
                    child: Stack(
                      alignment: Alignment.center,
                      children: [
                        if (_online)
                          ...List.generate(3, (i) {
                            final v = (_pulse.value + i * 0.33) % 1.0;
                            return Container(
                              width: 100 + v * 80,
                              height: 100 + v * 80,
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border: Border.all(
                                  color: OltviColors.accion
                                      .withOpacity((1 - v) * 0.5),
                                  width: 2,
                                ),
                              ),
                            );
                          }),
                        Container(
                          width: 120,
                          height: 120,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            gradient: _online
                                ? OltviColors.accionGradient
                                : LinearGradient(
                                    colors: [
                                      Colors.white.withOpacity(0.1),
                                      Colors.white.withOpacity(0.05),
                                    ],
                                  ),
                            boxShadow: _online
                                ? [
                                    BoxShadow(
                                      color: OltviColors.accion
                                          .withOpacity(0.6),
                                      blurRadius: 30,
                                      spreadRadius: 4,
                                    ),
                                  ]
                                : null,
                            border: Border.all(
                              color: _online
                                  ? Colors.white.withOpacity(0.3)
                                  : Colors.white.withOpacity(0.15),
                              width: 2,
                            ),
                          ),
                          child: Icon(
                            _online ? Icons.power_settings_new : Icons.power,
                            color: Colors.white,
                            size: 48,
                          ),
                        ),
                      ],
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 18),
            Text(
              _online ? 'TOCÁ PARA DESCONECTAR' : 'TOCÁ PARA CONECTAR',
              style: OltviTextStyles.eyebrow.copyWith(
                color: _online
                    ? OltviColors.accion
                    : Colors.white.withOpacity(0.6),
                letterSpacing: 2,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _panelSolicitud({Key? key}) {
    final s = _solicitudEntrante!;
    return Padding(
      key: key,
      padding: const EdgeInsets.all(16),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          gradient: const LinearGradient(
            begin: Alignment.topLeft,
            end: Alignment.bottomRight,
            colors: [Color(0xFF1A2733), Color(0xFF0E1620)],
          ),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: OltviColors.accion, width: 2),
          boxShadow: [
            BoxShadow(
              color: OltviColors.accion.withOpacity(0.4),
              blurRadius: 30,
              spreadRadius: 2,
            ),
          ],
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: OltviColors.accion,
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Text('NUEVO ${s.tipoLabel.toUpperCase()}',
                      style: OltviTextStyles.eyebrow.copyWith(
                        color: Colors.white,
                      )),
                ),
                const Spacer(),
                Text(OltviFormat.money(s.precio),
                    style: OltviTextStyles.titulo.copyWith(
                      color: OltviColors.accion,
                    )),
              ],
            ),
            const SizedBox(height: 14),
            _routeRow(Icons.radio_button_checked, OltviColors.accion,
                s.origen.nombre, 'Punto de recogida'),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.only(left: 11),
              child: Container(
                width: 2,
                height: 16,
                color: Colors.white.withOpacity(0.2),
              ),
            ),
            const SizedBox(height: 8),
            _routeRow(Icons.place, Colors.white, s.destino.nombre,
                'Destino del viaje'),
            const SizedBox(height: 14),
            Row(
              children: [
                _miniInfo(Icons.timer_outlined,
                    OltviFormat.duration(s.tiempoEstimadoMin)),
                const SizedBox(width: 14),
                _miniInfo(Icons.straighten, OltviFormat.distance(s.distanciaKm)),
              ],
            ),
            const SizedBox(height: 18),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: _rechazarSolicitud,
                    style: OutlinedButton.styleFrom(
                      foregroundColor: Colors.white,
                      side: BorderSide(
                          color: Colors.white.withOpacity(0.3)),
                      padding: const EdgeInsets.symmetric(vertical: 16),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                    ),
                    child: const Text('Rechazar'),
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  flex: 2,
                  child: OltviPrimaryButton(
                    label: 'Aceptar',
                    icon: Icons.check_circle,
                    onPressed: _aceptarSolicitud,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _panelServicioActivo({Key? key}) {
    final s = _servicioActivo!;
    return Padding(
      key: key,
      padding: const EdgeInsets.all(16),
      child: Container(
        padding: const EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: Colors.white.withOpacity(0.08)),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                OltviStatusChip.forEstado(s.estado),
                const Spacer(),
                Text(OltviFormat.money(s.precio),
                    style: OltviTextStyles.titulo.copyWith(
                      color: Colors.white,
                    )),
              ],
            ),
            const SizedBox(height: 14),
            _routeRow(Icons.place, OltviColors.accion, 'Destino',
                s.destino.nombre),
            const SizedBox(height: 16),
            Row(
              children: [
                Expanded(
                  child: OltviSecondaryButton(
                    label: 'Navegar',
                    icon: Icons.navigation,
                    color: OltviColors.accion,
                    onPressed: () {},
                  ),
                ),
                const SizedBox(width: 10),
                Expanded(
                  child: OltviPrimaryButton(
                    label: 'Completar',
                    icon: Icons.check,
                    onPressed: _completarServicio,
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _routeRow(IconData icon, Color color, String titulo, String sub) {
    return Row(
      children: [
        Icon(icon, color: color, size: 22),
        const SizedBox(width: 10),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(titulo,
                  style: OltviTextStyles.subtitulo.copyWith(
                    color: Colors.white,
                  )),
              Text(sub,
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                  style: OltviTextStyles.cuerpo.copyWith(
                    color: Colors.white.withOpacity(0.6),
                    fontSize: 12,
                  )),
            ],
          ),
        ),
      ],
    );
  }

  Widget _miniInfo(IconData icon, String text) {
    return Row(
      children: [
        Icon(icon, color: Colors.white.withOpacity(0.6), size: 16),
        const SizedBox(width: 4),
        Text(text,
            style: OltviTextStyles.cuerpo.copyWith(
              color: Colors.white.withOpacity(0.7),
              fontSize: 13,
            )),
      ],
    );
  }
}
