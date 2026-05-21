import 'package:flutter/material.dart';
import 'package:flutter_map/flutter_map.dart';
import 'package:latlong2/latlong.dart';
import 'package:oltvi_core/oltvi_core.dart';

class FlotaScreen extends StatelessWidget {
  const FlotaScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final vehiculos = MockDataService.instance.vehiculos;
    final servicios = MockDataService.instance.servicios;

    // Generar posiciones simuladas para vehículos alrededor de Buenos Aires.
    final markers = <Marker>[];
    const baseLat = -34.6037;
    const baseLng = -58.3816;
    for (int i = 0; i < vehiculos.length; i++) {
      final offset = (i + 1) * 0.013;
      final pos = LatLng(
        baseLat + (i.isEven ? offset : -offset),
        baseLng + (i.isOdd ? offset : -offset * 0.7),
      );
      markers.add(OltviMarkers.vehicle(pos, bearing: i * 0.78));
    }

    // Marcadores de servicios activos (origen).
    for (final s in servicios.where(
        (s) => s.estado == EstadoServicio.enRuta)) {
      markers.add(OltviMarkers.origin(s.origen));
    }

    return Scaffold(
      backgroundColor: Colors.black,
      body: Stack(
        children: [
          OltviMapView(
            initialCenter: const LatLng(baseLat, baseLng),
            initialZoom: 12,
            initialStyle: OltviMapStyle.dark,
            markers: markers,
          ),
          SafeArea(
            child: Padding(
              padding: const EdgeInsets.all(12),
              child: OltviGlassPanel(
                dark: true,
                borderRadius: BorderRadius.circular(20),
                padding: const EdgeInsets.all(14),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        const Icon(Icons.map,
                            color: OltviColors.accion, size: 22),
                        const SizedBox(width: 8),
                        Text('Flota en vivo',
                            style: OltviTextStyles.titulo
                                .copyWith(color: Colors.white)),
                        const Spacer(),
                        _badge('${vehiculos.length} unidades',
                            OltviColors.accion),
                      ],
                    ),
                    const SizedBox(height: 10),
                    Row(
                      children: [
                        _mini(
                            'Operativos',
                            vehiculos
                                .where((v) =>
                                    v.estado == EstadoVehiculo.operativo)
                                .length
                                .toString(),
                            OltviColors.exito),
                        const SizedBox(width: 8),
                        _mini(
                            'Mantenim.',
                            vehiculos
                                .where((v) =>
                                    v.estado ==
                                    EstadoVehiculo.mantenimiento)
                                .length
                                .toString(),
                            OltviColors.alerta),
                        const SizedBox(width: 8),
                        _mini(
                            'F/S',
                            vehiculos
                                .where((v) =>
                                    v.estado ==
                                    EstadoVehiculo.fueraDeServicio)
                                .length
                                .toString(),
                            OltviColors.error),
                      ],
                    ),
                  ],
                ),
              ),
            ),
          ),
          DraggableScrollableSheet(
            initialChildSize: 0.22,
            minChildSize: 0.12,
            maxChildSize: 0.7,
            builder: (context, scrollController) {
              return Container(
                decoration: const BoxDecoration(
                  color: Color(0xFF0E1620),
                  borderRadius:
                      BorderRadius.vertical(top: Radius.circular(24)),
                ),
                child: ListView(
                  controller: scrollController,
                  padding: EdgeInsets.zero,
                  children: [
                    const Padding(
                      padding: EdgeInsets.only(top: 8),
                      child: OltviSheetHandle(),
                    ),
                    Padding(
                      padding: const EdgeInsets.fromLTRB(16, 8, 16, 12),
                      child: Row(
                        children: [
                          Text('Unidades',
                              style: OltviTextStyles.subtitulo
                                  .copyWith(color: Colors.white)),
                          const Spacer(),
                          Text('${vehiculos.length}',
                              style: OltviTextStyles.cuerpo.copyWith(
                                color: Colors.white.withOpacity(0.5),
                              )),
                        ],
                      ),
                    ),
                    ...vehiculos.map(_vehiculoFila),
                  ],
                ),
              );
            },
          ),
        ],
      ),
    );
  }

  Widget _vehiculoFila(Vehiculo v) {
    Color estadoColor;
    String estadoLabel;
    switch (v.estado) {
      case EstadoVehiculo.operativo:
        estadoColor = OltviColors.exito;
        estadoLabel = 'Operativo';
        break;
      case EstadoVehiculo.mantenimiento:
        estadoColor = OltviColors.alerta;
        estadoLabel = 'Mantenim.';
        break;
      case EstadoVehiculo.fueraDeServicio:
        estadoColor = OltviColors.error;
        estadoLabel = 'F/S';
        break;
    }

    // Buscar nombre del conductor asociado.
    String conductorNombre = '—';
    if (v.idConductor != null) {
      try {
        conductorNombre = MockDataService.instance.usuarios
            .firstWhere((u) => u.id == v.idConductor)
            .nombre;
      } catch (_) {}
    }

    return Container(
      margin: const EdgeInsets.fromLTRB(12, 0, 12, 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.04),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        children: [
          Container(
            width: 36,
            height: 36,
            decoration: BoxDecoration(
              color: estadoColor.withOpacity(0.15),
              borderRadius: BorderRadius.circular(10),
            ),
            child:
                Icon(Icons.directions_car, color: estadoColor, size: 20),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('${v.marca} ${v.modelo}',
                    style: OltviTextStyles.cuerpo
                        .copyWith(color: Colors.white)),
                Text('${v.patente} · $conductorNombre',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.5),
                      fontSize: 11,
                    )),
              ],
            ),
          ),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
            decoration: BoxDecoration(
              color: estadoColor.withOpacity(0.15),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Text(estadoLabel,
                style: OltviTextStyles.eyebrow.copyWith(color: estadoColor)),
          ),
        ],
      ),
    );
  }

  Widget _badge(String text, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: color),
      ),
      child:
          Text(text, style: OltviTextStyles.eyebrow.copyWith(color: color)),
    );
  }

  Widget _mini(String label, String valor, Color color) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(8),
        decoration: BoxDecoration(
          color: color.withOpacity(0.1),
          borderRadius: BorderRadius.circular(10),
          border: Border.all(color: color.withOpacity(0.3)),
        ),
        child: Column(
          children: [
            Text(valor,
                style: OltviTextStyles.titulo.copyWith(
                  color: color,
                  fontSize: 18,
                )),
            Text(label,
                style: OltviTextStyles.eyebrow.copyWith(
                  color: Colors.white.withOpacity(0.5),
                  fontSize: 9,
                )),
          ],
        ),
      ),
    );
  }
}
