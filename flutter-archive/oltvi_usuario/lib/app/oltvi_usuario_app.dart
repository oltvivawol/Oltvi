import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

import '../screens/ride/ride_screen.dart';
import '../screens/services/services_screen.dart';
import '../screens/tracking/tracking_screen.dart';
import '../screens/payments/payments_screen.dart';
import '../screens/profile/profile_screen.dart';

class OltviUsuarioApp extends StatefulWidget {
  const OltviUsuarioApp({super.key});

  @override
  State<OltviUsuarioApp> createState() => _OltviUsuarioAppState();
}

class _OltviUsuarioAppState extends State<OltviUsuarioApp> {
  int _index = 0;

  // PRIORITY: The ride / map screen is the first thing the user sees -
  // the "cover" of the ecosystem, the immersive AAA-game-like experience.
  final _screens = const [
    RideScreen(),
    ServicesScreen(),
    TrackingScreen(),
    PaymentsScreen(),
    ProfileScreen(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: IndexedStack(index: _index, children: _screens),
      bottomNavigationBar: _OltviUsuarioNav(
        index: _index,
        onChanged: (i) => setState(() => _index = i),
      ),
    );
  }
}

class _OltviUsuarioNav extends StatelessWidget {
  final int index;
  final ValueChanged<int> onChanged;

  const _OltviUsuarioNav({required this.index, required this.onChanged});

  static const _items = <_NavItem>[
    _NavItem(Icons.explore_rounded, 'Viajar'),
    _NavItem(Icons.local_shipping_rounded, 'Solicitar'),
    _NavItem(Icons.gps_fixed_rounded, 'Seguir'),
    _NavItem(Icons.account_balance_wallet_rounded, 'Pagos'),
    _NavItem(Icons.person_rounded, 'Perfil'),
  ];

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: OltviColors.principal,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.18),
            blurRadius: 24,
            offset: const Offset(0, -8),
          ),
        ],
      ),
      child: SafeArea(
        top: false,
        child: SizedBox(
          height: 68,
          child: Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: List.generate(_items.length, (i) {
              final selected = i == index;
              final item = _items[i];
              return Expanded(
                child: InkWell(
                  onTap: () => onChanged(i),
                  splashColor: OltviColors.accion.withOpacity(0.15),
                  highlightColor: Colors.transparent,
                  child: AnimatedContainer(
                    duration: const Duration(milliseconds: 250),
                    curve: Curves.easeOut,
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        AnimatedContainer(
                          duration: const Duration(milliseconds: 250),
                          padding: const EdgeInsets.symmetric(
                            horizontal: 14,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: selected
                                ? OltviColors.accion.withOpacity(0.18)
                                : Colors.transparent,
                            borderRadius: BorderRadius.circular(20),
                          ),
                          child: Icon(
                            item.icon,
                            size: 22,
                            color: selected
                                ? OltviColors.accion
                                : Colors.white.withOpacity(0.55),
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          item.label,
                          style: TextStyle(
                            fontSize: 10,
                            fontWeight: selected
                                ? FontWeight.w800
                                : FontWeight.w500,
                            color: selected
                                ? Colors.white
                                : Colors.white.withOpacity(0.55),
                            letterSpacing: 0.3,
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

class _NavItem {
  final IconData icon;
  final String label;
  const _NavItem(this.icon, this.label);
}
