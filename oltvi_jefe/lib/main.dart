import 'package:flutter/material.dart';
import 'package:flutter_localizations/flutter_localizations.dart';
import 'package:oltvi_core/oltvi_core.dart';

import 'app/oltvi_jefe_app.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const OltviJefeRoot());
}

class OltviJefeRoot extends StatelessWidget {
  const OltviJefeRoot({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OLTVI Jefe',
      debugShowCheckedModeBanner: false,
      theme: OltviTheme.light(),
      darkTheme: OltviTheme.dark(),
      themeMode: ThemeMode.dark,
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
      home: const OltviJefeApp(),
    );
  }
}
