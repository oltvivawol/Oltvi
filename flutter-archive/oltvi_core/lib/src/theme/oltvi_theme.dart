import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'oltvi_colors.dart';
import 'oltvi_text_styles.dart';

class OltviTheme {
  OltviTheme._();

  static ThemeData light() {
    final base = ThemeData.light(useMaterial3: true);
    return base.copyWith(
      colorScheme: const ColorScheme.light(
        primary: OltviColors.principal,
        onPrimary: OltviColors.textoBlanco,
        secondary: OltviColors.accion,
        onSecondary: OltviColors.textoBlanco,
        surface: OltviColors.superficie,
        onSurface: OltviColors.textoPrincipal,
        error: OltviColors.error,
        onError: OltviColors.textoBlanco,
      ),
      scaffoldBackgroundColor: OltviColors.fondoGeneral,
      textTheme: base.textTheme.copyWith(
        displayLarge: OltviTextStyles.display,
        titleLarge: OltviTextStyles.titulo,
        titleMedium: OltviTextStyles.subtitulo,
        bodyLarge: OltviTextStyles.cuerpo,
        bodyMedium: OltviTextStyles.cuerpo,
        bodySmall: OltviTextStyles.cuerpoSecundario,
        labelSmall: OltviTextStyles.pequeno,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: OltviColors.principal,
        foregroundColor: OltviColors.textoBlanco,
        elevation: 0,
        systemOverlayStyle: SystemUiOverlayStyle(
          statusBarColor: Colors.transparent,
          statusBarIconBrightness: Brightness.light,
        ),
        titleTextStyle: TextStyle(
          fontFamily: OltviTextStyles.fontFamily,
          fontSize: 18,
          fontWeight: FontWeight.w700,
          color: OltviColors.textoBlanco,
        ),
      ),
      cardTheme: CardTheme(
        color: OltviColors.superficie,
        elevation: 4,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        margin: EdgeInsets.zero,
      ),
      bottomNavigationBarTheme: const BottomNavigationBarThemeData(
        backgroundColor: OltviColors.principal,
        selectedItemColor: OltviColors.accion,
        unselectedItemColor: Color(0xFF8FA0B3),
        type: BottomNavigationBarType.fixed,
        elevation: 8,
        showUnselectedLabels: true,
        selectedLabelStyle: TextStyle(
          fontFamily: OltviTextStyles.fontFamily,
          fontSize: 11,
          fontWeight: FontWeight.w700,
        ),
        unselectedLabelStyle: TextStyle(
          fontFamily: OltviTextStyles.fontFamily,
          fontSize: 11,
          fontWeight: FontWeight.w500,
        ),
      ),
      elevatedButtonTheme: ElevatedButtonThemeData(
        style: ElevatedButton.styleFrom(
          backgroundColor: OltviColors.accion,
          foregroundColor: OltviColors.textoBlanco,
          elevation: 6,
          shadowColor: OltviColors.accion.withOpacity(0.5),
          minimumSize: const Size(120, 48),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          textStyle: OltviTextStyles.botonChico,
        ),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: Colors.white,
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 14,
        ),
        border: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFFE2E8F0)),
        ),
        enabledBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: Color(0xFFE2E8F0)),
        ),
        focusedBorder: OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: const BorderSide(color: OltviColors.accion, width: 2),
        ),
        hintStyle: const TextStyle(
          color: OltviColors.textoSecundario,
          fontSize: 14,
        ),
      ),
      dividerTheme: const DividerThemeData(
        color: Color(0xFFE2E8F0),
        thickness: 1,
        space: 1,
      ),
      pageTransitionsTheme: const PageTransitionsTheme(
        builders: {
          TargetPlatform.android: ZoomPageTransitionsBuilder(),
          TargetPlatform.iOS: CupertinoPageTransitionsBuilder(),
        },
      ),
    );
  }

  static ThemeData dark() {
    final base = ThemeData.dark(useMaterial3: true);
    return base.copyWith(
      colorScheme: const ColorScheme.dark(
        primary: OltviColors.accion,
        onPrimary: OltviColors.textoBlanco,
        secondary: OltviColors.accionClaro,
        onSecondary: OltviColors.textoBlanco,
        surface: OltviColors.superficieOscura,
        onSurface: OltviColors.textoSobreOscuro,
        error: OltviColors.error,
      ),
      scaffoldBackgroundColor: OltviColors.fondoOscuro,
      textTheme: base.textTheme.apply(
        bodyColor: OltviColors.textoSobreOscuro,
        displayColor: OltviColors.textoSobreOscuro,
        fontFamily: OltviTextStyles.fontFamily,
      ),
      appBarTheme: const AppBarTheme(
        backgroundColor: OltviColors.fondoOscuro,
        foregroundColor: OltviColors.textoBlanco,
        elevation: 0,
      ),
      cardTheme: CardTheme(
        color: OltviColors.superficieOscura,
        elevation: 4,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        margin: EdgeInsets.zero,
      ),
    );
  }
}
