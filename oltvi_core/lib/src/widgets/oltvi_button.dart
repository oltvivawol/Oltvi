import 'package:flutter/material.dart';
import '../theme/oltvi_colors.dart';
import '../theme/oltvi_text_styles.dart';

/// The signature OLTVI action button - gradient orange, raised, big.
class OltviPrimaryButton extends StatelessWidget {
  final String label;
  final IconData? icon;
  final VoidCallback? onPressed;
  final double height;
  final bool expanded;
  final bool loading;

  const OltviPrimaryButton({
    super.key,
    required this.label,
    this.icon,
    this.onPressed,
    this.height = 64,
    this.expanded = true,
    this.loading = false,
  });

  @override
  Widget build(BuildContext context) {
    final disabled = onPressed == null || loading;
    final button = AnimatedOpacity(
      duration: const Duration(milliseconds: 150),
      opacity: disabled ? 0.55 : 1.0,
      child: Container(
        height: height,
        decoration: BoxDecoration(
          gradient: OltviColors.accionGradient,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: OltviColors.accion.withOpacity(0.45),
              blurRadius: 24,
              offset: const Offset(0, 10),
              spreadRadius: -4,
            ),
          ],
        ),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: disabled ? null : onPressed,
            child: Padding(
              padding: const EdgeInsets.symmetric(horizontal: 20),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  if (loading)
                    const SizedBox(
                      width: 22,
                      height: 22,
                      child: CircularProgressIndicator(
                        color: Colors.white,
                        strokeWidth: 2.5,
                      ),
                    )
                  else if (icon != null) ...[
                    Icon(icon, color: Colors.white, size: 24),
                    const SizedBox(width: 12),
                  ],
                  if (!loading)
                    Flexible(
                      child: Text(
                        label,
                        style: OltviTextStyles.botonGrande,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                ],
              ),
            ),
          ),
        ),
      ),
    );
    return expanded ? button : IntrinsicWidth(child: button);
  }
}

class OltviSecondaryButton extends StatelessWidget {
  final String label;
  final IconData? icon;
  final VoidCallback? onPressed;
  final Color color;

  const OltviSecondaryButton({
    super.key,
    required this.label,
    this.icon,
    this.onPressed,
    this.color = OltviColors.principal,
  });

  @override
  Widget build(BuildContext context) {
    return OutlinedButton.icon(
      onPressed: onPressed,
      icon: icon != null ? Icon(icon, size: 18) : const SizedBox.shrink(),
      label: Text(label),
      style: OutlinedButton.styleFrom(
        foregroundColor: color,
        side: BorderSide(color: color.withOpacity(0.4), width: 1.4),
        minimumSize: const Size(80, 44),
        padding: const EdgeInsets.symmetric(horizontal: 16),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
        textStyle: const TextStyle(fontWeight: FontWeight.w600),
      ),
    );
  }
}
