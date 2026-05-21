import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:oltvi_core/oltvi_core.dart';

import 'app/oltvi_usuario_app.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await StorageService.init();
  runApp(const OltviUsuarioRoot());
}

class OltviUsuarioRoot extends StatelessWidget {
  const OltviUsuarioRoot({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OLTVI · Viajes',
      debugShowCheckedModeBanner: false,
      theme: OltviTheme.light(),
      darkTheme: OltviTheme.dark(),
      themeMode: ThemeMode.system,
      localizationsDelegates: const [
        GlobalMaterialLocalizations.delegate,
        GlobalWidgetsLocalizations.delegate,
        GlobalCupertinoLocalizations.delegate,
      ],
      supportedLocales: const [
        Locale('es'),
        Locale('en'),
        Locale('pt'),
      ],
      locale: const Locale('es'),
      home: const OltviUsuarioApp(),
    );
  }
}
