import 'package:flutter/material.dart';
import '../theme/oltvi_colors.dart';
import '../models/servicio.dart';

class OltviStatusChip extends StatelessWidget {
  final String label;
  final Color color;
  final IconData? icon;
  final bool dense;

  const OltviStatusChip({
    super.key,
    required this.label,
    required this.color,
    this.icon,
    this.dense = false,
  });

  factory OltviStatusChip.forEstado(EstadoServicio estado, {bool dense = false}) {
    Color c;
    IconData ic;
    switch (estado) {
      case EstadoServicio.solicitado:
        c = OltviColors.alerta;
        ic = Icons.hourglass_top_rounded;
        break;
      case EstadoServicio.asignado:
        c = OltviColors.accionClaro;
        ic = Icons.assignment_turned_in_rounded;
        break;
      case EstadoServicio.enCamino:
      case EstadoServicio.llegando:
        c = OltviColors.accion;
        ic = Icons.directions_rounded;
        break;
      case EstadoServicio.enRuta:
        c = OltviColors.accion;
        ic = Icons.local_taxi_rounded;
        break;
      case EstadoServicio.entregado:
        c = OltviColors.exito;
        ic = Icons.check_circle_rounded;
        break;
      case EstadoServicio.cancelado:
        c = OltviColors.error;
        ic = Icons.cancel_rounded;
        break;
    }
    return OltviStatusChip(
      label: _estadoLabel(estado),
      color: c,
      icon: ic,
      dense: dense,
    );
  }

  static String _estadoLabel(EstadoServicio e) {
    switch (e) {
      case EstadoServicio.solicitado:
        return 'BUSCANDO';
      case EstadoServicio.asignado:
        return 'ASIGNADO';
      case EstadoServicio.enCamino:
        return 'EN CAMINO';
      case EstadoServicio.llegando:
        return 'LLEGANDO';
      case EstadoServicio.enRuta:
        return 'EN RUTA';
      case EstadoServicio.entregado:
        return 'ENTREGADO';
      case EstadoServicio.cancelado:
        return 'CANCELADO';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: EdgeInsets.symmetric(
        horizontal: dense ? 8 : 10,
        vertical: dense ? 4 : 6,
      ),
      decoration: BoxDecoration(
        color: color.withOpacity(0.13),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: color.withOpacity(0.45), width: 1),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (icon != null) ...[
            Icon(icon, size: dense ? 12 : 14, color: color),
            SizedBox(width: dense ? 4 : 6),
          ],
          Text(
            label,
            style: TextStyle(
              fontSize: dense ? 10 : 11,
              fontWeight: FontWeight.w800,
              color: color,
              letterSpacing: 0.5,
            ),
          ),
        ],
      ),
    );
  }
}
