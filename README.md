# OLTVI · Android Nativo con IA

**Organización de Logística, Transporte y Vialidad Inteligente**  
*Tu ruta, nuestra inteligencia*

App de transporte **nativa Android** que supera a Uber y Didi con **8 agentes de IA Gemini**, gráficos de videojuego y el ecosistema completo de Google Cloud.

## Estructura

```
android/
├── core/              # Módulo compartido: modelos, tema, agentes IA, componentes, efectos GPU
├── app-usuario/       # App del pasajero — mapa 3D + IA matchmaking + asistente Olivi
├── app-conductor/     # App del conductor — HUD + copiloto IA + análisis de ganancias
└── app-jefe/          # Panel de admin — analíticas predictivas + alertas proactivas IA
```

## La Orquesta de Agentes IA

| Agente | Función |
|--------|---------|
| 🎯 AgenteMatchmaker | Asigna el conductor IDEAL con score multi-variable |
| 💰 AgentePrecio | Precios dinámicos TRANSPARENTES con desglose explicado |
| 🛡️ AgenteSeguridad | Monitoreo en tiempo real: desvíos, paradas, velocidad |
| 🗺️ AgenteRuta | Rutas óptimas con tráfico + clima + 3 alternativas |
| 💬 AgenteSoporte "Olivi" | Resuelve el 90% de problemas sin intervención humana |
| 🔍 AgenteFraude | Detecta viajes fantasma y fraude de pagos |
| 🚗 AgenteCopiloto | Coach del conductor: zonas calientes y análisis de ganancias |
| 📊 AgenteOperaciones | Analíticas predictivas y alertas proactivas para el jefe |

## Tecnología

- **Kotlin 1.9** + **Jetpack Compose** + Material 3
- **Google Maps SDK** — mapa 3D con tilt 45°, estilo oscuro personalizado, edificios
- **Gemini AI** (`com.google.ai.client.generativeai 0.9`) — 8 agentes con function calling
- **Firebase** — Auth, Firestore, FCM
- **Hilt** — inyección de dependencias
- **Coroutines + Flow** — arquitectura reactiva
- **MVVM + Clean Architecture**

## Efectos Visuales (nivel videojuego)

- `PulseRing` — anillos concéntricos animados en ubicación del usuario
- `ParticleField` — campo de 80 partículas flotantes (Canvas + withFrameMillis)
- `NeonGlow` — brillo neón via BlurMaskFilter en GPU
- `GlassCard` — glassmorphism con gradiente en bordes
- `ConfettiExplosion` — 40 partículas al confirmar viaje
- `WaveformAnimation` — forma de onda para input de voz
- Mapa 3D oscuro + marcadores animados + polyline con glow

## Paleta oficial OLTVI

| Color | Hex | Uso |
|-------|-----|-----|
| Principal | `#23374D` | Fondos, estructura |
| Acción | `#E67E22` | CTAs, rutas, brillo neón |
| Acción claro | `#F39C12` | Acentos, gradiente |
| Éxito | `#27AE60` | Completado, seguro |
| Alerta | `#F1C40F` | Pendiente, demanda alta |
| Error | `#E74C3C` | Cancelado, emergencia |

## Cómo compilar

```bash
cd android
cp local.properties.example local.properties
# Completar con tus claves de Google Cloud

./gradlew :app-usuario:assembleDebug
./gradlew :app-conductor:assembleDebug
./gradlew :app-jefe:assembleDebug
```

## GitHub Actions — APKs automáticos

Configurar secrets: `MAPS_API_KEY`, `GEMINI_API_KEY`, `GOOGLE_SERVICES_JSON`.  
Push a `main` → 3 APKs en **Actions → Artifacts**.  
Tag `v1.0.0` → Release con los 3 APKs adjuntos.

## Por qué OLTVI supera a Uber y Didi

| | Uber / Didi | **OLTVI** |
|---|---|---|
| Matching | Solo proximidad | IA multi-variable (30% dist + 25% rating + ...) |
| Precio | Algoritmo opaco | IA con desglose visible y explicación en español |
| Seguridad | Botón de emergencia | IA monitorea ruta, velocidad y paradas en tiempo real |
| Soporte | Humano (20-30 min) | Olivi IA resuelve en segundos |
| Conductor | Mapa + destino | Copiloto IA con zonas calientes y coaching de ganancias |
| Admin | Dashboard estático | Analíticas predictivas con alertas proactivas |
| Gráficos | Interfaz funcional | Efectos GPU, partículas, neón, mapa 3D |
