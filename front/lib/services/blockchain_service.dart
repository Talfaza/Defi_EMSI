import 'package:http/http.dart';
import 'package:web3dart/web3dart.dart';

class BlockchainService {
  static const String rpcUrl = 'http://localhost:8545';
  
  late Web3Client _client;
  
  BlockchainService() {
    _client = Web3Client(rpcUrl, Client());
  }
  
  /// Get ETH balance for a wallet address
  Future<double> getBalance(String walletAddress) async {
    try {
      final address = EthereumAddress.fromHex(walletAddress);
      final balance = await _client.getBalance(address);
      
      // Convert from Wei to ETH
      final ethBalance = balance.getValueInUnit(EtherUnit.ether);
      return ethBalance.toDouble();
    } catch (e) {
      print('Error getting balance: $e');
      return 0.0;
    }
  }
  
  /// Format balance to display string
  static String formatBalance(double balance) {
    if (balance >= 1000) {
      return '${balance.toStringAsFixed(2)} ETH';
    } else if (balance >= 1) {
      return '${balance.toStringAsFixed(4)} ETH';
    } else {
      return '${balance.toStringAsFixed(6)} ETH';
    }
  }
  
  void dispose() {
    _client.dispose();
  }
}
