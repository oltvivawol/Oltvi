import 'punto_geo.dart';

enum TipoEventoVial {
  bache,
  obra,
  corteTotal,
  desvio,
  semaforoFallando,
  fugaAgua,
  fugaGas,
  cloacas,
  alumbrado,
  senalizacion,
  accidente,
  trafico,
  otro,
}

enum EstadoEvento { reportado, verificando, confirmado, resuelto, descartado }

class EventoVial {
  final String id;
  final TipoEventoVial tipo;
  final PuntoGeo ubicacion;
  final String descripcion;
  final String idReportador;
  final DateTime fechaReporte;
  final DateTime? fechaResuelto;
  final EstadoEvento estado;
  final int verificaciones; // people who confirmed
  final int rechazos;
  final double iaScore; // 0..1 confidence

  const EventoVial({
    required this.id,
    required this.tipo,
    required this.ubicacion,
    required this.descripcion,
    required this.idReportador,
    required this.fechaReporte,
    this.fechaResuelto,
    this.estado = EstadoEvento.reportado,
    this.verificaciones = 0,
    this.rechazos = 0,
    this.iaScore = 0.0,
  });

  EventoVial copyWith({
    TipoEventoVial? tipo,
    PuntoGeo? ubicacion,
    String? descripcion,
    DateTime? fechaResuelto,
    EstadoEvento? estado,
    int? verificaciones,
    int? rechazos,
    double? iaScore,
  }) {
    return EventoVial(
      id: id,
      tipo: tipo ?? this.tipo,
      ubicacion: ubicacion ?? this.ubicacion,
      descripcion: descripcion ?? this.descripcion,
      idReportador: idReportador,
      fechaReporte: fechaReporte,
      fechaResuelto: fechaResuelto ?? this.fechaResuelto,
      estado: estado ?? this.estado,
      verificaciones: verificaciones ?? this.verificaciones,
      rechazos: rechazos ?? this.rechazos,
      iaScore: iaScore ?? this.iaScore,
    );
  }

  String get tipoLabel {
    switch (tipo) {
      case TipoEventoVial.bache:
        return 'Bache';
      case TipoEventoVial.obra:
        return 'Obra';
      case TipoEventoVial.corteTotal:
        return 'Corte total';
      case TipoEventoVial.desvio:
        return 'Desvío';
      case TipoEventoVial.semaforoFallando:
        return 'Semáforo';
      case TipoEventoVial.fugaAgua:
        return 'Fuga de agua';
      case TipoEventoVial.fugaGas:
        return 'Fuga de gas';
      case TipoEventoVial.cloacas:
        return 'Cloacas';
      case TipoEventoVial.alumbrado:
        return 'Alumbrado';
      case TipoEventoVial.senalizacion:
        return 'Señalización';
      case TipoEventoVial.accidente:
        return 'Accidente';
      case TipoEventoVial.trafico:
        return 'Tráfico';
      case TipoEventoVial.otro:
        return 'Otro';
    }
  }
}
