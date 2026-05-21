import 'package:flutter/material.dart';
import 'package:oltvi_core/oltvi_core.dart';

class UsuariosScreen extends StatefulWidget {
  const UsuariosScreen({super.key});

  @override
  State<UsuariosScreen> createState() => _UsuariosScreenState();
}

class _UsuariosScreenState extends State<UsuariosScreen> {
  TipoPerfil _filtro = TipoPerfil.cliente;

  @override
  Widget build(BuildContext context) {
    final usuarios = MockDataService.instance.usuarios
        .where((u) => u.perfil == _filtro)
        .toList();

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        elevation: 0,
        title: Text('Usuarios',
            style: OltviTextStyles.titulo.copyWith(color: Colors.white)),
      ),
      body: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
            child: Container(
              padding: const EdgeInsets.all(4),
              decoration: BoxDecoration(
                color: Colors.white.withOpacity(0.05),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Row(
                children: [
                  _tabBtn(TipoPerfil.cliente, 'Clientes'),
                  _tabBtn(TipoPerfil.conductor, 'Conductores'),
                  _tabBtn(TipoPerfil.admin, 'Admins'),
                ],
              ),
            ),
          ),
          Expanded(
            child: usuarios.isEmpty
                ? Center(
                    child: Text('Sin usuarios en esta categoría',
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.5),
                        )),
                  )
                : ListView.builder(
                    padding: const EdgeInsets.fromLTRB(16, 0, 16, 16),
                    itemCount: usuarios.length,
                    itemBuilder: (_, i) => _usuarioFila(usuarios[i]),
                  ),
          ),
        ],
      ),
    );
  }

  Widget _tabBtn(TipoPerfil tipo, String label) {
    final selected = tipo == _filtro;
    return Expanded(
      child: GestureDetector(
        onTap: () => setState(() => _filtro = tipo),
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 10),
          decoration: BoxDecoration(
            gradient: selected ? OltviColors.accionGradient : null,
            borderRadius: BorderRadius.circular(10),
          ),
          child: Center(
            child: Text(label,
                style: OltviTextStyles.cuerpo.copyWith(
                  color: selected
                      ? Colors.white
                      : Colors.white.withOpacity(0.5),
                  fontWeight:
                      selected ? FontWeight.w700 : FontWeight.w500,
                )),
          ),
        ),
      ),
    );
  }

  Widget _usuarioFila(Usuario u) {
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF0E1620),
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: Colors.white.withOpacity(0.05)),
      ),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(
              gradient: OltviColors.heroGradient,
              borderRadius: BorderRadius.circular(12),
            ),
            child: Center(
              child: Text(
                u.inicial,
                style: OltviTextStyles.titulo
                    .copyWith(color: Colors.white),
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(u.nombre,
                    style: OltviTextStyles.subtitulo
                        .copyWith(color: Colors.white)),
                Text(u.correo,
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: OltviTextStyles.cuerpo.copyWith(
                      color: Colors.white.withOpacity(0.5),
                      fontSize: 12,
                    )),
                const SizedBox(height: 4),
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.symmetric(
                          horizontal: 6, vertical: 2),
                      decoration: BoxDecoration(
                        color: OltviColors.accion.withOpacity(0.15),
                        borderRadius: BorderRadius.circular(6),
                      ),
                      child: Text(u.nivelLabel,
                          style: OltviTextStyles.eyebrow.copyWith(
                            color: OltviColors.accion,
                            fontSize: 9,
                          )),
                    ),
                    const SizedBox(width: 6),
                    Icon(Icons.star,
                        color: Colors.white.withOpacity(0.5), size: 12),
                    const SizedBox(width: 2),
                    Text(u.rating.toStringAsFixed(2),
                        style: OltviTextStyles.cuerpo.copyWith(
                          color: Colors.white.withOpacity(0.7),
                          fontSize: 11,
                        )),
                    const SizedBox(width: 6),
                    if (u.verificado)
                      const Icon(Icons.verified,
                          color: OltviColors.exito, size: 14),
                  ],
                ),
              ],
            ),
          ),
          IconButton(
            onPressed: () {},
            icon: Icon(Icons.more_vert,
                color: Colors.white.withOpacity(0.5)),
          ),
        ],
      ),
    );
  }
}
