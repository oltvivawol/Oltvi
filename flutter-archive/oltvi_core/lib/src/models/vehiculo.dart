enum EstadoVehiculo { operativo, mantenimiento, fueraDeServicio }

enum TipoVehiculo { auto, moto, utilitario, camion, camionFrio }

class Vehiculo {
  final String id;
  final String marca;
  final String modelo;
  final int anio;
  final String patente;
  final String color;
  final TipoVehiculo tipo;
  final EstadoVehiculo estado;
  final double capacidadKg;
  final int capacidadPasajeros;
  final String? idConductor;
  final DateTime vencimientoSeguro;
  final DateTime vencimientoVtv;
  final double consumoPromedioKmL;

  const Vehiculo({
    required this.id,
    required this.marca,
    required this.modelo,
    required this.anio,
    required this.patente,
    required this.color,
    required this.tipo,
    this.estado = EstadoVehiculo.operativo,
    this.capacidadKg = 0,
    this.capacidadPasajeros = 0,
    this.idConductor,
    required this.vencimientoSeguro,
    required this.vencimientoVtv,
    this.consumoPromedioKmL = 12.0,
  });

  String get tipoLabel {
    switch (tipo) {
      case TipoVehiculo.auto:
        return 'Auto';
      case TipoVehiculo.moto:
        return 'Moto';
      case TipoVehiculo.utilitario:
        return 'Utilitario';
      case TipoVehiculo.camion:
        return 'Camión';
      case TipoVehiculo.camionFrio:
        return 'Camión refrigerado';
    }
  }

  String get descripcionCorta => '$marca $modelo · $patente';
}
