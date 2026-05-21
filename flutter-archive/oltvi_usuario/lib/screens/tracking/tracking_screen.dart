import 'package:flutter/material.dart';
import 'package:latlong2/latlong.dart';
import 'package:oltvi_core/oltvi_core.dart';

/// Pantalla de seguimiento en vivo del servicio activo.
class TrackingScreen extends StatefulWidget {
  const TrackingScreen({super.key});

  @override
  State<TrackingScreen> createState() => _TrackingScreenState();
}

class _TrackingScreenState extends State<TrackingScreen> {
  Servicio? _servicioActivo;

  static const _vehiclePosition = LatLng(-34.6037, -58.3816);

  @override
  void initState() {
    super.initState();
    _cargarServicioActivo();
  }

  void _cargarServicioActivo() {
    final activos = MockDataService.instance.servicios.where((s) =>
        s.estado == EstadoServicio.enRuta ||
        s.estado == EstadoServicio.enCamino ||
        s.estado == EstadoServicio.asignado ||
        s.estado == EstadoServicio.llegando);
    if (activos.isNotEmpty) {
      setState(() => _servicioActivo = activos.first);
    } else {
      setState(() => _servicioActivo = null);
    }
  }

  void _cancelarServicio() {
    if (_servicioActivo == null) return;
    MockDataService.instance.actualizar(
      _servicioActivo!.copyWith(estado: EstadoServicio.cancelado),
    );
    setState(() => _servicioActivo = null);
  }

  @override
  Widget build(BuildContext context) {
    final s = _servicioActivo;
    if (s == null) return _emptyState();

    return Scaffold(
      body: Stack(
        children: [
          OltviMapView(
            initialCenter: s.origen.toLatLng(),
            initialZoom: 14,
            markers: [
              OltviMarkers.origin(s.origen),
              OltviMarkers.destination(s.destino),
              OltviMarkers.vehicle(_vehiclePosition, bearing: 0.78),
            ],
            route: [_vehiclePosition, s.origen.toLatLng()],
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: OltviGlassPanel(
                borderRadius: BorderRadius.circular(20),
                padding: const EdgeInsets.all(16),
                child: Row(
                  children: [
                    Container(
                      width: 48,
                      height: 48,
                      decoration: BoxDecoration(
                        gradient: OltviColors.accionGradient,
                        borderRadius: BorderRadius.circular(14),
                      ),
                      child: const Icon(Icons.directions_car_filled,
                          color: Colors.white, size: 26),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text('Tu conductor está en camino',
                              style: OltviTextStyles.subtitulo),
                          const SizedBox(height: 2),
                          Text(
                            'Llega en ~${s.tiempoEstimadoMin} min',
                            style: OltviTextStyles.cuerpo.copyWith(
                              color: OltviColors.textoSecundario,
                            ),
                          ),
                        ],
                      ),
                    ),
                    OltviStatusChip.forEstado(s.estado),
                  ],
                ),
              ),
            ),
          ),
          Align(
            alignment: Alignment.bottomCenter,
            child: SafeArea(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: OltviCard(
                  child: Column(
                    children: [
                      Row(
                        children: [
                          const CircleAvatar(
                            radius: 28,
                            backgroundColor: OltviColors.principal,
                            child: Icon(Icons.person, color: Colors.white),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  s.nombreConductor ?? 'Conductor asignado',
                                  style: OltviTextStyles.subtitulo,
                                ),
                                const SizedBox(height: 2),
                                Row(
                                  children: [
                                    const Icon(Icons.star_rounded,
                                        size: 16, color: OltviColors.accion),
                                    const SizedBox(width: 4),
                                    const Text('4.9',
                                        style: OltviTextStyles.cuerpo),
                                    const SizedBox(width: 8),
                                    Text(
                                      '• ${s.matriculaVehiculo ?? "—"}',
                                      style: OltviTextStyles.cuerpo.copyWith(
                                        color: OltviColors.textoSecundario,
                                      ),
                                    ),
                                  ],
                                ),
                              ],
                            ),
                          ),
                          _miniBtn(Icons.phone, () {}),
                          const SizedBox(width: 8),
                          _miniBtn(Icons.message, () {}),
                        ],
                      ),
                      const SizedBox(height: 14),
                      const Divider(height: 1),
                      const SizedBox(height: 14),
                      Row(
                        children: [
                          Expanded(
                            child: OltviSecondaryButton(
                              label: 'Compartir',
                              icon: Icons.share,
                              onPressed: () {},
                            ),
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: OltviSecondaryButton(
                              label: 'Cancelar',
                              icon: Icons.close,
                              color: OltviColors.error,
                              onPressed: _cancelarServicio,
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _miniBtn(IconData icon, VoidCallback onTap) {
    return Material(
      color: OltviColors.principal,
      shape: const CircleBorder(),
      child: InkWell(
        customBorder: const CircleBorder(),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Icon(icon, color: Colors.white, size: 20),
        ),
      ),
    );
  }

  Widget _emptyState() {
    return Scaffold(
      appBar: const OltviAppBar(title: 'Seguir'),
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 96,
                height: 96,
                decoration: BoxDecoration(
                  gradient: OltviColors.heroGradient,
                  borderRadius: BorderRadius.circular(28),
                ),
                child: const Icon(Icons.radar,
                    color: Colors.white, size: 48),
              ),
              const SizedBox(height: 24),
              const Text('Sin servicios activos',
                  style: OltviTextStyles.titulo),
              const SizedBox(height: 8),
              Text(
                'Cuando tengas un viaje, envío o reporte en curso, lo vas a ver acá en vivo.',
                style: OltviTextStyles.cuerpo.copyWith(
                  color: OltviColors.textoSecundario,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 24),
              OltviPrimaryButton(
                label: 'Buscar servicio activo',
                icon: Icons.refresh,
                expanded: false,
                onPressed: _cargarServicioActivo,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
