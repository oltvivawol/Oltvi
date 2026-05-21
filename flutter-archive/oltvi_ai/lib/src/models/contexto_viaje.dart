import 'package:oltvi_core/oltvi_core.dart';

class ContextoViaje {
  final String? idViaje;
  final String? idUsuario;
  final String? idConductor;
  final PuntoGeo? origen;
  final PuntoGeo? destino;
  final EstadoServicio? estadoActual;
  final TipoServicio? tipoServicio;
  final double? precioEstimado;
  final List<PuntoGeo> historialPosiciones;
  final Map<String, dynamic> metadatos;

  const ContextoViaje({
    this.idViaje,
    this.idUsuario,
    this.idConductor,
    this.origen,
    this.destino,
    this.estadoActual,
    this.tipoServicio,
    this.precioEstimado,
    this.historialPosiciones = const [],
    this.metadatos = const {},
  });

  ContextoViaje copyWith({
    String? idViaje,
    String? idUsuario,
    String? idConductor,
    PuntoGeo? origen,
    PuntoGeo? destino,
    EstadoServicio? estadoActual,
    TipoServicio? tipoServicio,
    double? precioEstimado,
    List<PuntoGeo>? historialPosiciones,
    Map<String, dynamic>? metadatos,
  }) {
    return ContextoViaje(
      idViaje: idViaje ?? this.idViaje,
      idUsuario: idUsuario ?? this.idUsuario,
      idConductor: idConductor ?? this.idConductor,
      origen: origen ?? this.origen,
      destino: destino ?? this.destino,
      estadoActual: estadoActual ?? this.estadoActual,
      tipoServicio: tipoServicio ?? this.tipoServicio,
      precioEstimado: precioEstimado ?? this.precioEstimado,
      historialPosiciones: historialPosiciones ?? this.historialPosiciones,
      metadatos: metadatos ?? this.metadatos,
    );
  }

  Map<String, dynamic> toJson() => {
        'idViaje': idViaje,
        'idUsuario': idUsuario,
        'idConductor': idConductor,
        'origen': origen != null ? {'lat': origen!.latitud, 'lng': origen!.longitud, 'nombre': origen!.nombre} : null,
        'destino': destino != null ? {'lat': destino!.latitud, 'lng': destino!.longitud, 'nombre': destino!.nombre} : null,
        'estado': estadoActual?.name,
        'tipoServicio': tipoServicio?.name,
        'precioEstimado': precioEstimado,
      };
}
