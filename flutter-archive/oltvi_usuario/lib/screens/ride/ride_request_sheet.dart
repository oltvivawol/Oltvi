import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:latlong2/latlong.dart';
import 'package:oltvi_core/oltvi_core.dart';

/// Destination picker with smart suggestions. In a real build this
/// would proxy to a places autocomplete service - here we use a curated
/// list around the user's region so it always works.
class RideRequestSheet extends StatefulWidget {
  final PuntoGeo origen;
  const RideRequestSheet({super.key, required this.origen});

  @override
  State<RideRequestSheet> createState() => _RideRequestSheetState();
}

class _RideRequestSheetState extends State<RideRequestSheet> {
  final _query = TextEditingController();
  String _q = '';

  static const _suggestions = <_Suggestion>[
    _Suggestion(
      icon: Icons.flight_takeoff_rounded,
      name: 'Aeroparque',
      detail: 'Av. Costanera Rafael Obligado',
      lat: -34.5592,
      lng: -58.4156,
      tag: 'Frecuente',
    ),
    _Suggestion(
      icon: Icons.train_rounded,
      name: 'Estación Retiro',
      detail: 'Av. del Libertador 405',
      lat: -34.5915,
      lng: -58.3743,
    ),
    _Suggestion(
      icon: Icons.local_mall_rounded,
      name: 'Alto Palermo',
      detail: 'Av. Santa Fe 3253',
      lat: -34.5882,
      lng: -58.4106,
    ),
    _Suggestion(
      icon: Icons.business_rounded,
      name: 'Puerto Madero',
      detail: 'Dock 4',
      lat: -34.6090,
      lng: -58.3640,
      tag: 'Trabajo',
    ),
    _Suggestion(
      icon: Icons.local_hospital_rounded,
      name: 'Hospital Italiano',
      detail: 'Tte. Gral. Juan Domingo Perón 4190',
      lat: -34.6019,
      lng: -58.4271,
    ),
    _Suggestion(
      icon: Icons.account_balance_rounded,
      name: 'UBA - Ciudad Universitaria',
      detail: 'Pabellón II',
      lat: -34.5430,
      lng: -58.4434,
    ),
    _Suggestion(
      icon: Icons.park_rounded,
      name: 'Parque Centenario',
      detail: 'Caballito',
      lat: -34.6064,
      lng: -58.4358,
    ),
    _Suggestion(
      icon: Icons.stadium_rounded,
      name: 'Estadio Monumental',
      detail: 'Núñez',
      lat: -34.5454,
      lng: -58.4498,
    ),
    _Suggestion(
      icon: Icons.home_rounded,
      name: 'Casa',
      detail: 'Av. Cabildo 2200, Belgrano',
      lat: -34.5631,
      lng: -58.4565,
      tag: 'Guardado',
    ),
    _Suggestion(
      icon: Icons.work_rounded,
      name: 'Oficina',
      detail: 'Reconquista 1088, Microcentro',
      lat: -34.5953,
      lng: -58.3735,
      tag: 'Guardado',
    ),
  ];

