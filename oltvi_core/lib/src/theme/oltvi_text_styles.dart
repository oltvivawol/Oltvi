import 'package:flutter/material.dart';
import 'oltvi_colors.dart';

class OltviTextStyles {
  OltviTextStyles._();

  static const String fontFamily = 'Roboto';

  static const TextStyle display = TextStyle(
    fontFamily: fontFamily,
    fontSize: 32,
    fontWeight: FontWeight.w800,
    color: OltviColors.textoPrincipal,
    letterSpacing: -0.5,
  );

  static const TextStyle titulo = TextStyle(
    fontFamily: fontFamily,
    fontSize: 22,
    fontWeight: FontWeight.w700,
    color: OltviColors.textoPrincipal,
  );

  static const TextStyle subtitulo = TextStyle(
    fontFamily: fontFamily,
    fontSize: 18,
    fontWeight: FontWeight.w600,
    color: OltviColors.textoPrincipal,
  );

  static const TextStyle cuerpo = TextStyle(
    fontFamily: fontFamily,
    fontSize: 14,
    fontWeight: FontWeight.w400,
    color: OltviColors.textoPrincipal,
  );

  static const TextStyle cuerpoSecundario = TextStyle(
    fontFamily: fontFamily,
    fontSize: 14,
    fontWeight: FontWeight.w400,
    color: OltviColors.textoSecundario,
  );

  static const TextStyle pequeno = TextStyle(
    fontFamily: fontFamily,
    fontSize: 12,
    fontWeight: FontWeight.w500,
    color: OltviColors.textoSecundario,
  );

  static const TextStyle botonGrande = TextStyle(
    fontFamily: fontFamily,
    fontSize: 18,
    fontWeight: FontWeight.w700,
    color: OltviColors.textoBlanco,
    letterSpacing: 0.5,
  );

  static const TextStyle botonChico = TextStyle(
    fontFamily: fontFamily,
    fontSize: 14,
    fontWeight: FontWeight.w600,
    color: OltviColors.textoBlanco,
    letterSpacing: 0.3,
  );

  static const TextStyle hud = TextStyle(
    fontFamily: fontFamily,
    fontSize: 28,
    fontWeight: FontWeight.w800,
    color: OltviColors.textoBlanco,
    letterSpacing: -0.5,
  );

  static const TextStyle eyebrow = TextStyle(
    fontFamily: fontFamily,
    fontSize: 11,
    fontWeight: FontWeight.w700,
    color: OltviColors.accion,
    letterSpacing: 2.0,
  );
}
