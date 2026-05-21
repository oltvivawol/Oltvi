import 'package:flutter/material.dart';
import '../theme/oltvi_colors.dart';

/// Floating card with the OLTVI signature elevation and rounded edges.
class OltviCard extends StatelessWidget {
  final Widget child;
  final EdgeInsetsGeometry padding;
  final VoidCallback? onTap;
  final Color? color;
  final Color? borderColor;
  final double borderRadius;

  const OltviCard({
    super.key,
    required this.child,
    this.padding = const EdgeInsets.all(16),
    this.onTap,
    this.color,
    this.borderColor,
    this.borderRadius = 16,
  });

  @override
  Widget build(BuildContext context) {
    final card = Container(
      decoration: BoxDecoration(
        color: color ?? Colors.white,
        borderRadius: BorderRadius.circular(borderRadius),
        border: borderColor != null
            ? Border.all(color: borderColor!, width: 1.2)
            : Border.all(color: const Color(0xFFEDF1F5), width: 1),
        boxShadow: [
          BoxShadow(
            color: OltviColors.principal.withOpacity(0.06),
            blurRadius: 18,
            offset: const Offset(0, 8),
            spreadRadius: -6,
          ),
        ],
      ),
      child: Padding(padding: padding, child: child),
    );

    if (onTap == null) return card;

    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(borderRadius),
        child: card,
      ),
    );
  }
}
