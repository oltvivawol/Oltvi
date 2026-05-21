import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

import '../screens/dashboard/dashboard_screen.dart';
import '../screens/flota/flota_screen.dart';
import '../screens/incidentes/incidentes_screen.dart';
import '../screens/usuarios/usuarios_screen.dart';
import '../screens/analiticas/analiticas_screen.dart';

class OltviJefeApp extends StatefulWidget {
  const OltviJefeApp({super.key});

  @override
  State<OltviJefeApp> createState() => _OltviJefeAppState();
}

class _OltviJefeAppState extends State<OltviJefeApp> {
  int _index = 0;

  static const _tabs = [
    _TabDef('Mando', Icons.dashboard),
    _TabDef('Flota', Icons.map),
    _TabDef('Reportes', Icons.report),
    _TabDef('Usuarios', Icons.people),
    _TabDef('Analítica', Icons.analytics),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.black,
      body: IndexedStack(
        index: _index,
        children: const [
          DashboardScreen(),
          FlotaScreen(),
          IncidentesScreen(),
          UsuariosScreen(),
          AnaliticasScreen(),
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
          padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 8),
          child: Row(
            children: List.generate(_tabs.length, (i) {
              final tab = _tabs[i];
              final selected = i == _index;
              return Expanded(
                child: InkWell(
                  onTap: () => setState(() => _index = i),
                  borderRadius: BorderRadius.circular(12),
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 220),
                    padding: const EdgeInsets.symmetric(
                        vertical: 8, horizontal: 4),
                    decoration: BoxDecoration(
                      gradient: selected
                          ? OltviColors.accionGradient
                          : null,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Column(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        Icon(tab.icon,
                            color: selected
                                ? Colors.white
                                : Colors.white.withOpacity(0.5),
                            size: 20),
                        const SizedBox(height: 4),
                        Text(
                          tab.label,
                          style: TextStyle(
                            color: selected
                                ? Colors.white
                                : Colors.white.withOpacity(0.5),
                            fontSize: 10,
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
