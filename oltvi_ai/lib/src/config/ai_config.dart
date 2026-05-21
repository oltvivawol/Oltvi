import 'package:google_generative_ai/google_generative_ai.dart';

class AiConfig {
  static const String modelFast = 'gemini-2.0-flash';
  static const String modelPro = 'gemini-1.5-pro';

  static String? _apiKey;
  static bool _mockMode = false;

  static void init({required String apiKey}) {
    _apiKey = apiKey;
    _mockMode = false;
  }

  static void initMock() {
    _mockMode = true;
    _apiKey = 'mock';
  }

  static bool get isMockMode => _mockMode;
  static String get apiKey => _apiKey ?? (throw StateError('AiConfig.init() not called'));

  static GenerativeModel buildModel({
    String model = modelFast,
    String? systemInstruction,
    List<Tool>? tools,
    GenerationConfig? generationConfig,
  }) {
    return GenerativeModel(
      model: model,
      apiKey: apiKey,
      tools: tools,
      systemInstruction: systemInstruction != null
          ? Content.system(systemInstruction)
          : null,
      generationConfig: generationConfig ??
          GenerationConfig(
            temperature: 0.7,
            maxOutputTokens: 1024,
          ),
    );
  }
}
