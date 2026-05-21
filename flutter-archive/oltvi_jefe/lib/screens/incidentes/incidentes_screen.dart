import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class IncidentesScreen extends StatefulWidget {
  const IncidentesScreen({super.key});

  @override
  State<IncidentesScreen> createState() => _IncidentesScreenState();
}

class _IncidentesScreenState extends State<IncidentesScreen> {
  String _filtro = 'todos'; // todos, pendientes, confirmados, criticos

  @override
  Widget build(BuildContext context) {
    var eventos = MockDataService.instance.eventos;
    if (_filtro == 'pendientes') {
      eventos = eventos
          .where((e) =>
              e.estado == EstadoEvento.reportado ||
              e.estado == EstadoEvento.verificando)
          .toList();
    } else if (_filtro == 'confirmados') {
      eventos = eventos
          .where((e) => e.estado == EstadoEvento.confirmado)
          .toList();
    } else if (_filtro == 'criticos') {
      eventos = eventos
          .where((e) =>
              e.tipo == TipoEventoVial.accidente ||
              e.tipo == TipoEventoVial.corteTotal ||
              e.tipo == TipoEventoVial.fugaGas)
          .toList();
    }

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        title: Text('Reportes viales',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: SingleChildScrollView(
              scrollDirection: Axis.horizontal,
              child: Row(
                children: [
                  _filterChip('todos', 'Todos'),
                  _filterChip('pendientes', 'Pendientes'),
                  _filterChip('confirmados', 'Confirmados'),
                  _filterChip('criticos', 'Críticos'),
                ],
              ),
            ),
          ),
          Expanded(
            child: eventos.isEmpty
                ? Center(
                    child: Text('Sin reportes en esta categoría',
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.5),
                        )),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                    itemCount: eventos.length,
                    itemBuilder: (context, i) => _eventoCard(eventos[i]),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _filterChip(String key, String label) {
    final selected = _filtro == key;
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: () => setState(() => _filtro = key),
        child: Container(
          padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
          decoration: BoxDecoration(
            gradient: selected ? OltviColors.accionGradient : null,
            color: selected ? null : Colors.white.withOpacity(0.05),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(
              color: selected
                  ? OltviColors.accion
                  : Colors.white.withOpacity(0.1),
            ),
          ),
          child: Text(label,
              style: OltviTextStyles.cuerpo.copyWith(
                color: selected
                    ? Colors.white
                    : Colors.white.withOpacity(0.7),
                fontWeight: selected ? FontWeight.w700 : FontWeight.w500,
              )),
        ),
      ),
    );
  }

  Widget _eventoCard(EventoVial e) {
    final ic = _iconForTipo(e.tipo);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF0E1620),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.white.withOpacity(0.06)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 44,
                height: 44,
                decoration: BoxDecoration(
                  color: ic.color.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(12),
                  border: Border.all(color: ic.color.withOpacity(0.4)),
                ),
                child: Icon(ic.icon, color: ic.color, size: 22),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(e.tipoLabel,
                        style: OltviTextStyles.subtitulo
                            .copyWith(color: Colors.white)),
                    Text(e.descripcion,
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.6),
                          fontSize: 12,
                        )),
                  ],
                ),
              ),
              _estadoChip(e.estado),
            ],
          ),
          const SizedBox(height: 12),
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.03),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Column(
              children: [
                _row(Icons.location_on, e.ubicacion.nombre),
                const SizedBox(height: 6),
                _row(Icons.thumb_up_outlined,
                    '${e.verificaciones} confirmaciones · ${e.rechazos} rechazos'),
                const SizedBox(height: 6),
                _row(Icons.psychology_outlined,
                    'IA: ${(e.iaScore * 100).toStringAsFixed(0)}% confianza'),
              ],
            ),
          ),
          if (e.estado == EstadoEvento.reportado ||
              e.estado == EstadoEvento.verificando) ...[
            const SizedBox(height: 12),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: () => _verificar(e, false),
                    style: OutlinedButton.styleFrom(
                      foregroundColor: OltviColors.error,
                      side: const BorderSide(color: OltviColors.error),
                      padding: const EdgeInsets.symmetric(vertical: 12),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    child: const Text('Descartar'),
                  ),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: OltviPrimaryButton(
                    label: 'Confirmar',
                    icon: Icons.check_circle,
                    onPressed: () => _verificar(e, true),
                  ),
                ),
              ],
            ),
          ],
        ],
      ),
    );
  }

  void _verificar(EventoVial e, bool aceptar) {
    setState(() {
      MockDataService.instance.actualizarEvento(
        e.copyWith(
          estado: aceptar
              ? EstadoEvento.confirmado
              : EstadoEvento.descartado,
        ),
      );
    });
  }

  Widget _row(IconData icon, String text) {
    return Row(
      children: [
        Icon(icon, color: Colors.white.withOpacity(0.5), size: 14),
        const SizedBox(width: 6),
        Expanded(
          child: Text(text,
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.7),
                fontSize: 12,
              )),
        ),
      ],
    );
  }

  Widget _estadoChip(EstadoEvento estado) {
    Color color;
    String label;
    switch (estado) {
      case EstadoEvento.reportado:
        color = OltviColors.alerta;
        label = 'Reportado';
        break;
      case EstadoEvento.verificando:
        color = OltviColors.accion;
        label = 'Verificando';
        break;
      case EstadoEvento.confirmado:
        color = OltviColors.exito;
        label = 'Confirmado';
        break;
      case EstadoEvento.descartado:
        color = Colors.white.withOpacity(0.4);
        label = 'Descartado';
        break;
      case EstadoEvento.resuelto:
        color = OltviColors.principal;
        label = 'Resuelto';
        break;
    }
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: color.withOpacity(0.15),
        borderRadius: BorderRadius.circular(8),
      ),
      child: Text(label,
          style: OltviTextStyles.eyebrow.copyWith(color: color)),
    );
  }

  _IconAndColor _iconForTipo(TipoEventoVial t) {
    switch (t) {
      case TipoEventoVial.bache:
        return _IconAndColor(Icons.warning, OltviColors.alerta);
      case TipoEventoVial.obra:
        return _IconAndColor(Icons.construction, OltviColors.alerta);
      case TipoEventoVial.corteTotal:
        return _IconAndColor(Icons.block, OltviColors.error);
      case TipoEventoVial.desvio:
        return _IconAndColor(Icons.alt_route, OltviColors.alerta);
      case TipoEventoVial.semaforoFallando:
        return _IconAndColor(Icons.traffic, OltviColors.error);
      case TipoEventoVial.fugaAgua:
        return _IconAndColor(Icons.water_drop, Colors.cyan);
      case TipoEventoVial.fugaGas:
        return _IconAndColor(
            Icons.local_fire_department, OltviColors.error);
      case TipoEventoVial.cloacas:
        return _IconAndColor(Icons.plumbing, Colors.brown);
      case TipoEventoVial.alumbrado:
        return _IconAndColor(Icons.lightbulb, OltviColors.alerta);
      case TipoEventoVial.senalizacion:
        return _IconAndColor(Icons.signpost, OltviColors.alerta);
      case TipoEventoVial.accidente:
        return _IconAndColor(Icons.car_crash, OltviColors.error);
      case TipoEventoVial.trafico:
        return _IconAndColor(Icons.traffic, OltviColors.alerta);
      case TipoEventoVial.otro:
        return _IconAndColor(Icons.report, OltviColors.principal);
    }
  }
}

class _IconAndColor {
  final IconData icon;
  final Color color;
  _IconAndColor(this.icon, this.color);
}
