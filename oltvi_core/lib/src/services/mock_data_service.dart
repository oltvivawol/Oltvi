import 'dart:math';
import 'package:latlong2/latlong.dart';
import '../models/usuario.dart';
import '../models/servicio.dart';
import '../models/vehiculo.dart';
import '../models/evento_vial.dart';
import '../models/punto_geo.dart';

/// In-memory data layer used while the real backend is wired up.
/// Singleton, deterministic-ish, broadcasts changes through a stream so
/// the three apps behave like a single live system.
class MockDataService {
  MockDataService._internal() {
    _seed();
  }
  static final MockDataService instance = MockDataService._internal();

  final List<Usuario> _usuarios = [];
  final List<Servicio> _servicios = [];
  final List<Vehiculo> _vehiculos = [];
  final List<EventoVial> _eventos = [];

  List<Usuario> get usuarios => List.unmodifiable(_usuarios);
  List<Servicio> get servicios => List.unmodifiable(_servicios);
  List<Vehiculo> get vehiculos => List.unmodifiable(_vehiculos);
  List<EventoVial> get eventos => List.unmodifiable(_eventos);

  Usuario get clienteDemo => _usuarios.firstWhere((u) => u.perfil == TipoPerfil.cliente);
  Usuario get conductorDemo => _usuarios.firstWhere((u) => u.perfil == TipoPerfil.conductor);
  Usuario get adminDemo => _usuarios.firstWhere((u) => u.perfil == TipoPerfil.admin);

  List<Usuario> get conductores =>
      _usuarios.where((u) => u.perfil == TipoPerfil.conductor).toList();

