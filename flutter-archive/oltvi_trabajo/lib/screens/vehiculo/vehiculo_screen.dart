import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class VehiculoScreen extends StatelessWidget {
  const VehiculoScreen({super.key});

  @override
  Widget build(BuildContext context) {
    // Tomamos un vehículo del mock (el primero del conductor demo).
    final conductorId = MockDataService.instance.conductorDemo.id;
    final vehiculo = MockDataService.instance.vehiculos.firstWhere(
      (v) => v.idConductor == conductorId,
      orElse: () => MockDataService.instance.vehiculos.first,
    );

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Text('Mi vehículo',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _vehiculoCard(vehiculo),
          const SizedBox(height: 20),
          Text('Documentación',
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          const SizedBox(height: 10),
          _docItem('Licencia de conducir', 'Vigente hasta 03/2028', true),
          _docItem('Cédula verde', 'Vigente', true),
          _docItem('Seguro',
              'Renueva en ${_diasHasta(vehiculo.vencimientoSeguro)} días',
              _diasHasta(vehiculo.vencimientoSeguro) > 15),
          _docItem('VTV',
              'Vence en ${_diasHasta(vehiculo.vencimientoVtv)} días',
              _diasHasta(vehiculo.vencimientoVtv) > 15),
          const SizedBox(height: 20),
          Text('Mantenimiento',
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          const SizedBox(height: 10),
          _mantItem('Próximo service', 'En 2.300 km', Icons.build),
          _mantItem('Cambio de aceite', 'OK', Icons.opacity),
          _mantItem('Frenos', 'OK', Icons.disc_full),
        ],
      ),
    );
  }

  int _diasHasta(DateTime d) => d.difference(DateTime.now()).inDays;

  Widget _vehiculoCard(Vehiculo v) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF1A2733), Color(0xFF0E1620)],
        ),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: Colors.white.withOpacity(0.08)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 64,
                height: 64,
                decoration: BoxDecoration(
                  gradient: OltviColors.accionGradient,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(_iconoTipo(v.tipo),
                    color: Colors.white, size: 32),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text('${v.marca} ${v.modelo} ${v.anio}',
                        style: OltviTextStyles.titulo
                            .copyWith(color: Colors.white)),
                    const SizedBox(height: 4),
                    Text('${v.patente} · ${v.color}',
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.6),
                        )),
                  ],
                ),
              ),
              _estadoChip(v.estado),
            ],
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.all(12),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.04),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                _kpi('Tipo', v.tipoLabel),
                _divider(),
                _kpi('Consumo',
                    '${v.consumoPromedioKmL.toStringAsFixed(1)} km/L'),
                _divider(),
                _kpi('Capacidad',
                    v.capacidadPasajeros > 0
                        ? '${v.capacidadPasajeros} pas.'
                        : '${v.capacidadKg.toStringAsFixed(0)} kg'),
              ],
            ),
          ),
        ],
      ),
    );
  }

  IconData _iconoTipo(TipoVehiculo t) {
    switch (t) {
      case TipoVehiculo.auto:
        return Icons.directions_car;
      case TipoVehiculo.moto:
        return Icons.motorcycle;
      case TipoVehiculo.utilitario:
        return Icons.local_shipping;
      case TipoVehiculo.camion:
        return Icons.fire_truck;
      case TipoVehiculo.camionFrio:
        return Icons.ac_unit;
    }
  }

  Widget _estadoChip(EstadoVehiculo estado) {
    Color color;
    String label;
    switch (estado) {
      case EstadoVehiculo.operativo:
        color = OltviColors.exito;
        label = 'Operativo';
        break;
      case EstadoVehiculo.mantenimiento:
        color = OltviColors.alerta;
        label = 'Mantenim.';
        break;
      case EstadoVehiculo.fueraDeServicio:
        color = OltviColors.error;
        label = 'F/S';
        break;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 5),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Container(
            width: 8,
            height: 8,
            decoration: BoxDecoration(color: color, shape: BoxShape.circle),
          ),
          const SizedBox(width: 6),
          Text(label,
              style: OltviTextStyles.eyebrow.copyWith(color: color)),
        ],
      ),
    );
  }

  Widget _kpi(String label, String valor) {
    return Expanded(
      child: Column(
        children: [
          Text(valor,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          Text(label,
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.5),
                fontSize: 11,
              )),
        ],
      ),
    );
  }

  Widget _divider() {
    return Container(
      width: 1,
      height: 30,
      color: Colors.white.withOpacity(0.08),
    );
  }

  Widget _docItem(String titulo, String estado, bool ok) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.white.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            Icon(
              ok ? Icons.check_circle : Icons.warning_rounded,
              color: ok ? OltviColors.exito : OltviColors.alerta,
              size: 24,
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(titulo,
                      style: OltviTextStyles.cuerpo
                          .copyWith(color: Colors.white)),
                  Text(estado,
                      style: OltviTextStyles.cuerpo.copyWith(
                        color: ok
                            ? Colors.white.withOpacity(0.6)
                            : OltviColors.alerta,
                        fontSize: 12,
                      )),
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: Colors.white.withOpacity(0.3)),
          ],
        ),
      ),
    );
  }

  Widget _mantItem(String titulo, String estado, IconData icon) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.white.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            Icon(icon, color: OltviColors.accion, size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Text(titulo,
                  style: OltviTextStyles.cuerpo
                      .copyWith(color: Colors.white)),
            ),
            Text(estado,
                style: OltviTextStyles.cuerpo.copyWith(
                  color: Colors.white.withOpacity(0.6),
                )),
          ],
        ),
      ),
    );
  }
}
