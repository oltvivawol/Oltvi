import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final servicios = MockDataService.instance.servicios;
    final activos = servicios.where((s) =>
        s.estado == EstadoServicio.enRuta ||
        s.estado == EstadoServicio.asignado ||
        s.estado == EstadoServicio.enCamino ||
        s.estado == EstadoServicio.llegando).length;
    final pendientes = servicios
        .where((s) => s.estado == EstadoServicio.solicitado)
        .length;
    final completados = servicios
        .where((s) => s.estado == EstadoServicio.entregado)
        .length;

    return Scaffold(
      backgroundColor: Colors.black,
      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(child: _heroHeader()),
          SliverPadding(
            padding: const EdgeInsets.all(16),
            sliver: SliverList(
              delegate: SliverChildListDelegate([
                _kpiGrid(activos, pendientes, completados),
                const SizedBox(height: 20),
                _alertasBanner(),
                const SizedBox(height: 20),
                Text('Operación en vivo',
                    style: OltviTextStyles.subtitulo
                        .copyWith(color: Colors.white)),
                const SizedBox(height: 12),
                ...servicios.take(5).map(_servicioFila),
                const SizedBox(height: 24),
              ]),
            ),
          ),
        ],
      ),
    );
  }

  Widget _heroHeader() {
    return Container(
      padding: const EdgeInsets.fromLTRB(16, 50, 16, 24),
      decoration: const BoxDecoration(gradient: OltviColors.heroGradient),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const OltviLogo(size: 40),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('MANDO',
                      style: OltviTextStyles.eyebrow.copyWith(
                        color: OltviColors.accion,
                      )),
                  Text('Centro de Operaciones',
                      style: OltviTextStyles.titulo
                          .copyWith(color: Colors.white)),
                ],
              ),
              const Spacer(),
              Container(
                padding: const EdgeInsets.symmetric(
                    horizontal: 10, vertical: 6),
                decoration: BoxDecoration(
                  color: OltviColors.exito.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: OltviColors.exito),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Container(
                      width: 8,
                      height: 8,
                      decoration: const BoxDecoration(
                        color: OltviColors.exito,
                        shape: BoxShape.circle,
                      ),
                    ),
                    const SizedBox(width: 6),
                    Text('SISTEMAS OK',
                        style: OltviTextStyles.eyebrow.copyWith(
                          color: OltviColors.exito,
                        )),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Text('Buenas tardes, Jefe',
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.7),
              )),
          Text('Aquí está la radiografía operativa de hoy',
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.5),
              )),
        ],
      ),
    );
  }

  Widget _kpiGrid(int activos, int pendientes, int completados) {
    return GridView.count(
      crossAxisCount: 2,
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      mainAxisSpacing: 10,
      crossAxisSpacing: 10,
      childAspectRatio: 1.3,
      children: [
        _kpiCard('Servicios activos', '$activos', Icons.local_taxi,
            OltviColors.accion, '+12% vs ayer'),
        _kpiCard('Pendientes', '$pendientes', Icons.pending,
            OltviColors.alerta, 'En cola'),
        _kpiCard('Completados', '$completados', Icons.check_circle,
            OltviColors.exito, 'Hoy'),
        _kpiCard('Facturación', OltviFormat.money(842350),
            Icons.attach_money, Colors.cyan, '+18%'),
      ],
    );
  }

  Widget _kpiCard(String label, String valor, IconData icon, Color color,
      String hint) {
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF0E1620),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: color.withOpacity(0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(6),
                decoration: BoxDecoration(
                  color: color.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: color, size: 18),
              ),
              const Spacer(),
              Flexible(
                child: Text(hint,
                    overflow: TextOverflow.ellipsis,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.4),
                      fontSize: 10,
                    )),
              ),
            ],
          ),
          const Spacer(),
          FittedBox(
            fit: BoxFit.scaleDown,
            alignment: Alignment.centerLeft,
            child: Text(valor,
                style: OltviTextStyles.display.copyWith(
                  color: Colors.white,
                  fontSize: 26,
                )),
          ),
          Text(label,
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.5),
                fontSize: 11,
              )),
        ],
      ),
    );
  }

  Widget _alertasBanner() {
    final pendientes = MockDataService.instance.eventos
        .where((e) =>
            e.estado == EstadoEvento.reportado ||
            e.estado == EstadoEvento.verificando)
        .length;
    return Container(
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: OltviColors.alerta.withOpacity(0.1),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: OltviColors.alerta.withOpacity(0.4)),
      ),
      child: Row(
        children: [
          const Icon(Icons.warning_amber_rounded,
              color: OltviColors.alerta, size: 24),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '$pendientes reportes viales pendientes de verificar',
                  style: OltviTextStyles.subtitulo
                      .copyWith(color: Colors.white),
                ),
                Text('Pueden afectar las rutas activas',
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.6),
                      fontSize: 12,
                    )),
              ],
            ),
          ),
          Icon(Icons.chevron_right, color: Colors.white.withOpacity(0.5)),
        ],
      ),
    );
  }

  Widget _servicioFila(Servicio s) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 8),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.white.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            Container(
              width: 36,
              height: 36,
              decoration: BoxDecoration(
                color: OltviColors.principal,
                borderRadius: BorderRadius.circular(10),
              ),
              child: Icon(_iconoServicio(s.tipo),
                  color: Colors.white, size: 18),
            ),
            const SizedBox(width: 10),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('${s.origen.nombre} → ${s.destino.nombre}',
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: OltviTextStyles.cuerpo
                          .copyWith(color: Colors.white)),
                  Text(
                    '${s.tipoLabel} · ${OltviFormat.distance(s.distanciaKm)}',
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.5),
                      fontSize: 11,
                    ),
                  ),
                ],
              ),
            ),
            OltviStatusChip.forEstado(s.estado, dense: true),
          ],
        ),
      ),
    );
  }

  IconData _iconoServicio(TipoServicio t) {
    switch (t) {
      case TipoServicio.pasajero:
        return Icons.person;
      case TipoServicio.mensajeria:
        return Icons.markunread_mailbox;
      case TipoServicio.carga:
        return Icons.local_shipping;
      case TipoServicio.vial:
        return Icons.report;
    }
  }
}
