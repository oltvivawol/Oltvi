import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

import '../screens/operacion/operacion_screen.dart';
import '../screens/ganancias/ganancias_screen.dart';
import '../screens/vehiculo/vehiculo_screen.dart';
import '../screens/perfil_conductor/perfil_conductor_screen.dart';

class OltviTrabajoApp extends StatefulWidget {
  const OltviTrabajoApp({super.key});

  @override
  State<OltviTrabajoApp> createState() => _OltviTrabajoAppState();
}

class _OltviTrabajoAppState extends State<OltviTrabajoApp> {
  int _index = 0;

  static const _tabs = [
    _TabDef('Operación', Icons.radar),
    _TabDef('Ganancias', Icons.account_balance_wallet),
    _TabDef('Vehículo', Icons.directions_car),
    _TabDef('Perfil', Icons.person),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: IndexedStack(
        index: _index,
        children: const [
          OperacionScreen(),
          GananciasScreen(),
          VehiculoScreen(),
          PerfilConductorScreen(),
        ],
      ),
      bottomNavigationBar: _bottomNav(),
    );
  }

  Widget _bottomNav() {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF0E1620),
        border: Border(
          top: BorderSide(color: Colors.white.withOpacity(0.06)),
        ),
      ),
      child: SafeArea(
        top: false,
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: List.generate(_tabs.length, (i) {
              final tab = _tabs[i];
              final selected = i == _index;
              return Expanded(
                child: InkWell(
                  onTap: () => setState(() => _index = i),
                  borderRadius: BorderRadius.circular(14),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 220),
                    padding: const EdgeInsets.symmetric(
                        vertical: 10, horizontal: 8),
                    decoration: BoxDecoration(
                      gradient: selected
                          ? OltviColors.accionGradient
                          : null,
                      borderRadius: BorderRadius.circular(14),
                      boxShadow: selected
                          ? [
                              BoxShadow(
                                color: OltviColors.accion.withOpacity(0.4),
                                blurRadius: 16,
                                offset: const Offset(0, 4),
                              )
                            ]
                          : null,
                    ),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(tab.icon,
                            color: selected
                                ? Colors.white
                                : Colors.white.withOpacity(0.5),
                            size: 22),
                        const SizedBox(height: 4),
                        Text(
                          tab.label,
                          style: TextStyle(
                            color: selected
                                ? Colors.white
                                : Colors.white.withOpacity(0.5),
                            fontSize: 11,
                            fontWeight: selected
                                ? FontWeight.w700
                                : FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              );
            }),
          ),
        ),
      ),
    );
  }
}

class _TabDef {
  final String label;
  final IconData icon;
  const _TabDef(this.label, this.icon);
}
