import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class ServicesScreen extends StatelessWidget {
  const ServicesScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const OltviAppBar(
        title: 'Solicitar servicio',
        subtitle: 'MENSAJERÍA · CARGA · ESPECIAL',
      ),
      body: ListView(
        padding: const EdgeInsets.fromLTRB(16, 20, 16, 24),
        children: [
          const _SectionHeader(
            eyebrow: 'OLTVI ECOSYSTEM',
            title: 'Más que un viaje',
            subtitle: 'Todos los servicios del ecosistema en un solo toque.',
          ),
          const SizedBox(height: 20),
          _ServiceTypeCard(
            color: OltviColors.accion,
            icon: Icons.directions_car_rounded,
            title: 'Viajes',
            subtitle: 'Movete con conductores verificados',
            tag: 'Ya disponible',
            onTap: () {},
          ),
          const SizedBox(height: 12),
          _ServiceTypeCard(
            color: OltviColors.principal,
            icon: Icons.markunread_mailbox_rounded,
            title: 'Mensajería',
            subtitle: 'Documentos y paquetes pequeños',
            tag: 'Express',
            onTap: () {},
          ),
          const SizedBox(height: 12),
          _ServiceTypeCard(
            color: OltviColors.exito,
            icon: Icons.local_shipping_rounded,
            title: 'Carga y logística',
            subtitle: 'Mercancía, pallets, fríos',
            tag: 'B2B',
            onTap: () {},
          ),
          const SizedBox(height: 12),
          _ServiceTypeCard(
            color: OltviColors.alerta,
            icon: Icons.report_problem_rounded,
            title: 'Reporte vial',
            subtitle: 'Baches, obras, accidentes',
            tag: 'Comunidad',
            onTap: () {},
          ),
          const SizedBox(height: 28),
          const _SectionHeader(
            eyebrow: 'POR HACER',
            title: 'Acciones rápidas',
          ),
          const SizedBox(height: 12),
          OltviCard(
            child: Column(
              children: [
                _QuickRow(
                  icon: Icons.history_rounded,
                  label: 'Repetir último envío',
                  badge: 'Hace 2h',
                ),
                _Sep(),
                _QuickRow(
                  icon: Icons.calendar_month_rounded,
                  label: 'Programar para más tarde',
                ),
                _Sep(),
                _QuickRow(
                  icon: Icons.group_rounded,
                  label: 'Compartir viaje',
                ),
                _Sep(),
                _QuickRow(
                  icon: Icons.contact_phone_rounded,
                  label: 'Soporte directo 24/7',
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _SectionHeader extends StatelessWidget {
  final String eyebrow;
  final String title;
  final String? subtitle;
  const _SectionHeader({
    required this.eyebrow,
    required this.title,
    this.subtitle,
  });

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(eyebrow, style: OltviTextStyles.eyebrow),
        const SizedBox(height: 4),
        Text(
          title,
          style: OltviTextStyles.titulo.copyWith(letterSpacing: -0.4),
        ),
        if (subtitle != null) ...[
          const SizedBox(height: 4),
          Text(subtitle!, style: OltviTextStyles.cuerpoSecundario),
        ],
      ],
    );
  }
}

class _ServiceTypeCard extends StatelessWidget {
  final Color color;
  final IconData icon;
  final String title;
  final String subtitle;
  final String tag;
  final VoidCallback onTap;

  const _ServiceTypeCard({
    required this.color,
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.tag,
    required this.onTap,
  });

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        borderRadius: BorderRadius.circular(18),
        onTap: onTap,
        child: Container(
          padding: const EdgeInsets.all(18),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(18),
            border: Border.all(color: const Color(0xFFEDF1F5)),
            boxShadow: [
              BoxShadow(
                color: color.withOpacity(0.08),
                blurRadius: 22,
                offset: const Offset(0, 12),
                spreadRadius: -8,
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                width: 56,
                height: 56,
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [color, color.withOpacity(0.7)],
                  ),
                  borderRadius: BorderRadius.circular(14),
                  boxShadow: [
                    BoxShadow(
                      color: color.withOpacity(0.35),
                      blurRadius: 16,
                      spreadRadius: -4,
                    ),
                  ],
                ),
                child: Icon(icon, color: Colors.white, size: 28),
              ),
              const SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            title,
                            style: const TextStyle(
                              fontSize: 17,
                              fontWeight: FontWeight.w800,
                              color: OltviColors.textoPrincipal,
                            ),
                          ),
                        ),
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 8,
                            vertical: 3,
                          ),
                          decoration: BoxDecoration(
                            color: color.withOpacity(0.12),
                            borderRadius: BorderRadius.circular(6),
                          ),
                          child: Text(
                            tag.toUpperCase(),
                            style: TextStyle(
                              fontSize: 9,
                              fontWeight: FontWeight.w800,
                              color: color,
                              letterSpacing: 0.7,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text(
                      subtitle,
                      style: const TextStyle(
                        fontSize: 13,
                        color: OltviColors.textoSecundario,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 8),
              const Icon(
                Icons.chevron_right_rounded,
                color: OltviColors.textoSecundario,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _QuickRow extends StatelessWidget {
  final IconData icon;
  final String label;
  final String? badge;
  const _QuickRow({required this.icon, required this.label, this.badge});

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {},
      child: Padding(
        padding: const EdgeInsets.symmetric(vertical: 14, horizontal: 4),
        child: Row(
          children: [
            Icon(icon, color: OltviColors.principal, size: 20),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                label,
                style: const TextStyle(
                  fontWeight: FontWeight.w600,
                  color: OltviColors.textoPrincipal,
                  fontSize: 14,
                ),
              ),
            ),
            if (badge != null)
              Text(
                badge!,
                style: const TextStyle(
                  fontSize: 11,
                  color: OltviColors.textoSecundario,
                ),
              ),
            const SizedBox(width: 6),
            const Icon(
              Icons.chevron_right_rounded,
              size: 18,
              color: OltviColors.textoSecundario,
            ),
          ],
        ),
      ),
    );
  }
}

class _Sep extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return const Divider(height: 1, color: Color(0xFFEDF1F5));
  }
}
