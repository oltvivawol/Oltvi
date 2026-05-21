import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import '../theme/oltvi_colors.dart';
import '../theme/oltvi_text_styles.dart';
import '../services/sync_service.dart';
import 'oltvi_logo.dart';

class OltviAppBar extends StatelessWidget implements PreferredSizeWidget {
  final String title;
  final String? subtitle;
  final List<Widget>? actions;
  final Widget? leading;
  final bool showLogo;
  final bool showSyncBadge;
  final Color background;
  final Color foreground;

  const OltviAppBar({
    super.key,
    required this.title,
    this.subtitle,
    this.actions,
    this.leading,
    this.showLogo = true,
    this.showSyncBadge = true,
    this.background = OltviColors.principal,
    this.foreground = OltviColors.textoBlanco,
  });

  @override
  Size get preferredSize => const Size.fromHeight(72);

  @override
  Widget build(BuildContext context) {
    return AnnotatedRegion<SystemUiOverlayStyle>(
      value: const SystemUiOverlayStyle(
        statusBarColor: Colors.transparent,
        statusBarIconBrightness: Brightness.light,
      ),
      child: Material(
        color: background,
        elevation: 0,
        child: SafeArea(
          bottom: false,
          child: SizedBox(
            height: 72,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16),
              child: Row(
                children: [
                  if (leading != null) ...[
                    leading!,
                    const SizedBox(width: 8),
                  ] else if (showLogo) ...[
                    const OltviLogo(size: 36),
                    const SizedBox(width: 12),
                  ],
                  Expanded(
                    child: Column(
                      mainAxisAlignment: MainAxisAlignment.center,
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          title,
                          style: OltviTextStyles.subtitulo.copyWith(
                            color: foreground,
                            fontSize: 17,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        if (subtitle != null)
                          Text(
                            subtitle!,
                            style: OltviTextStyles.pequeno.copyWith(
                              color: foreground.withOpacity(0.65),
                              letterSpacing: 0.4,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                      ],
                    ),
                  ),
                  if (showSyncBadge) const _SyncBadge(),
                  if (actions != null) ...actions!,
                ],
              ),
            ),
          ),
        ),
      ),
    );
  }
}

class _SyncBadge extends StatelessWidget {
  const _SyncBadge();

  @override
  Widget build(BuildContext context) {
    return StreamBuilder<ConnectivityState>(
      stream: SyncService.instance.stream,
      initialData: SyncService.instance.state,
      builder: (context, snapshot) {
        final state = snapshot.data ?? ConnectivityState.online;
        IconData icon;
        Color color;
        switch (state) {
          case ConnectivityState.online:
            icon = Icons.cloud_done_rounded;
            color = OltviColors.exito;
            break;
          case ConnectivityState.offline:
            icon = Icons.cloud_off_rounded;
            color = OltviColors.alerta;
            break;
          case ConnectivityState.syncing:
            icon = Icons.sync_rounded;
            color = OltviColors.accion;
            break;
        }
        return Container(
          margin: const EdgeInsets.symmetric(horizontal: 4),
          padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
          decoration: BoxDecoration(
            color: color.withOpacity(0.15),
            borderRadius: BorderRadius.circular(20),
            border: Border.all(color: color.withOpacity(0.6), width: 1),
          ),
          child: Icon(icon, color: color, size: 16),
        );
      },
    );
  }
}
