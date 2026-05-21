import 'dart:async';

enum ConnectivityState { online, offline, syncing }

/// Simple connectivity/sync engine.
/// In a real build this would observe network state and replay queued ops.
/// Here we expose a stream the UI can subscribe to for the connectivity badge.
class SyncService {
  SyncService._internal();
  static final SyncService instance = SyncService._internal();

  final _controller = StreamController<ConnectivityState>.broadcast();
  ConnectivityState _state = ConnectivityState.online;

  Stream<ConnectivityState> get stream => _controller.stream;
  ConnectivityState get state => _state;

  void setState(ConnectivityState s) {
    _state = s;
    _controller.add(s);
  }

  /// Simulates a sync cycle - the UI sees a syncing pulse, then online.
  Future<void> triggerSync() async {
    setState(ConnectivityState.syncing);
    await Future.delayed(const Duration(milliseconds: 900));
    setState(ConnectivityState.online);
  }

  void dispose() => _controller.close();
}