  @override
  void dispose() {
    _query.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final viewInsets = MediaQuery.of(context).viewInsets.bottom;
    final filtered = _q.isEmpty
        ? _suggestions
        : _suggestions
            .where((s) =>
                s.name.toLowerCase().contains(_q.toLowerCase()) ||
                s.detail.toLowerCase().contains(_q.toLowerCase()))
            .toList();

    return Padding(
      padding: EdgeInsets.only(bottom: viewInsets),
      child: DraggableScrollableSheet(
        initialChildSize: 0.85,
        minChildSize: 0.5,
        maxChildSize: 0.95,
        expand: false,
        builder: (context, scrollController) {
          return OltviGlassPanel(
            tint: Colors.white,
            padding: EdgeInsets.zero,
            borderRadius: const BorderRadius.vertical(
              top: Radius.circular(28),
            ),
            child: Column(
              children: [
                const SizedBox(height: 12),
                const OltviSheetHandle(),
                Padding(
                  padding: const EdgeInsets.fromLTRB(20, 0, 20, 12),
                  child: Row(
                    children: [
                      const Expanded(
                        child: Text(
                          '¿A dónde vamos?',
                          style: TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.w800,
                            color: OltviColors.textoPrincipal,
                          ),
                        ),
                      ),
                      Material(
                        color: OltviColors.fondoGeneral,
                        shape: const CircleBorder(),
                        child: InkWell(
                          customBorder: const CircleBorder(),
                          onTap: () => Navigator.of(context).pop(),
                          child: const SizedBox(
                            width: 36,
                            height: 36,
                            child: Icon(
                              Icons.close_rounded,
                              size: 18,
                              color: OltviColors.textoPrincipal,
                            ),
                          ),
                        ),
                      ),
                    ],
                  ),
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Container(
                    decoration: BoxDecoration(
                      color: OltviColors.fondoGeneral,
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Column(
                      children: [
                        _OriginRow(text: widget.origen.nombre ?? 'Mi ubicación'),
                        Container(
                          height: 1,
                          margin: const EdgeInsets.symmetric(horizontal: 16),
                          color: const Color(0xFFE2E8F0),
                        ),
                        Padding(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 16,
                            vertical: 4,
                          ),
                          child: Row(
                            children: [
                              Container(
                                width: 10,
                                height: 10,
                                decoration: BoxDecoration(
                                  color: OltviColors.exito,
                                  borderRadius: BorderRadius.circular(2),
                                ),
                              ),
                              const SizedBox(width: 14),
                              Expanded(
                                child: TextField(
                                  controller: _query,
                                  autofocus: true,
                                  decoration: const InputDecoration(
                                    hintText: 'Buscar destino, lugar o dirección',
                                    border: InputBorder.none,
                                    enabledBorder: InputBorder.none,
                                    focusedBorder: InputBorder.none,
                                    filled: false,
                                    contentPadding:
                                        EdgeInsets.symmetric(vertical: 14),
                                  ),
                                  onChanged: (v) => setState(() => _q = v),
                                ),
                              ),
                            ],
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 16),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 20),
                  child: Row(
                    children: const [
                      Text(
                        'SUGERIDOS',
                        style: OltviTextStyles.eyebrow,
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 6),
                Expanded(
                  child: ListView.separated(
                    controller: scrollController,
                    padding: const EdgeInsets.fromLTRB(20, 4, 20, 24),
                    itemCount: filtered.length,
                    separatorBuilder: (_, __) => const SizedBox(height: 6),
                    itemBuilder: (_, i) {
                      final s = filtered[i];
                      return _SuggestionTile(
                        suggestion: s,
                        onTap: () {
                          HapticFeedback.selectionClick();
                          Navigator.of(context).pop(
                            PuntoGeo(
                              lat: s.lat,
                              lng: s.lng,
                              nombre: s.name,
                              direccion: s.detail,
                            ),
                          );
                        },
                      );
                    },
                  ),
                ),
              ],
            ),
          );
        },
      ),
    );
  }
}

class _OriginRow extends StatelessWidget {
  final String text;
  const _OriginRow({required this.text});

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 14, 16, 4),
      child: Row(
        children: [
          Container(
            width: 10,
            height: 10,
            decoration: const BoxDecoration(
              color: OltviColors.principal,
              shape: BoxShape.circle,
            ),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Text(
              text,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                color: OltviColors.textoPrincipal,
              ),
              maxLines: 1,
              overflow: TextOverflow.ellipsis,
            ),
          ),
        ],
      ),
    );
  }
}

class _SuggestionTile extends StatelessWidget {
  final _Suggestion suggestion;
  final VoidCallback onTap;
  const _SuggestionTile({required this.suggestion, required this.onTap});

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.white,
      borderRadius: BorderRadius.circular(14),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(14),
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: OltviColors.fondoGeneral,
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(
                  suggestion.icon,
                  color: OltviColors.principal,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: Text(
                            suggestion.name,
                            style: const TextStyle(
                              fontSize: 15,
                              fontWeight: FontWeight.w700,
                              color: OltviColors.textoPrincipal,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        if (suggestion.tag != null) ...[
                          const SizedBox(width: 6),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 6,
                              vertical: 2,
                            ),
                            decoration: BoxDecoration(
                              color: OltviColors.accion.withOpacity(0.12),
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              suggestion.tag!.toUpperCase(),
                              style: const TextStyle(
                                fontSize: 9,
                                fontWeight: FontWeight.w800,
                                color: OltviColors.accion,
                                letterSpacing: 0.6,
                              ),
                            ),
                          ),
                        ]
                      ],
                    ),
                    const SizedBox(height: 2),
                    Text(
                      suggestion.detail,
                      style: const TextStyle(
                        fontSize: 12,
                        color: OltviColors.textoSecundario,
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ],
                ),
              ),
              const Icon(
                Icons.arrow_forward_ios_rounded,
                size: 14,
                color: OltviColors.textoSecundario,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _Suggestion {
  final IconData icon;
  final String name;
  final String detail;
  final double lat;
  final double lng;
  final String? tag;

  const _Suggestion({
    required this.icon,
    required this.name,
    required this.detail,
    required this.lat,
    required this.lng,
    this.tag,
  });
}
