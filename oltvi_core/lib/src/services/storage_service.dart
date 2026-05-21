import 'dart:convert';
import 'package:shared_preferences/shared_preferences.dart';

/// Lightweight local storage for offline-first persistence.
/// All operations are safe: failures return defaults, never throw.
class StorageService {
  StorageService._();

  static SharedPreferences? _prefs;

  static Future<void> init() async {
    _prefs ??= await SharedPreferences.getInstance();
  }

  static Future<SharedPreferences> _instance() async {
    _prefs ??= await SharedPreferences.getInstance();
    return _prefs!;
  }

  static Future<void> setString(String key, String value) async {
    final p = await _instance();
    await p.setString(key, value);
  }

  static Future<String?> getString(String key) async {
    final p = await _instance();
    return p.getString(key);
  }

  static Future<void> setJson(String key, Map<String, dynamic> value) async {
    await setString(key, jsonEncode(value));
  }

  static Future<Map<String, dynamic>?> getJson(String key) async {
    final s = await getString(key);
    if (s == null) return null;
    try {
      return jsonDecode(s) as Map<String, dynamic>;
    } catch (_) {
      return null;
    }
  }

  static Future<void> setList(String key, List<Map<String, dynamic>> list) async {
    await setString(key, jsonEncode(list));
  }

  static Future<List<Map<String, dynamic>>> getList(String key) async {
    final s = await getString(key);
    if (s == null) return const [];
    try {
      final decoded = jsonDecode(s);
      if (decoded is! List) return const [];
      return decoded.cast<Map<String, dynamic>>();
    } catch (_) {
      return const [];
    }
  }

  static Future<void> setBool(String key, bool value) async {
    final p = await _instance();
    await p.setBool(key, value);
  }

  static Future<bool> getBool(String key, {bool fallback = false}) async {
    final p = await _instance();
    return p.getBool(key) ?? fallback;
  }

  static Future<void> remove(String key) async {
    final p = await _instance();
    await p.remove(key);
  }
}
