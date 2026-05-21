import 'package:intl/intl.dart';

class OltviFormat {
  OltviFormat._();

  static final _money = NumberFormat.currency(
    locale: 'es_AR',
    symbol: r'$',
    decimalDigits: 0,
  );
  static final _money2 = NumberFormat.currency(
    locale: 'es_AR',
    symbol: r'$',
    decimalDigits: 2,
  );
  static final _shortDate = DateFormat('dd MMM · HH:mm', 'es');
  static final _dayTime = DateFormat('EEEE HH:mm', 'es');

  static String money(num value) => _money.format(value);
  static String money2(num value) => _money2.format(value);

  static String date(DateTime d) => _shortDate.format(d);
  static String dayTime(DateTime d) => _dayTime.format(d);

  static String distance(double km) {
    if (km < 1.0) return '${(km * 1000).round()} m';
    if (km < 10) return '${km.toStringAsFixed(1)} km';
    return '${km.round()} km';
  }

  static String duration(int minutes) {
    if (minutes < 60) return '$minutes min';
    final h = minutes ~/ 60;
    final m = minutes % 60;
    if (m == 0) return '${h}h';
    return '${h}h ${m}m';
  }

  static String shortRating(double r) => r.toStringAsFixed(2);
}
