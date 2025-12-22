import 'dart:convert';
import 'package:http/http.dart' as http;

/// Service for getting risk predictions from the ML model
class RiskPredictionService {
  static const String baseUrl = 'http://localhost:8081/api/payments';
  
  /// Get risk prediction for a patient wallet and clinic
  static Future<RiskPrediction?> getRiskPrediction({
    required String patientWallet,
    required int clinicId,
    required double amount,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/risk'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'patientWallet': patientWallet,
          'clinicId': clinicId,
          'amount': amount,
        }),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        if (data['success'] == true) {
          return RiskPrediction(
            riskScore: (data['riskScore'] ?? 0.5).toDouble(),
            riskLevel: data['riskLevel'] ?? 'MEDIUM',
          );
        }
      }
      return null;
    } catch (e) {
      print('Error getting risk prediction: $e');
      return null;
    }
  }
  
  /// Check if ML service is available
  static Future<bool> isServiceAvailable() async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/risk/health'),
      );
      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return data['available'] == true;
      }
      return false;
    } catch (e) {
      return false;
    }
  }
}

class RiskPrediction {
  final double riskScore;
  final String riskLevel;
  
  RiskPrediction({
    required this.riskScore,
    required this.riskLevel,
  });
  
  /// Get color based on risk level
  RiskColor get color {
    switch (riskLevel) {
      case 'LOW':
        return RiskColor.green;
      case 'MEDIUM':
        return RiskColor.yellow;
      case 'HIGH':
        return RiskColor.red;
      default:
        return RiskColor.yellow;
    }
  }
  
  /// Get percentage for display
  int get percentage => (riskScore * 100).round();
  
  /// Get display text
  String get displayText {
    switch (riskLevel) {
      case 'LOW':
        return 'Low Risk';
      case 'MEDIUM':
        return 'Medium Risk';
      case 'HIGH':
        return 'High Risk';
      default:
        return 'Unknown Risk';
    }
  }
}

enum RiskColor {
  green,
  yellow,
  red,
}
