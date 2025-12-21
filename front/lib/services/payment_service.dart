import 'dart:convert';
import 'package:http/http.dart' as http;

class PaymentService {
  static const String baseUrl = 'http://localhost:8081/api/payments';

  /// Create a new payment request from clinic to patient
  static Future<Map<String, dynamic>?> createPaymentRequest({
    required int clinicId,
    required String patientWallet,
    required double amount,
    required String description,
  }) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/create'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'clinicId': clinicId,
          'patientWallet': patientWallet,
          'amount': amount,
          'description': description,
        }),
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      return null;
    } catch (e) {
      print('Error creating payment request: $e');
      return null;
    }
  }

  /// Get all payment requests for a patient
  static Future<List<Map<String, dynamic>>> getPatientPayments(int patientId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/patient/$patientId'),
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.cast<Map<String, dynamic>>();
      }
      return [];
    } catch (e) {
      print('Error getting patient payments: $e');
      return [];
    }
  }

  /// Get pending payment requests for a patient
  static Future<List<Map<String, dynamic>>> getPendingPatientPayments(int patientId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/patient/$patientId/pending'),
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.cast<Map<String, dynamic>>();
      }
      return [];
    } catch (e) {
      print('Error getting pending payments: $e');
      return [];
    }
  }

  /// Get all payment requests for a clinic
  static Future<List<Map<String, dynamic>>> getClinicPayments(int clinicId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/clinic/$clinicId'),
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.cast<Map<String, dynamic>>();
      }
      return [];
    } catch (e) {
      print('Error getting clinic payments: $e');
      return [];
    }
  }

  /// Get pending payment requests for a clinic
  static Future<List<Map<String, dynamic>>> getPendingClinicPayments(int clinicId) async {
    try {
      final response = await http.get(
        Uri.parse('$baseUrl/clinic/$clinicId/pending'),
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.cast<Map<String, dynamic>>();
      }
      return [];
    } catch (e) {
      print('Error getting pending clinic payments: $e');
      return [];
    }
  }

  /// Mark a payment as paid (executes blockchain transfer)
  static Future<Map<String, dynamic>?> payWithPrivateKey(
    String requestId, 
    String privateKey,
  ) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/$requestId/pay'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'privateKey': privateKey}),
      );

      if (response.statusCode == 200) {
        return jsonDecode(response.body);
      }
      print('Pay failed: ${response.body}');
      return null;
    } catch (e) {
      print('Error paying: $e');
      return null;
    }
  }

  /// Mark a payment as paid (legacy - no blockchain)
  static Future<bool> markAsPaid(String requestId, String transactionHash) async {
    try {
      final response = await http.post(
        Uri.parse('$baseUrl/$requestId/pay'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'transactionHash': transactionHash}),
      );

      return response.statusCode == 200;
    } catch (e) {
      print('Error marking payment as paid: $e');
      return false;
    }
  }
}
