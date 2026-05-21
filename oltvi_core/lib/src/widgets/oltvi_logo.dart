import 'package:flutter/material.dart';
import '../theme/oltvi_colors.dart';

/// Abstract OLTVI mark: interlocking route nodes & energy lines.
/// No vehicles, no animals, no letters - just dynamic geometric flow.
class OltviLogo extends StatelessWidget {
  final double size;
  final bool compact;
  const OltviLogo({super.key, this.size = 48, this.compact = false});

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: size,
      height: size,
      child: CustomPaint(painter: _OltviLogoPainter(compact: compact)),
    );
  }
}

class _OltviLogoPainter extends CustomPainter {
  final bool compact;
  _OltviLogoPainter({required this.compact});

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;

    // Rounded square backdrop (Android-style adaptive icon)
    final bgRect = RRect.fromRectAndRadius(
      Rect.fromLTWH(0, 0, w, h),
      Radius.circular(w * 0.22),
    );
    final bgPaint = Paint()
      ..shader = OltviColors.heroGradient.createShader(
        Rect.fromLTWH(0, 0, w, h),
      );
    canvas.drawRRect(bgRect, bgPaint);

    // Subtle inner glow
    canvas.drawRRect(
      bgRect,
      Paint()
        ..shader = RadialGradient(
          colors: [
            OltviColors.accion.withOpacity(0.25),
            OltviColors.accion.withOpacity(0.0),
          ],
        ).createShader(Rect.fromLTWH(w * 0.2, h * 0.2, w * 0.6, h * 0.6)),
    );

    // Three nodes connected forming an upward dynamic route
    final cx = w * 0.5;
    final cy = h * 0.5;
    final r = w * 0.07;

    final pA = Offset(w * 0.22, h * 0.74);
    final pB = Offset(w * 0.78, h * 0.74);
    final pC = Offset(cx, h * 0.24);

    // Energy curves
    final curvePaint = Paint()
      ..color = OltviColors.accionClaro
      ..strokeWidth = w * 0.045
      ..style = PaintingStyle.stroke
      ..strokeCap = StrokeCap.round;

    final path = Path()
      ..moveTo(pA.dx, pA.dy)
      ..quadraticBezierTo(cx - w * 0.18, cy + h * 0.02, pC.dx, pC.dy)
      ..quadraticBezierTo(cx + w * 0.18, cy + h * 0.02, pB.dx, pB.dy);
    canvas.drawPath(path, curvePaint);

    // Inner thinner highlight curve
    canvas.drawPath(
      path,
      Paint()
        ..color = Colors.white.withOpacity(0.85)
        ..strokeWidth = w * 0.012
        ..style = PaintingStyle.stroke
        ..strokeCap = StrokeCap.round,
    );

    // Nodes
    final nodePaint = Paint()..color = OltviColors.accion;
    final nodeStroke = Paint()
      ..color = Colors.white
      ..style = PaintingStyle.stroke
      ..strokeWidth = w * 0.018;
    for (final p in [pA, pB, pC]) {
      canvas.drawCircle(p, r, nodePaint);
      canvas.drawCircle(p, r, nodeStroke);
    }

    // Pulse halo on top node
    canvas.drawCircle(
      pC,
      r * 1.9,
      Paint()
        ..color = OltviColors.accion.withOpacity(0.25)
        ..style = PaintingStyle.stroke
        ..strokeWidth = w * 0.02,
    );
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}