  void _seed() {
    final now = DateTime.now();

    _usuarios.addAll([
      Usuario(
        id: 'u1',
        perfil: TipoPerfil.cliente,
        nombre: 'Mateo Rivas',
        correo: 'mateo@oltvi.app',
        telefono: '+54 9 11 5555 0001',
        nivel: NivelUsuario.experto,
        puntos: 1240,
        rating: 4.9,
        verificado: true,
        fechaRegistro: now.subtract(const Duration(days: 320)),
      ),
      Usuario(
        id: 'u2',
        perfil: TipoPerfil.conductor,
        nombre: 'Lucía Ferreyra',
        correo: 'lucia@oltvi.app',
        telefono: '+54 9 11 5555 0002',
        nivel: NivelUsuario.lider,
        puntos: 3870,
        rating: 4.95,
        verificado: true,
        fechaRegistro: now.subtract(const Duration(days: 480)),
      ),
      Usuario(
        id: 'u3',
        perfil: TipoPerfil.conductor,
        nombre: 'Diego Salinas',
        correo: 'diego@oltvi.app',
        telefono: '+54 9 11 5555 0003',
        nivel: NivelUsuario.experto,
        puntos: 1980,
        rating: 4.87,
        verificado: true,
        fechaRegistro: now.subtract(const Duration(days: 200)),
      ),
      Usuario(
        id: 'u4',
        perfil: TipoPerfil.conductor,
        nombre: 'Camila Ortega',
        correo: 'camila@oltvi.app',
        telefono: '+54 9 11 5555 0004',
        nivel: NivelUsuario.colaborador,
        puntos: 720,
        rating: 4.78,
        verificado: true,
        fechaRegistro: now.subtract(const Duration(days: 90)),
      ),
      Usuario(
        id: 'u5',
        perfil: TipoPerfil.admin,
        nombre: 'Centro de Mando OLTVI',
        correo: 'mando@oltvi.app',
        telefono: '+54 9 11 5555 0000',
        nivel: NivelUsuario.leyenda,
        puntos: 99999,
        rating: 5.0,
        verificado: true,
        fechaRegistro: now.subtract(const Duration(days: 720)),
      ),
    ]);

    _vehiculos.addAll([
      Vehiculo(
        id: 'v1',
        marca: 'Toyota',
        modelo: 'Corolla',
        anio: 2024,
        patente: 'AE 312 KP',
        color: 'Gris',
        tipo: TipoVehiculo.auto,
        capacidadPasajeros: 4,
        idConductor: 'u2',
        vencimientoSeguro: now.add(const Duration(days: 180)),
        vencimientoVtv: now.add(const Duration(days: 90)),
        consumoPromedioKmL: 14.5,
      ),
      Vehiculo(
        id: 'v2',
        marca: 'Renault',
        modelo: 'Kangoo',
        anio: 2023,
        patente: 'AC 887 LM',
        color: 'Blanco',
        tipo: TipoVehiculo.utilitario,
        capacidadKg: 650,
        idConductor: 'u3',
        vencimientoSeguro: now.add(const Duration(days: 45)),
        vencimientoVtv: now.add(const Duration(days: 10)),
        consumoPromedioKmL: 11.2,
      ),
      Vehiculo(
        id: 'v3',
        marca: 'Honda',
        modelo: 'Wave',
        anio: 2025,
        patente: 'A 1234 BC',
        color: 'Negro',
        tipo: TipoVehiculo.moto,
        capacidadKg: 15,
        idConductor: 'u4',
        vencimientoSeguro: now.add(const Duration(days: 300)),
        vencimientoVtv: now.add(const Duration(days: 200)),
        consumoPromedioKmL: 38.0,
      ),
    ]);

    // Seed servicios around Buenos Aires
    const baLat = -34.6037;
    const baLng = -58.3816;
    final rnd = Random(42);

    for (var i = 0; i < 8; i++) {
      final dx = (rnd.nextDouble() - 0.5) * 0.08;
      final dy = (rnd.nextDouble() - 0.5) * 0.08;
      final dx2 = (rnd.nextDouble() - 0.5) * 0.12;
      final dy2 = (rnd.nextDouble() - 0.5) * 0.12;
      final origen = PuntoGeo(
        lat: baLat + dx,
        lng: baLng + dy,
        nombre: _origenes[i % _origenes.length],
      );
      final destino = PuntoGeo(
        lat: baLat + dx2,
        lng: baLng + dy2,
        nombre: _destinos[i % _destinos.length],
      );
      final distKm = _distanceKm(origen.toLatLng(), destino.toLatLng());
      final tipo = TipoServicio.values[i % 3];
      final estado = i < 2
          ? EstadoServicio.enRuta
          : i < 4
              ? EstadoServicio.asignado
              : i < 6
                  ? EstadoServicio.entregado
                  : EstadoServicio.solicitado;
      _servicios.add(Servicio(
        id: 's${1000 + i}',
        tipo: tipo,
        nivel: NivelServicio.values[i % 4],
        origen: origen,
        destino: destino,
        idCliente: 'u1',
        idConductor: estado == EstadoServicio.solicitado ? null : 'u${2 + (i % 3)}',
        nombreConductor: estado == EstadoServicio.solicitado
            ? null
            : _usuarios[1 + (i % 3)].nombre,
        matriculaVehiculo: estado == EstadoServicio.solicitado
            ? null
            : _vehiculos[i % 3].patente,
        estado: estado,
        precio: 850 + distKm * 320 + rnd.nextInt(500),
        distanciaKm: distKm,
        tiempoEstimadoMin: (distKm * 3.5).round() + 4,
        fechaCreacion: now.subtract(Duration(minutes: 10 + i * 25)),
      ));
    }

    _eventos.addAll([
      EventoVial(
        id: 'e1',
        tipo: TipoEventoVial.bache,
        ubicacion: const PuntoGeo(lat: -34.6020, lng: -58.3795, nombre: 'Av. 9 de Julio'),
        descripcion: 'Bache profundo en carril central, esquivar derecha',
        idReportador: 'u3',
        fechaReporte: now.subtract(const Duration(hours: 2)),
        estado: EstadoEvento.confirmado,
        verificaciones: 8,
        rechazos: 1,
        iaScore: 0.94,
      ),
      EventoVial(
        id: 'e2',
        tipo: TipoEventoVial.obra,
        ubicacion: const PuntoGeo(lat: -34.6105, lng: -58.3920, nombre: 'San Telmo'),
        descripcion: 'Obra municipal, calle reducida a un carril',
        idReportador: 'u2',
        fechaReporte: now.subtract(const Duration(hours: 6)),
        estado: EstadoEvento.confirmado,
        verificaciones: 12,
        rechazos: 0,
        iaScore: 0.98,
      ),
      EventoVial(
        id: 'e3',
        tipo: TipoEventoVial.accidente,
        ubicacion: const PuntoGeo(lat: -34.5980, lng: -58.3700, nombre: 'Puerto Madero'),
        descripcion: 'Accidente leve, asistencia en camino',
        idReportador: 'u4',
        fechaReporte: now.subtract(const Duration(minutes: 18)),
        estado: EstadoEvento.verificando,
        verificaciones: 3,
        rechazos: 0,
        iaScore: 0.81,
      ),
      EventoVial(
        id: 'e4',
        tipo: TipoEventoVial.trafico,
        ubicacion: const PuntoGeo(lat: -34.5870, lng: -58.4000, nombre: 'Palermo'),
        descripcion: 'Congestión densa, evitar entre 18-20hs',
        idReportador: 'u3',
        fechaReporte: now.subtract(const Duration(minutes: 45)),
        estado: EstadoEvento.confirmado,
        verificaciones: 22,
        rechazos: 2,
        iaScore: 0.91,
      ),
    ]);
  }

  static const _origenes = [
    'Av. Corrientes 1500',
    'Plaza de Mayo',
    'Caballito',
    'Belgrano R',
    'Recoleta',
    'Palermo Soho',
    'Villa Crespo',
    'San Telmo',
  ];
  static const _destinos = [
    'Aeroparque',
    'Retiro',
    'Puerto Madero',
    'Núñez',
    'Almagro',
    'Microcentro',
    'Once',
    'Boedo',
  ];

  double _distanceKm(LatLng a, LatLng b) {
    const distance = Distance();
    return distance(a, b) / 1000.0;
  }

  Servicio crear(Servicio s) {
    _servicios.insert(0, s);
    return s;
  }

  void actualizar(Servicio s) {
    final i = _servicios.indexWhere((x) => x.id == s.id);
    if (i >= 0) _servicios[i] = s;
  }

  EventoVial crearEvento(EventoVial e) {
    _eventos.insert(0, e);
    return e;
  }

  void actualizarEvento(EventoVial e) {
    final i = _eventos.indexWhere((x) => x.id == e.id);
    if (i >= 0) _eventos[i] = e;
  }
}
