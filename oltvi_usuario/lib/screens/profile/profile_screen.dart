import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class ProfileScreen extends StatelessWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final user = MockDataService.instance.clienteDemo;

    return Scaffold(
      appBar: const OltviAppBar(title: 'Perfil'),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _heroPerfil(user),
          const SizedBox(height: 20),
          _stats(),
          const SizedBox(height: 24),
          _section('Cuenta', [
            _opcion(Icons.person_outline, 'Datos personales'),
            _opcion(Icons.security_outlined, 'Seguridad'),
            _opcion(Icons.location_on_outlined, 'Direcciones guardadas'),
            _opcion(Icons.notifications_outlined, 'Notificaciones'),
          ]),
          const SizedBox(height: 16),
          _section('OLTVI', [
            _opcion(Icons.workspace_premium_outlined, 'Programa de puntos'),
            _opcion(Icons.card_giftcard_outlined, 'Invitá y ganá'),
            _opcion(Icons.support_agent_outlined, 'Soporte 24/7'),
            _opcion(Icons.gavel_outlined, 'Términos y privacidad'),
          ]),
          const SizedBox(height: 16),
          _section('Apariencia', [
            _opcion(Icons.dark_mode_outlined, 'Tema',
                trailing: 'Automático'),
            _opcion(Icons.language_outlined, 'Idioma', trailing: 'Español'),
          ]),
          const SizedBox(height: 24),
          OltviSecondaryButton(
            label: 'Cerrar sesión',
            icon: Icons.logout,
            color: OltviColors.error,
            onPressed: () {},
          ),
          const SizedBox(height: 16),
          Center(
            child: Text(
              'OLTVI · ${OltviConstants.slogan}',
              style: OltviTextStyles.cuerpo.copyWith(
                color: OltviColors.textoSecundario,
                fontSize: 12,
              ),
            ),
          ),
          const SizedBox(height: 24),
        ],
      ),
    );
  }

  Widget _heroPerfil(Usuario u) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: OltviColors.heroGradient,
        borderRadius: BorderRadius.circular(20),
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
                  color: OltviColors.accion.withOpacity(0.5),
                  blurRadius: 20,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Center(
              child: Text(
                u.inicial,
                style: OltviTextStyles.display.copyWith(
                  color: Colors.white,
                  fontSize: 30,
                ),
              ),
            ),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(u.nombre,
                    style: OltviTextStyles.titulo.copyWith(
                      color: Colors.white,
                    )),
                const SizedBox(height: 4),
                Text(u.correo,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.7),
                    )),
                const SizedBox(height: 8),
                Container(
                  padding: const EdgeInsets.symmetric(
                      horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: OltviColors.accion.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(20),
                    border: Border.all(color: OltviColors.accion),
                  ),
                  child: Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      const Icon(Icons.workspace_premium,
                          color: OltviColors.accion, size: 14),
                      const SizedBox(width: 4),
                      Text('Nivel ${u.nivelLabel} · ${u.puntos} pts',
                          style: OltviTextStyles.eyebrow.copyWith(
                            color: OltviColors.accion,
                          )),
                    ],
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _stats() {
    return Row(
      children: [
        _statCard('Viajes', '142', Icons.directions_car_outlined),
        const SizedBox(width: 10),
        _statCard('Envíos', '38', Icons.local_shipping_outlined),
        const SizedBox(width: 10),
        _statCard('Reportes', '12', Icons.report_outlined),
      ],
    );
  }

  Widget _statCard(String label, String valor, IconData icon) {
    return Expanded(
      child: OltviCard(
        padding: const EdgeInsets.all(14),
        child: Column(
          children: [
            Icon(icon, color: OltviColors.accion, size: 24),
            const SizedBox(height: 8),
            Text(valor, style: OltviTextStyles.titulo),
            Text(label,
                style: OltviTextStyles.cuerpo.copyWith(
                  color: OltviColors.textoSecundario,
                  fontSize: 12,
                )),
          ],
        ),
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
                color: OltviColors.textoSecundario,
              )),
        ),
        OltviCard(
          padding: EdgeInsets.zero,
          child: Column(children: items),
        ),
      ],
    );
  }

  Widget _opcion(IconData icon, String label, {String? trailing}) {
    return InkWell(
      onTap: () {},
      borderRadius: BorderRadius.circular(12),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
        child: Row(
          children: [
            Icon(icon, color: OltviColors.principal, size: 22),
            const SizedBox(width: 14),
            Expanded(child: Text(label, style: OltviTextStyles.cuerpo)),
            if (trailing != null) ...[
              Text(trailing,
                  style: OltviTextStyles.cuerpo.copyWith(
                    color: OltviColors.textoSecundario,
                  )),
              const SizedBox(width: 6),
            ],
            Icon(Icons.chevron_right,
                color: OltviColors.textoSecundario.withOpacity(0.5)),
          ],
        ),
      ),
    );
  }
}
