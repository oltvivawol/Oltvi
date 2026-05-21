import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:oltvi_core/oltvi_core.dart';

import 'app/oltvi_trabajo_app.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const OltviTrabajoRoot());
}

class OltviTrabajoRoot extends StatelessWidget {
  const OltviTrabajoRoot({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OLTVI Trabajo',
      debugShowCheckedModeBanner: false,
      theme: OltviTheme.light(),
      darkTheme: OltviTheme.dark(),
      themeMode: ThemeMode.dark, // El conductor usa modo oscuro por defecto
      locale: const Locale('es'),
      supportedLocales: const [
        Locale('es'),
        Locale('en'),
        Locale('pt'),
      ],
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      home: const OltviTrabajoApp(),
    );
  }
}
