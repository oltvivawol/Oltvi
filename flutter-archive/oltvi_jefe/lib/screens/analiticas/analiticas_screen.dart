import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class AnaliticasScreen extends StatelessWidget {
  const AnaliticasScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        title: Text('Analítica',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.calendar_today, color: Colors.white),
          ),
        ],
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _heroChart(),
          const SizedBox(height: 20),
          _kpis(),
          const SizedBox(height: 24),
          Text('Performance por servicio',
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          const SizedBox(height: 12),
          _serviceRow('Viajes', 0.82, OltviColors.accion, '6.480'),
          _serviceRow('Mensajería', 0.45, Colors.cyan, '1.245'),
          _serviceRow('Carga', 0.28, Colors.purpleAccent, '342'),
          _serviceRow('Reportes viales', 0.15, OltviColors.alerta, '189'),
          const SizedBox(height: 24),
          Text('Zonas calientes',
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          const SizedBox(height: 12),
          _zonaRow('Palermo', '32% del total', 1),
          _zonaRow('Microcentro', '24% del total', 2),
          _zonaRow('Recoleta', '14% del total', 3),
          _zonaRow('Belgrano', '11% del total', 4),
          _zonaRow('Caballito', '8% del total', 5),
        ],
      ),
    );
  }

  Widget _heroChart() {
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
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text('Facturación · 7 días',
                  style: OltviTextStyles.eyebrow.copyWith(
                    color: Colors.white.withOpacity(0.6),
                  )),
              const Spacer(),
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: OltviColors.exito.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Row(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    const Icon(Icons.trending_up,
                        color: OltviColors.exito, size: 12),
                    const SizedBox(width: 4),
                    Text('+18%',
                        style: OltviTextStyles.eyebrow.copyWith(
                          color: OltviColors.exito,
                        )),
                  ],
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Text(OltviFormat.money(5842350),
              style: OltviTextStyles.display.copyWith(
                color: Colors.white,
                fontSize: 32,
              )),
          const SizedBox(height: 16),
          SizedBox(
            height: 120,
            child: CustomPaint(
              size: Size.infinite,
              painter: _SparklinePainter(
                values: const [0.4, 0.55, 0.5, 0.7, 0.65, 0.85, 0.95],
                color: OltviColors.accion,
              ),
            ),
          ),
          const SizedBox(height: 8),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: const ['Lu', 'Ma', 'Mi', 'Ju', 'Vi', 'Sá', 'Do']
                .map((d) => Text(d,
                    style: TextStyle(
                      color: Colors.white.withOpacity(0.4),
                      fontSize: 11,
                    )))
                .toList(),
          ),
        ],
      ),
    );
  }

  Widget _kpis() {
    return Row(
      children: [
        _kpi('Tasa éxito', '97.3%', OltviColors.exito),
        const SizedBox(width: 10),
        _kpi('Tiempo prom.', '4.2 min', Colors.cyan),
        const SizedBox(width: 10),
        _kpi('Rating', '4.87★', OltviColors.accion),
      ],
    );
  }

  Widget _kpi(String label, String valor, Color color) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: color.withOpacity(0.2)),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(label,
                style: OltviTextStyles.eyebrow.copyWith(
                  color: Colors.white.withOpacity(0.5),
                  fontSize: 10,
                )),
            const SizedBox(height: 4),
            FittedBox(
              fit: BoxFit.scaleDown,
              alignment: Alignment.centerLeft,
              child: Text(valor,
                  style: OltviTextStyles.titulo.copyWith(color: color)),
            ),
          ],
        ),
      ),
    );
  }

  Widget _serviceRow(
      String label, double pct, Color color, String valor) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 14),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(label,
                  style:
                      OltviTextStyles.cuerpo.copyWith(color: Colors.white)),
              const Spacer(),
              Text(valor,
                  style: OltviTextStyles.subtitulo.copyWith(color: color)),
            ],
          ),
          const SizedBox(height: 6),
          Container(
            height: 8,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.05),
              borderRadius: BorderRadius.circular(4),
            ),
            child: Align(
              alignment: Alignment.centerLeft,
              child: FractionallySizedBox(
                widthFactor: pct,
                child: Container(
                  decoration: BoxDecoration(
                    gradient: LinearGradient(
                      colors: [color, color.withOpacity(0.6)],
                    ),
                    borderRadius: BorderRadius.circular(4),
                    boxShadow: [
                      BoxShadow(
                        color: color.withOpacity(0.4),
                        blurRadius: 8,
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _zonaRow(String label, String sub, int rank) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: const Color(0xFF0E1620),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.white.withOpacity(0.05)),
      ),
      child: Row(
        children: [
          Container(
            width: 32,
            height: 32,
            decoration: BoxDecoration(
              gradient: rank <= 3
                  ? OltviColors.accionGradient
                  : LinearGradient(colors: [
                      Colors.white.withOpacity(0.1),
                      Colors.white.withOpacity(0.05),
                    ]),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Center(
              child: Text('#$rank',
                  style: OltviTextStyles.subtitulo
                      .copyWith(color: Colors.white)),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(label,
                    style: OltviTextStyles.cuerpo
                        .copyWith(color: Colors.white)),
                Text(sub,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.5),
                      fontSize: 12,
                    )),
              ],
            ),
          ),
          Icon(Icons.location_on,
              color: Colors.white.withOpacity(0.3), size: 18),
        ],
      ),
    );
  }
}

class _SparklinePainter extends CustomPainter {
  final List<double> values;
  final Color color;

  _SparklinePainter({required this.values, required this.color});

  @override
  void paint(Canvas canvas, Size size) {
    if (values.length < 2) return;
    final stepX = size.width / (values.length - 1);
    final path = Path();
    final fillPath = Path();

    for (int i = 0; i < values.length; i++) {
      final x = i * stepX;
      final y = size.height * (1 - values[i]);
      if (i == 0) {
        path.moveTo(x, y);
        fillPath.moveTo(x, size.height);
        fillPath.lineTo(x, y);
      } else {
        path.lineTo(x, y);
        fillPath.lineTo(x, y);
      }
    }
    fillPath.lineTo(size.width, size.height);
    fillPath.close();

    canvas.drawPath(
      fillPath,
      Paint()
        ..shader = LinearGradient(
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
          colors: [color.withOpacity(0.4), color.withOpacity(0)],
        ).createShader(Rect.fromLTWH(0, 0, size.width, size.height)),
    );

    canvas.drawPath(
      path,
      Paint()
        ..color = color
        ..strokeWidth = 3
        ..style = PaintingStyle.stroke
        ..strokeCap = StrokeCap.round
        ..strokeJoin = StrokeJoin.round,
    );

    for (int i = 0; i < values.length; i++) {
      final x = i * stepX;
      final y = size.height * (1 - values[i]);
      canvas.drawCircle(
        Offset(x, y),
        i == values.length - 1 ? 5 : 3,
        Paint()..color = color,
      );
      if (i == values.length - 1) {
        canvas.drawCircle(
          Offset(x, y),
          8,
          Paint()..color = color.withOpacity(0.3),
        );
      }
    }
  }

  @override
  bool shouldRepaint(_) => false;
}
