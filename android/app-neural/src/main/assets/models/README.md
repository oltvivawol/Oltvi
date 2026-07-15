# Modelos 3D — Capa Neural

## Estructura esperada

```
assets/models/
├── avatar_default.glb      ← Personaje humanoide rigged (idle/walk/jump/crouch/punch animations)
├── avatar_m.glb            ← Variante masculina
├── avatar_f.glb            ← Variante femenina
├── clothing/
│   ├── torso_hoodie_gray.glb
│   ├── torso_neural_tee.glb
│   ├── head_cap_neural.glb
│   └── ... (id de prenda + .glb)
└── environments/
    ├── urban_day.hdr        ← IBL para iluminación diurna
    └── urban_night.hdr      ← IBL para iluminación nocturna
```

## Especificaciones del avatar

- **Formato:** glTF 2.0 (.glb, binary)
- **Polígonos:** < 15.000 tris (mobile-friendly)
- **Rig:** esqueleto humanoid compatible con Mixamo o ReadyPlayerMe
- **Animaciones incluidas en el glb:**
  - `idle` — respiración suave en bucle
  - `walk` — ciclo de caminata
  - `run` — ciclo de carrera
  - `jump` — salto y aterrizaje (one-shot)
  - `crouch` — agacharse/levantarse
  - `grab` — agarrar objeto
  - `punch` — golpe (combo 1-2)
- **Materiales:** PBR (metallic-roughness), Skin separado de ropa para swap

## Placeholder temporal

Mientras no hay arte final, `Avatar3DScreen` carga primitivos de Filament
(esfera para cabeza, cápsula para cuerpo) para probar los controles y la cámara.
El swap al modelo real se hace reemplazando el archivo y actualizando
`AVATAR_MODEL_PATH` en `Avatar3DScreen.kt`.

## Storage GCP

Los modelos también se sirven desde Firebase Storage para actualizaciones
sin publish a la Play Store:
- Bucket: `project-34c5d1da-2226-4ace-bb3.firebasestorage.app`
- Path: `neural/models/avatar_default.glb`
- Path ropa: `neural/models/clothing/{prendaId}.glb`
