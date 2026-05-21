import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class PaymentsScreen extends StatelessWidget {
  const PaymentsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: const OltviAppBar(title: 'Pagos'),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          _saldoCard(),
          const SizedBox(height: 20),
          const Text('Métodos de pago', style: OltviTextStyles.subtitulo),
          const SizedBox(height: 10),
          _metodoTile(Icons.credit_card, 'Visa •••• 4521', 'Vence 09/27', true),
          const SizedBox(height: 10),
          _metodoTile(
              Icons.account_balance, 'MercadoPago', 'martin@oltvi.app', false),
          const SizedBox(height: 10),
          _metodoTile(
              Icons.payments_outlined, 'Efectivo', 'En el viaje', false),
          const SizedBox(height: 12),
          OltviSecondaryButton(
            label: 'Agregar método',
            icon: Icons.add,
            onPressed: () {},
          ),
          const SizedBox(height: 24),
          const Text('Historial reciente',
              style: OltviTextStyles.subtitulo),
          const SizedBox(height: 10),
          _historialItem('Viaje a Aeroparque', '12 may · 19:42', 8450, true),
          _historialItem('Envío a Palermo', '11 may · 14:10', 1850, true),
          _historialItem('Recarga de saldo', '10 may · 09:00', 5000, false),
          _historialItem('Viaje a UBA', '09 may · 08:25', 3200, true),
        ],
      ),
    );
  }

  Widget _saldoCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: OltviColors.heroGradient,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: OltviColors.principal.withOpacity(0.3),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text('SALDO OLTVI',
                  style: OltviTextStyles.eyebrow.copyWith(
                    color: Colors.white.withOpacity(0.7),
                  )),
              const Spacer(),
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: OltviColors.accion.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(color: OltviColors.accion),
                ),
                child: Text('Nivel Experto',
                    style: OltviTextStyles.eyebrow.copyWith(
                      color: OltviColors.accion,
                    )),
              ),
            ],
          ),
          const SizedBox(height: 14),
          Text(OltviFormat.money(12450),
              style: OltviTextStyles.display.copyWith(
                color: Colors.white,
                fontSize: 38,
              )),
          const SizedBox(height: 4),
          Text('Disponible para viajes y envíos',
              style: OltviTextStyles.cuerpo.copyWith(
                color: Colors.white.withOpacity(0.7),
              )),
          const SizedBox(height: 18),
          Row(
            children: [
              Expanded(
                child: OltviPrimaryButton(
                  label: 'Cargar saldo',
                  icon: Icons.add_circle_outline,
                  onPressed: () {},
                ),
              ),
              const SizedBox(width: 10),
              Container(
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.15),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: IconButton(
                  onPressed: () {},
                  icon: const Icon(Icons.qr_code_scanner, color: Colors.white),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _metodoTile(IconData icon, String titulo, String sub, bool principal) {
    return OltviCard(
      padding: const EdgeInsets.all(14),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              color: OltviColors.principal.withOpacity(0.08),
              borderRadius: BorderRadius.circular(12),
            ),
            child: const Icon(Icons.credit_card, color: OltviColors.principal),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(titulo, style: OltviTextStyles.subtitulo),
                const SizedBox(height: 2),
                Text(sub,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: OltviColors.textoSecundario,
                    )),
              ],
            ),
          ),
          if (principal)
            Container(
              padding:
                  const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: OltviColors.accion.withOpacity(0.15),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Text('Principal',
                  style: OltviTextStyles.eyebrow.copyWith(
                    color: OltviColors.accion,
                  )),
            ),
        ],
      ),
    );
  }

  Widget _historialItem(String titulo, String fecha, int monto, bool gasto) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          Container(
            width: 40,
            height: 40,
            decoration: BoxDecoration(
              color: (gasto ? OltviColors.error : OltviColors.exito)
                  .withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Icon(
              gasto ? Icons.arrow_upward : Icons.arrow_downward,
              color: gasto ? OltviColors.error : OltviColors.exito,
              size: 20,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(titulo, style: OltviTextStyles.cuerpo),
                Text(fecha,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: OltviColors.textoSecundario,
                      fontSize: 12,
                    )),
              ],
            ),
          ),
          Text(
            '${gasto ? '-' : '+'} ${OltviFormat.money(monto)}',
            style: OltviTextStyles.subtitulo.copyWith(
              color: gasto ? OltviColors.error : OltviColors.exito,
            ),
          ),
        ],
      ),
    );
  }
}
