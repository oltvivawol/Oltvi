import 'dart:ui';
import 'package:flutter/material.dart';
import '../theme/oltvi_colors.dart';

/// Frosted glass panel used as the immersive bottom sheet on map screens.
class OltviGlassPanel extends StatelessWidget {
  final Widget child;
  final double blur;
  final Color tint;
  final EdgeInsets padding;
  final BorderRadius? borderRadius;
  final bool dark;

  const OltviGlassPanel({
    super.key,
    required this.child,
    this.blur = 14,
    this.tint = const Color(0xCCFFFFFF),
    this.padding = const EdgeInsets.all(20),
    this.borderRadius,
    this.dark = false,
  });

  @override
  Widget build(BuildContext context) {
    final radius = borderRadius ??
        const BorderRadius.vertical(top: Radius.circular(28));
    final effectiveTint = dark
        ? OltviColors.principal.withOpacity(0.85)
        : tint;
    return ClipRRect(
      borderRadius: radius,
      child: BackdropFilter(
        filter: ImageFilter.blur(sigmaX: blur, sigmaY: blur),
        child: Container(
          decoration: BoxDecoration(
            color: effectiveTint,
            borderRadius: radius,
            border: Border(
              top: BorderSide(
                color: dark
                    ? Colors.white.withOpacity(0.08)
                    : OltviColors.principal.withOpacity(0.06),
                width: 1,
              ),
            ),
            boxShadow: [
              BoxShadow(
                color: OltviColors.principal.withOpacity(0.15),
                blurRadius: 32,
                offset: const Offset(0, -8),
                spreadRadius: -8,
              ),
            ],
          ),
          child: Padding(padding: padding, child: child),
        ),
      ),
    );
  }
}

/// Drag handle for bottom sheets.
class OltviSheetHandle extends StatelessWidget {
  final Color color;
  const OltviSheetHandle({super.key, this.color = const Color(0xFFCBD5E1)});

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Container(
        width: 44,
        height: 5,
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: color,
          borderRadius: BorderRadius.circular(4),
        ),
      ),
    );
  }
}
