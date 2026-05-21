import 'punto_geo.dart';

enum TipoServicio {
  pasajero, // The Ride
  mensajeria,
  carga,
  vial,
}

enum NivelServicio { estandar, prioritario, expres, especial }

enum EstadoServicio {
  solicitado,
  asignado,
  enCamino, // hacia el origen
  llegando,
  enRuta, // con pasajero/carga
  entregado,
  cancelado,
}

class Servicio {
  final String id;
  final TipoServicio tipo;
  final NivelServicio nivel;
  final PuntoGeo origen;
  final PuntoGeo destino;
  final String idCliente;
  final String? idConductor;
  final String? nombreConductor;
  final String? matriculaVehiculo;
  final EstadoServicio estado;
  final double precio;
  final double distanciaKm;
  final int tiempoEstimadoMin;
  final DateTime fechaCreacion;
  final DateTime? fechaProgramada;
  final String? detalles;
  final List<PuntoGeo> puntosRuta;

  const Servicio({
    required this.id,
    required this.tipo,
    this.nivel = NivelServicio.estandar,
    required this.origen,
    required this.destino,
    required this.idCliente,
    this.idConductor,
    this.nombreConductor,
    this.matriculaVehiculo,
    this.estado = EstadoServicio.solicitado,
    required this.precio,
    required this.distanciaKm,
    required this.tiempoEstimadoMin,
    required this.fechaCreacion,
    this.fechaProgramada,
    this.detalles,
    this.puntosRuta = const [],
  });

  Servicio copyWith({
    EstadoServicio? estado,
    String? idConductor,
    String? nombreConductor,
    String? matriculaVehiculo,
    List<PuntoGeo>? puntosRuta,
  }) {
    return Servicio(
      id: id,
      tipo: tipo,
      nivel: nivel,
      origen: origen,
      destino: destino,
      idCliente: idCliente,
      idConductor: idConductor ?? this.idConductor,
      nombreConductor: nombreConductor ?? this.nombreConductor,
      matriculaVehiculo: matriculaVehiculo ?? this.matriculaVehiculo,
      estado: estado ?? this.estado,
      precio: precio,
      distanciaKm: distanciaKm,
      tiempoEstimadoMin: tiempoEstimadoMin,
      fechaCreacion: fechaCreacion,
      fechaProgramada: fechaProgramada,
      detalles: detalles,
      puntosRuta: puntosRuta ?? this.puntosRuta,
    );
  }

  String get tipoLabel {
    switch (tipo) {
      case TipoServicio.pasajero:
        return 'Viaje';
      case TipoServicio.mensajeria:
        return 'Mensajería';
      case TipoServicio.carga:
        return 'Carga';
      case TipoServicio.vial:
        return 'Reporte Vial';
    }
  }

  String get nivelLabel {
    switch (nivel) {
      case NivelServicio.estandar:
        return 'Estándar';
      case NivelServicio.prioritario:
        return 'Prioritario';
      case NivelServicio.expres:
        return 'Exprés';
      case NivelServicio.especial:
        return 'Especial';
    }
  }

  String get estadoLabel {
    switch (estado) {
      case EstadoServicio.solicitado:
        return 'Buscando';
      case EstadoServicio.asignado:
        return 'Asignado';
      case EstadoServicio.enCamino:
        return 'En camino';
      case EstadoServicio.llegando:
        return 'Llegando';
      case EstadoServicio.enRuta:
        return 'En ruta';
      case EstadoServicio.entregado:
        return 'Entregado';
      case EstadoServicio.cancelado:
        return 'Cancelado';
    }
  }
}
