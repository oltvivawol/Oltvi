enum TipoPerfil { cliente, conductor, admin }

enum NivelUsuario { iniciado, colaborador, experto, lider, leyenda }

class Usuario {
  final String id;
  final TipoPerfil perfil;
  final String nombre;
  final String correo;
  final String telefono;
  final String? documento;
  final String? fotoUrl;
  final NivelUsuario nivel;
  final int puntos;
  final double rating;
  final bool verificado;
  final DateTime fechaRegistro;

  const Usuario({
    required this.id,
    required this.perfil,
    required this.nombre,
    required this.correo,
    required this.telefono,
    this.documento,
    this.fotoUrl,
    this.nivel = NivelUsuario.iniciado,
    this.puntos = 0,
    this.rating = 5.0,
    this.verificado = false,
    required this.fechaRegistro,
  });

  String get inicial => nombre.isNotEmpty ? nombre[0].toUpperCase() : '?';

  String get nivelLabel {
    switch (nivel) {
      case NivelUsuario.iniciado:
        return 'Iniciado';
      case NivelUsuario.colaborador:
        return 'Colaborador';
      case NivelUsuario.experto:
        return 'Experto';
      case NivelUsuario.lider:
        return 'Líder';
      case NivelUsuario.leyenda:
        return 'Leyenda';
    }
  }
}
