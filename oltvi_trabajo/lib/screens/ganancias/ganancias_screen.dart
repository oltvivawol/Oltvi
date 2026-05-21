import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class GananciasScreen extends StatefulWidget {
  const GananciasScreen({super.key});

  @override
  State<GananciasScreen> createState() => _GananciasScreenState();
}

class _GananciasScreenState extends State<GananciasScreen> {
  int _periodo = 1; // 0 hoy, 1 semana, 2 mes, 3 año

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: Text('Ganancias',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
        elevation: 0,
      ),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _heroBalance(),
          const SizedBox(height: 20),
          _periodSelector(),
          const SizedBox(height: 20),
          _resumen(),
          const SizedBox(height: 24),
          Text('Movimientos',
              style: OltviTextStyles.subtitulo.copyWith(color: Colors.white)),
          const SizedBox(height: 12),
          _movItem('Viaje · Aeroparque → Recoleta', '14:23', 4250),
          _movItem('Envío · Palermo → Belgrano', '12:08', 1850),
          _movItem('Viaje · Centro → Caballito', '10:42', 2900),
          _movItem('Viaje · Villa Crespo → UBA', '09:15', 3100),
          _movItem('Carga · Microcentro → La Boca', '08:30', 5800),
        ],
      ),
    );
  }

  Widget _heroBalance() {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Color(0xFF1A2733), Color(0xFF0E1620)],
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: OltviColors.accion.withOpacity(0.3)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text('Esta semana',
              style: OltviTextStyles.eyebrow.copyWith(
                color: Colors.white.withOpacity(0.6),
              )),
          const SizedBox(height: 8),
          Text(OltviFormat.money(67450),
              style: OltviTextStyles.display.copyWith(
                color: Colors.white,
                fontSize: 42,
              )),
          const SizedBox(height: 4),
          Row(
            children: [
              const Icon(Icons.trending_up,
                  color: OltviColors.exito, size: 18),
              const SizedBox(width: 4),
              Text('+18% vs semana anterior',
                  style: OltviTextStyles.cuerpo.copyWith(
                    color: OltviColors.exito,
                  )),
            ],
          ),
          const SizedBox(height: 20),
          OltviPrimaryButton(
            label: 'Retirar',
            icon: Icons.arrow_downward,
            onPressed: () {},
          ),
        ],
      ),
    );
  }

  Widget _periodSelector() {
    const labels = ['Hoy', 'Semana', 'Mes', 'Año'];
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: Colors.white.withOpacity(0.05),
        borderRadius: BorderRadius.circular(14),
      ),
      child: Row(
        children: List.generate(labels.length, (i) {
          final selected = i == _periodo;
          return Expanded(
            child: GestureDetector(
              onTap: () => setState(() => _periodo = i),
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 10),
                decoration: BoxDecoration(
                  gradient: selected ? OltviColors.accionGradient : null,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Center(
                  child: Text(labels[i],
                      style: OltviTextStyles.cuerpo.copyWith(
                        color: selected
                            ? Colors.white
                            : Colors.white.withOpacity(0.6),
                        fontWeight:
                            selected ? FontWeight.w700 : FontWeight.w500,
                      )),
                ),
              ),
            ),
          );
        }),
      ),
    );
  }

  Widget _resumen() {
    return Row(
      children: [
        _miniStat('Viajes', '34', Icons.directions_car),
        const SizedBox(width: 10),
        _miniStat('Horas', '38', Icons.timer),
        const SizedBox(width: 10),
        _miniStat('Rating', '4.92', Icons.star),
      ],
    );
  }

  Widget _miniStat(String label, String valor, IconData icon) {
    return Expanded(
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.white.withOpacity(0.06)),
        ),
        child: Column(
          children: [
            Icon(icon, color: OltviColors.accion, size: 22),
            const SizedBox(height: 8),
            Text(valor,
                style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
            Text(label,
                style: OltviTextStyles.cuerpo.copyWith(
                  color: Colors.white.withOpacity(0.5),
                  fontSize: 11,
                )),
          ],
        ),
      ),
    );
  }

  Widget _movItem(String titulo, String hora, int monto) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 10),
      child: Container(
        padding: const EdgeInsets.all(14),
        decoration: BoxDecoration(
          color: const Color(0xFF0E1620),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: Colors.white.withOpacity(0.05)),
        ),
        child: Row(
          children: [
            Container(
              width: 40,
              height: 40,
              decoration: BoxDecoration(
                color: OltviColors.exito.withOpacity(0.15),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(Icons.arrow_downward,
                  color: OltviColors.exito, size: 20),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(titulo,
                      style: OltviTextStyles.cuerpo
                          .copyWith(color: Colors.white)),
                  Text(hora,
                      style: OltviTextStyles.cuerpo.copyWith(
                        color: Colors.white.withOpacity(0.5),
                        fontSize: 12,
                      )),
                ],
              ),
            ),
            Text('+ ${OltviFormat.money(monto)}',
                style: OltviTextStyles.subtitulo.copyWith(
                  color: OltviColors.exito,
                )),
          ],
        ),
      ),
    );
  }
}
