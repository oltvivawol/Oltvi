import 'package:flutter/material.dart';

/// OLTVI official color palette.
/// These are the only allowed brand colors - no variants without justification.
class OltviColors {
  OltviColors._();

  // Brand
  static const Color principal = Color(0xFF23374D); // Gris Azul Oscuro
  static const Color accion = Color(0xFFE67E22); // Naranja Humo Flotante
  static const Color accionClaro = Color(0xFFF39C12); // Naranja Suave

  // Backgrounds
  static const Color fondoGeneral = Color(0xFFF5F7FA); // Blanco Grisáceo
  static const Color fondoOscuro = Color(0xFF0F1924); // Dark mode bg
  static const Color superficie = Color(0xFFFFFFFF);
  static const Color superficieOscura = Color(0xFF1A2838);

  // Text
  static const Color textoPrincipal = Color(0xFF1A252F);
  static const Color textoSecundario = Color(0xFF64748B);
  static const Color textoBlanco = Color(0xFFFFFFFF);
  static const Color textoSobreOscuro = Color(0xFFE6EDF5);

  // States
  static const Color exito = Color(0xFF27AE60);
  static const Color alerta = Color(0xFFF1C40F);
  static const Color error = Color(0xFFE74C3C);

  // Glass / overlays
  static const Color cristal = Color(0x4023374D);
  static const Color cristalClaro = Color(0x66FFFFFF);

  // Map layers
  static const Color rutaActiva = Color(0xFFE67E22);
  static const Color rutaSecundaria = Color(0xFF64748B);

  /// Gradient used for the immersive hero panels.
  static const LinearGradient heroGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFF23374D), Color(0xFF0F1924)],
  );

  /// Action gradient used on the main CTAs.
  static const LinearGradient accionGradient = LinearGradient(
    begin: Alignment.topLeft,
    end: Alignment.bottomRight,
    colors: [Color(0xFFE67E22), Color(0xFFF39C12)],
  );
}
