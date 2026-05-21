import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class PerfilConductorScreen extends StatelessWidget {
  const PerfilConductorScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final u = MockDataService.instance.conductorDemo;

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Text('Perfil',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _heroPerfil(u),
          const SizedBox(height: 20),
          _section('Performance', [
            _opcion(Icons.star, 'Rating y reseñas',
                u.rating.toStringAsFixed(2)),
            _opcion(Icons.workspace_premium, 'Nivel', u.nivelLabel),
            _opcion(Icons.history, 'Historial de viajes'),
          ]),
          const SizedBox(height: 14),
          _section('Cuenta', [
            _opcion(Icons.person, 'Datos personales'),
            _opcion(Icons.account_balance, 'Datos bancarios'),
            _opcion(Icons.security, 'Seguridad'),
          ]),
          const SizedBox(height: 14),
          _section('Configuración', [
            _opcion(Icons.notifications, 'Notificaciones'),
            _opcion(Icons.map, 'Zonas de trabajo'),
            _opcion(Icons.support_agent, 'Soporte 24/7'),
          ]),
          const SizedBox(height: 20),
          OutlinedButton.icon(
            onPressed: () {},
            icon: const Icon(Icons.logout, color: Colors.white70),
            label: const Text('Cerrar sesión',
                style: TextStyle(color: Colors.white70)),
            style: OutlinedButton.styleFrom(
              padding: const EdgeInsets.symmetric(vertical: 16),
              side: BorderSide(color: Colors.white.withOpacity(0.2)),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(14),
              ),
            ),
          ),
          const SizedBox(height: 20),
          Center(
            child: Text('OLTVI Trabajo · v${OltviConstants.version}',
                style: OltviTextStyles.cuerpo.copyWith(
                  color: Colors.white.withOpacity(0.3),
                  fontSize: 12,
                )),
          ),
        ],
      ),
    );
  }

  Widget _heroPerfil(Usuario u) {
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
      child: Row(
        children: [
          Container(
            width: 72,
            height: 72,
            decoration: BoxDecoration(
              gradient: OltviColors.accionGradient,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(
                  color: OltviColors.accion.withOpacity(0.4),
                  blurRadius: 20,
                ),
              ],
            ),
            child: Center(
              child: Text(u.inicial,
                  style: OltviTextStyles.display.copyWith(
                    color: Colors.white,
                    fontSize: 30,
                  )),
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(u.nombre,
                    style: OltviTextStyles.titulo
                        .copyWith(color: Colors.white)),
                Text('Conductor OLTVI',
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.6),
                    )),
                const SizedBox(height: 6),
                Row(
                  children: [
                    const Icon(Icons.star,
                        color: OltviColors.accion, size: 16),
                    const SizedBox(width: 4),
                    Text(u.rating.toStringAsFixed(2),
                        style: OltviTextStyles.subtitulo
                            .copyWith(color: Colors.white)),
                    const SizedBox(width: 8),
                    Text('· ${u.puntos} pts',
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.5),
                        )),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _section(String titulo, List<Widget> items) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 8),
          child: Text(titulo,
              style: OltviTextStyles.eyebrow.copyWith(
                color: Colors.white.withOpacity(0.5),
              )),
        ),
        Container(
          decoration: BoxDecoration(
            color: const Color(0xFF0E1620),
            borderRadius: BorderRadius.circular(14),
            border: Border.all(color: Colors.white.withOpacity(0.05)),
          ),
          child: Column(children: items),
        ),
      ],
    );
  }

  Widget _opcion(IconData icon, String label, [String? trailing]) {
    return InkWell(
      onTap: () {},
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        child: Row(
          children: [
            Icon(icon, color: OltviColors.accion, size: 22),
            const SizedBox(width: 12),
            Expanded(
              child: Text(label,
                  style: OltviTextStyles.cuerpo
                      .copyWith(color: Colors.white)),
            ),
            if (trailing != null)
              Text(trailing,
                  style: OltviTextStyles.cuerpo.copyWith(
                    color: OltviColors.accion,
                  )),
            const SizedBox(width: 6),
            Icon(Icons.chevron_right, color: Colors.white.withOpacity(0.3)),
          ],
        ),
      ),
    );
  }
}
